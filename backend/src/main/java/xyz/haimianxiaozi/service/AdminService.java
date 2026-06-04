package xyz.haimianxiaozi.service;

import xyz.haimianxiaozi.entity.User;

public interface AdminService {

    boolean isModerator(User user);

    boolean isAdmin(User user);

    User requireModerator();

    User requireAdmin();

    void audit(User operator, String action, String targetType, Long targetId, String detail);
}
