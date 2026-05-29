package xyz.haimianxiaozi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点赞记录实体
 */
@Data
@TableName("`like`")
public class Like {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 目标ID（帖子ID或评论ID） */
    private Long targetId;

    /** 目标类型：POST/COMMENT */
    private String targetType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}