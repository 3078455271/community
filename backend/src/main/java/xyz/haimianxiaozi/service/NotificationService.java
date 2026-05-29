package xyz.haimianxiaozi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import xyz.haimianxiaozi.entity.Notification;

public interface NotificationService extends IService<Notification> {

    /**
     * 获取用户通知列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 通知分页
     */
    Page<Notification> getUserNotifications(Long userId, int page, int size);

    /**
     * 获取用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    long getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     *
     * @param id     通知ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAsRead(Long id, Long userId);

    /**
     * 标记所有通知为已读
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAllAsRead(Long userId);

    /**
     * 发送通知
     *
     * @param userId   接收用户ID
     * @param type     通知类型
     * @param content  通知内容
     * @param targetId 相关目标ID
     */
    void sendNotification(Long userId, String type, String content, Long targetId);
}