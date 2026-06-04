-- 内容生态功能升级脚本
ALTER TABLE `post`
    ADD COLUMN IF NOT EXISTS `essence` TINYINT(1) DEFAULT 0 COMMENT '是否精华' AFTER `status`;

CREATE TABLE IF NOT EXISTS `post_view_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `post_id` BIGINT NOT NULL,
    `viewed_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
    INDEX `idx_user_viewed_at` (`user_id`, `viewed_at`),
    INDEX `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
