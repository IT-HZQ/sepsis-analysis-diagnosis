package com.hzq.system.controller;

import com.hzq.api.controller.system.SysRoleApi;
import com.hzq.api.pojo.system.SysRoleDTO;
import com.hzq.api.entity.system.SysRole;
import com.hzq.core.result.Result;
import com.hzq.system.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author gc
 * @class com.hzq.system.controller SysRoleController
 * @date 2024/8/30 16:50
 * @description TODO
 */
@RestController
public class SysRoleController implements SysRoleApi {
    private final SysRoleService sysRoleService;

    @Autowired
    public SysRoleController(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    @Override
    public Result<?> list(SysRoleDTO sysRoleDTO) {
        List<SysRole> list = sysRoleService.list(sysRoleDTO);
        return Result.success(list);
    }

    @Override
    public Result<List<String>> findRoleKeyByRoleIds(List<Long> list) {
        List<String> resList = sysRoleService.findRoleKeyByRoleIds(list);
        return Result.success(resList);
    }
}