-- 运营管理功能升级脚本
ALTER TABLE `user`
    ADD COLUMN IF NOT EXISTS `role` VARCHAR(20) DEFAULT 'USER' COMMENT 'USER/MODERATOR/ADMIN' AFTER `status`,
    ADD COLUMN IF NOT EXISTS `muted_until` DATETIME DEFAULT NULL COMMENT '禁言截止时间' AFTER `role`;

UPDATE `user` SET `role` = 'USER' WHERE `role` IS NULL OR `role` = '';

CREATE TABLE IF NOT EXISTS `report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `reporter_id` BIGINT NOT NULL,
    `target_type` VARCHAR(20) NOT NULL COMMENT 'POST/COMMENT/USER',
    `target_id` BIGINT NOT NULL,
    `reason` VARCHAR(500) NOT NULL,
    `status` TINYINT DEFAULT 0 COMMENT '0-待处理 1-已通过 2-已驳回',
    `handled_by` BIGINT DEFAULT NULL,
    `handle_remark` VARCHAR(500) DEFAULT NULL,
    `handled_at` DATETIME DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sensitive_word` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `word` VARCHAR(100) NOT NULL,
    `enabled` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `audit_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `operator_id` BIGINT DEFAULT NULL,
    `operator_name` VARCHAR(50) DEFAULT NULL,
    `action` VARCHAR(50) NOT NULL,
    `target_type` VARCHAR(30) DEFAULT NULL,
    `target_id` BIGINT DEFAULT NULL,
    `detail` VARCHAR(1000) DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_operator_id` (`operator_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 首次启用后台时，请把一个可信账号提升为管理员，例如：
-- UPDATE `user` SET `role` = 'ADMIN' WHERE `username` = 'admin';
