package com.hzq.system.api;

import com.hzq.core.result.Result;
import com.hzq.system.dto.SysUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author gc
 * @interface com.hzq.api.feign SysUserFeignClient
 * @date 2024/9/3 14:44
 * @description 用户远程调用服务
 */
@FeignClient(contextId = "sys-user", name = "sepsis-system", path = "/system/user")
public interface SysUserFeignClient {

    @GetMapping("/username/{username}")
    Result<SysUserDTO> selectByUsername(@PathVariable("username") String username);
}
