package com.hzq.auth.login.service;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author gc
 * @class com.hzq.auth.service CustomOAuth2UserService
 * @date 2024/11/4 18:19
 * @description 自定义用户信息加载服务
 */
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final Map<String, OAuth2UserService<OAuth2UserRequest, OAuth2User>> userServiceMap = new HashMap<>();

    public CustomOAuth2UserService() {
    }

    public CustomOAuth2UserService setOAuth2UserService(String registrationId, OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService) {
        this.userServiceMap.put(registrationId, oAuth2UserService);
        return this;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = Optional.ofNullable(userServiceMap.get(registrationId))
                .orElseThrow(() -> new OAuth2AuthenticationException("没有该OAuth2UserService实例"));

        return oAuth2UserService.loadUser(userRequest);
    }
}