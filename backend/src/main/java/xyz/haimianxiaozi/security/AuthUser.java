package xyz.haimianxiaozi.security;

import xyz.haimianxiaozi.enums.RoleEnum;

/**
 * 认证主体，放入 SecurityContext 作为 principal。
 * 仅承载从 JWT 解析出的轻量身份信息，避免每次请求查库。
 *
 * @author haimianxiaozi
 */
public record AuthUser(Long userId, String username, RoleEnum role) {
}
