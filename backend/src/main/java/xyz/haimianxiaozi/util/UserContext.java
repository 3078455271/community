package xyz.haimianxiaozi.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.enums.RoleEnum;
import xyz.haimianxiaozi.security.AuthUser;
import xyz.haimianxiaozi.service.UserService;

/**
 * 当前登录用户上下文，从 Spring Security 的 SecurityContext 读取身份，
 * 由 {@code JwtAuthenticationFilter} 在请求进入时填充。
 *
 * @author haimianxiaozi
 */
@Component
@RequiredArgsConstructor
public class UserContext {

    private final UserService userService;

    private AuthUser currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthUser authUser) {
            return authUser;
        }
        return null;
    }

    public Long getCurrentUserId() {
        AuthUser principal = currentPrincipal();
        return principal == null ? null : principal.userId();
    }

    public RoleEnum getCurrentRole() {
        AuthUser principal = currentPrincipal();
        return principal == null ? null : principal.role();
    }

    /**
     * 是否具备管理权限（管理员或版主），用于内容管理类操作的越权放行判断。
     */
    public boolean isAdminOrModerator() {
        RoleEnum role = getCurrentRole();
        return role == RoleEnum.ADMIN || role == RoleEnum.MODERATOR;
    }

    public User getCurrentUser() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return null;
        }
        return userService.getById(userId);
    }
}
