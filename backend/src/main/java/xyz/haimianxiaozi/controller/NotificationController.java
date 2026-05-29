package xyz.haimianxiaozi.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.haimianxiaozi.common.R;
import xyz.haimianxiaozi.entity.Notification;
import xyz.haimianxiaozi.service.NotificationService;
import xyz.haimianxiaozi.util.UserContext;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserContext userContext;

    @GetMapping
    public R<Page<Notification>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        return R.ok(notificationService.getUserNotifications(userId, page, size));
    }

    @GetMapping("/unread-count")
    public R<Long> unreadCount() {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.ok(0L);
        }
        return R.ok(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public R<String> markAsRead(@PathVariable Long id) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        boolean success = notificationService.markAsRead(id, userId);
        return success ? R.ok("已读") : R.fail("操作失败");
    }

    @PutMapping("/read-all")
    public R<String> markAllAsRead() {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }
        boolean success = notificationService.markAllAsRead(userId);
        return success ? R.ok("全部已读") : R.fail("操作失败");
    }
}