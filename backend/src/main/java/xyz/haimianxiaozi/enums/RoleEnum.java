package xyz.haimianxiaozi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 * 用于 RBAC 权限控制，存储于 user.role 字段
 *
 * @author haimianxiaozi
 */
@Getter
@AllArgsConstructor
public enum RoleEnum {

    USER("USER", "普通用户"),
    MODERATOR("MODERATOR", "版主"),
    ADMIN("ADMIN", "管理员");

    private final String code;
    private final String desc;

    /**
     * Spring Security 角色名前缀约定
     */
    public String authority() {
        return "ROLE_" + code;
    }

    /**
     * 根据 code 解析角色，非法值回退为 USER
     */
    public static RoleEnum fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return USER;
        }
        for (RoleEnum role : values()) {
            if (role.code.equalsIgnoreCase(code)) {
                return role;
            }
        }
        return USER;
    }
}
