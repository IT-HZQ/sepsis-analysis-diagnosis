package com.hzq.system.server.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author gc
 * @class com.hzq.api.entity.system SysUserRole
 * @date 2024/8/30 15:40
 * @description 系统用户角色映射实体类
 */
@Data
@ToString
@Entity
@Table(name = "sys_user_role")
public class SysUserRole {

    @EmbeddedId
    private SysUserRoleId id;

    public SysUserRole() {}

    public SysUserRole(Long userId, Long roleId) {
        this.id = new SysUserRoleId(userId, roleId);
    }

    @Embeddable
    @Data
    public static class SysUserRoleId implements Serializable {
        private Long userId;
        private Long roleId;

        public SysUserRoleId() {}

        public SysUserRoleId(Long userId, Long roleId) {
            this.userId = userId;
            this.roleId = roleId;
        }
    }
}