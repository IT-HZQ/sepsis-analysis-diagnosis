package com.hzq.auth.service;

import com.hzq.auth.domain.LoginUser;
import com.hzq.core.result.ResultEnum;
import com.hzq.system.api.SysRoleFeignClient;
import com.hzq.system.api.SysUserFeignClient;
import com.hzq.system.api.SysUserRoleFeignClient;
import com.hzq.system.dto.SysUserDTO;
import com.hzq.web.exception.SystemException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author hua
 * @className com.hzq.auth.service LoginUserDetailService
 * @date 2024/9/28 11:04
 * @description 登录业务核心类
 */
@Service
@AllArgsConstructor
public class LoginUserService implements UserDetailsService {
    private final SysUserFeignClient sysUserFeignClient;
    private final SysUserRoleFeignClient sysUserRoleFeignClient;
    private final SysRoleFeignClient sysRoleFeignClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 通过用户名查找用户表信息
        SysUserDTO sysUserDTO = Optional.ofNullable(sysUserFeignClient.selectByUsername(username).getData())
                .orElseThrow(() -> new SystemException(ResultEnum.USERNAME_OR_PASSWORD_ERROR));
        // 通过用户ID查找角色信息
        List<Long> roleIds = Optional.ofNullable(sysUserRoleFeignClient.selectRolesByUserId(sysUserDTO.getUserId()).getData())
                .orElseThrow(() -> new SystemException(ResultEnum.USERNAME_OR_PASSWORD_ERROR));
        // 通过角色ID集合查找用户所属的角色字符串
        Set<String> roleKeys = Optional.ofNullable(sysRoleFeignClient.selectRoleKeys(roleIds).getData())
                .orElseThrow(() -> new SystemException(ResultEnum.USERNAME_OR_PASSWORD_ERROR));
        // 更新 sysUserDTO
        sysUserDTO.setRoles(roleKeys);
        // 创建并返回用户登录信息对象
        return new LoginUser(sysUserDTO);
    }
}
