package xyz.haimianxiaozi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.haimianxiaozi.entity.Notification;
import xyz.haimianxiaozi.mapper.NotificationMapper;
import xyz.haimianxiaozi.service.NotificationService;
import xyz.haimianxiaozi.websocket.WebSocketEventPublisher;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    private final WebSocketEventPublisher eventPublisher;

    @Override
    public Page<Notification> getUserNotifications(Long userId, int page, int size) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false);
        return count(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long id, Long userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getId, id)
                .eq(Notification::getUserId, userId)
                .set(Notification::getIsRead, true);
        boolean success = update(wrapper);
        if (success) {
            eventPublisher.publish(userId, "notification.read", id);
            eventPublisher.publishUnreadCount(userId, "notification.unreadCount", getUnreadCount(userId));
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false)
                .set(Notification::getIsRead, true);
        boolean success = update(wrapper);
        if (success) {
            eventPublisher.publish(userId, "notification.readAll", true);
            eventPublisher.publishUnreadCount(userId, "notification.unreadCount", 0L);
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendNotification(Long userId, String type, String content, Long targetId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setContent(content);
        notification.setTargetId(targetId);
        notification.setIsRead(false);
        save(notification);
        eventPublisher.publish(userId, "notification.created", notification);
        eventPublisher.publishUnreadCount(userId, "notification.unreadCount", getUnreadCount(userId));
    }
}