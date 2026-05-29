package xyz.haimianxiaozi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知实体
 */
@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收用户ID */
    private Long userId;

    /** 通知类型：LIKE, COMMENT, SYSTEM */
    private String type;

    /** 通知内容 */
    private String content;

    /** 相关目标ID（帖子ID或评论ID） */
    private Long targetId;

    /** 是否已读 */
    private Boolean isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}