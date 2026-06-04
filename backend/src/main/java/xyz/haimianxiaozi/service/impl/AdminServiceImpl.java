package xyz.haimianxiaozi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.haimianxiaozi.entity.AuditLog;
import xyz.haimianxiaozi.entity.User;
import xyz.haimianxiaozi.mapper.AuditLogMapper;
import xyz.haimianxiaozi.service.AdminService;
import xyz.haimianxiaozi.util.UserContext;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MODERATOR = "MODERATOR";

    private final UserContext userContext;
    private final AuditLogMapper auditLogMapper;

    @Override
    public boolean isModerator(User user) {
        return user != null && (ROLE_ADMIN.equals(user.getRole()) || ROLE_MODERATOR.equals(user.getRole()));
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && ROLE_ADMIN.equals(user.getRole());
    }

    @Override
    public User requireModerator() {
        User user = userContext.getCurrentUser();
        if (!isModerator(user)) {
            throw new IllegalStateException("无管理权限");
        }
        return user;
    }

    @Override
    public User requireAdmin() {
        User user = userContext.getCurrentUser();
        if (!isAdmin(user)) {
            throw new IllegalStateException("无管理员权限");
        }
        return user;
    }

    @Override
    public void audit(User operator, String action, String targetType, Long targetId, String detail) {
        AuditLog log = new AuditLog();
        log.setOperatorId(operator == null ? null : operator.getId());
        log.setOperatorName(operator == null ? null : operator.getUsername());
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        log.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(log);
    }
}
