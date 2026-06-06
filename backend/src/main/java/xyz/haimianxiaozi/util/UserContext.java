package xyz.haimianxiaozi.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.security.LoginUser;
import xyz.haimianxiaozi.service.UserService;

@Component
public class UserContext {

    private final UserService userService;

    public UserContext(UserService userService) {
        this.userService = userService;
    }

    public Long getCurrentUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getUserId();
    }

    public User getCurrentUser() {
        LoginUser loginUser = getLoginUser();
        if (loginUser != null) {
            return loginUser.getUser();
        }
        Long userId = getCurrentUserId();
        if (userId == null) {
            return null;
        }
        return userService.getById(userId);
    }

    private LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            return null;
        }
        return loginUser;
    }
}