-- 手动执行：为已有 post 表补充发布设置字段
USE community;

ALTER TABLE `post`
    ADD COLUMN `comment_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否开启评论' AFTER `essence`,
    ADD COLUMN `visibility` VARCHAR(20) NOT NULL DEFAULT 'PUBLIC' COMMENT 'PUBLIC-公开 FOLLOWERS-仅粉丝可见' AFTER `comment_enabled`,
    ADD INDEX `idx_visibility` (`visibility`, `user_id`);
