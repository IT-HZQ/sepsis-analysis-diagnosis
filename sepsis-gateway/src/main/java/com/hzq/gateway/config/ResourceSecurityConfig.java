package com.hzq.gateway.config;

import com.hzq.core.result.ResultEnum;
import com.hzq.core.util.RSAUtils;
import com.hzq.gateway.util.WebFluxUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.security.interfaces.RSAPublicKey;

/**
 * @author hua
 * @className com.hzq.gateway.config ResourceServerConfig
 * @date 2024/9/26 20:32
 * @description 资源访问安全配置类，所有请求先进入这里，根据规则进行请求放行和认证处理，通过后进入网关全局过滤器
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableWebFluxSecurity
public class ResourceSecurityConfig {

    // 存放在 resource 下的公钥文件
    private static final String PUBLIC_KEY_FILE_NAME = "public.key";

    // 请求白名单，该集合中的路径，跳过认证，可直接进入系统
    private static final String[] whitesUrIs = new String[] {"/oauth/**"};

    /**
     * @param serverHttpSecurity ServerHttpSecurity 类似于 HttpSecurity 但适用于 WebFlux。
     * @return org.springframework.security.web.SecurityFilterChain
     * @author hua
     * @date 2024/9/26 21:29
     * @apiNote 基于 webflux 的 spring security 安全过滤配置，用于处理请求认证和授权
     **/
    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity serverHttpSecurity) {
        // 配置访问限制, 通过 URL 模式限制请求的访问
        serverHttpSecurity.authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                .pathMatchers(whitesUrIs).permitAll()
                .anyExchange().authenticated()
        );
        // 配置认证和授权失败的处理器
        serverHttpSecurity.exceptionHandling(exception -> exception
                .authenticationEntryPoint(customAuthenticationEntryPoint())
                .accessDeniedHandler(customAccessDeniedHandler())
        );
        // 配置 OAuth 2.0 资源服务器保护支持
        serverHttpSecurity.oauth2ResourceServer(oauth2 -> oauth2
                // 配置自定义 JWT 身份验证转换器, 以及jwt解码器
                .jwt(jwtSpec -> jwtSpec
                        .jwtAuthenticationConverter(customJwtConverter())
                        .jwtDecoder(reactiveJwtDecoder())
                )
        );
        // 其他配置
        serverHttpSecurity
                // 禁用默认登录页面
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                // 禁用默认登出页面
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // 配置 CORS 跨域
                .cors(corsSpec -> corsSpec.configurationSource(customCorsConfiguration()));

        log.info("sepsis gateway resource server config init successfully");
        return serverHttpSecurity.build();
    }

    /**
     * @author hua
     * @date 2024/9/26 21:39
     * @apiNote 自定义权限管理器，默认转换器 JwtGrantedAuthoritiesConverter
     * 1. ServerHttpSecurity 没有将 jwt 中 authorities 的负载部分当做 Authentication。需要把 jwt 的 Claim 中的 authorities 加入
     **/
    @Bean
    public Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> customJwtConverter() {
        // 创建 JwtGrantedAuthoritiesConverter，用于将 JWT 中的权限声明转换为 Spring Security 的 GrantedAuthority
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // 设置权限前缀，Spring Security 通常会为角色加上 "ROLE_" 前缀，这里手动指定前缀
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        // 指定 JWT 中用于存储权限声明的 Claim 名称，默认为 "authorities"
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
        // 创建 JwtAuthenticationConverter，用于将 JWT 转换为 Spring Security 的 Authentication 对象
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        // 将自定义的权限转换器设置到 JwtAuthenticationConverter 中
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        // 使用 ReactiveJwtAuthenticationConverterAdapter 包装 JwtAuthenticationConverter，使其适应响应式编程模型
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }

    /**
     * @return ReactiveJwtDecoder
     * @author hua
     * @date 2024/10/04
     * @apiNote 配置 Reactive JWT 解码器
     **/
    @SneakyThrows
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return NimbusReactiveJwtDecoder
                .withPublicKey((RSAPublicKey) RSAUtils.getPublicKey(RSAUtils.PUBLIC_KEY_CONTEXT))
                .build();
    }

    /**
     * @return org.springframework.security.web.server.ServerAuthenticationEntryPoint
     * @author hua
     * @date 2024/9/23 0:46
     * @apiNote 处理 token无效 或者 已过期 自定义响应
     * 1. 这里使用 ServerAuthenticationEntryPoint 而不是 AuthenticationEntryPoint
     * a) AuthenticationEntryPoint 适用于传统的 Spring Security（基于 Servlet 的应用），例如 Spring MVC 应用。
     * 处理请求的方式是基于阻塞式的 Servlet API (HttpServletRequest, HttpServletResponse)
     * b) ServerAuthenticationEntryPoint 适用于基于 Spring WebFlux 的应用（响应式应用），如 Spring WebFlux 的项目。
     * 处理请求的方式是基于非阻塞的 WebFlux API (ServerWebExchange)
     **/
    @Bean
    public ServerAuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (exchange, e) -> Mono.defer(() -> Mono.just(exchange.getResponse()))
                .flatMap(response -> WebFluxUtils.writeResponse(response, ResultEnum.TOKEN_INVALID_OR_EXPIRED));
    }

    /**
     * @return org.springframework.security.web.server.authorization.ServerAccessDeniedHandler
     * @author hua
     * @date 2024/9/26 20:53
     * @apiNote 处理用户未被授权 自定义响应
     * 1. 这里使用 ServerAccessDeniedHandler 而不是 AccessDeniedHandler
     * a) AccessDeniedHandler 适用于传统的 Spring Security（基于 Servlet 的应用），例如 Spring MVC 应用。
     * 处理请求的方式是基于阻塞式的 Servlet API (HttpServletRequest, HttpServletResponse)
     * b) ServerAccessDeniedHandler 适用于基于 Spring WebFlux 的应用（响应式应用），如 Spring WebFlux 的项目。
     * 处理请求的方式是基于非阻塞的 WebFlux API (ServerWebExchange)
     **/
    @Bean
    public ServerAccessDeniedHandler customAccessDeniedHandler() {
        return (exchange, denied) -> Mono.defer(() -> Mono.just(exchange.getResponse()))
                .flatMap(response -> WebFluxUtils.writeResponse(response, ResultEnum.ACCESS_UNAUTHORIZED));
    }

    /**
     * @return org.springframework.web.cors.reactive.CorsConfigurationSource
     * @author hua
     * @date 2024/9/28 8:21
     * @apiNote 自定义 CORS 跨域
     **/
    @Bean
    public CorsConfigurationSource customCorsConfiguration() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 放行所有域名，生产环境请对此进行修改
        corsConfiguration.addAllowedOriginPattern("*");
        // 放行的请求头
        corsConfiguration.addAllowedHeader("*");
        // 放行的请求方式，主要有：GET, POST, PUT, DELETE, OPTIONS
        corsConfiguration.addAllowedMethod("*");
        // 暴露头部信息
        corsConfiguration.addExposedHeader("*");
        // 是否允许发送cookie
        corsConfiguration.setAllowCredentials(true);
        // 使用 UrlBasedCorsConfigurationSource 来注册 CorsConfiguration, 注册路径为所有请求
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/**", corsConfiguration);
        return configSource;
    }
}
