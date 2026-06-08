-- Backfill post publishing settings for databases created before Flyway adoption.

SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'post'
      AND COLUMN_NAME = 'comment_enabled'
);
SET @ddl := IF(
    @column_exists = 0,
    'ALTER TABLE `post` ADD COLUMN `comment_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT ''是否开启评论'' AFTER `essence`',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'post'
      AND COLUMN_NAME = 'visibility'
);
SET @ddl := IF(
    @column_exists = 0,
    'ALTER TABLE `post` ADD COLUMN `visibility` VARCHAR(20) NOT NULL DEFAULT ''PUBLIC'' COMMENT ''PUBLIC-公开 FOLLOWERS-仅粉丝可见'' AFTER `comment_enabled`',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'post'
      AND INDEX_NAME = 'idx_visibility'
);
SET @ddl := IF(
    @index_exists = 0,
    'ALTER TABLE `post` ADD INDEX `idx_visibility` (`visibility`, `user_id`)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
