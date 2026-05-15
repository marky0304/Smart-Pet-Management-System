-- 智能客服系统 - 数据库新增表
-- 使用方法: mysql -u root -p < database/agent_system.sql

USE smart_pet_system;

-- 1. 对话消息表
CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID',
  `role` VARCHAR(20) NOT NULL COMMENT '角色：USER/ASSISTANT/SYSTEM',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `operation` VARCHAR(20) COMMENT '操作类型：QUERY/ADD/MODIFY/DELETE/CHAT',
  `domain` VARCHAR(20) COMMENT '业务领域：PET/HEALTH/MALL/GENERAL',
  `agent_type` VARCHAR(30) COMMENT '处理的Agent类型',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_user_session (`user_id`, `session_id`),
  INDEX idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能客服对话消息';

-- 2. 提醒表
CREATE TABLE IF NOT EXISTS `reminder` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '提醒ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `reminder_type` VARCHAR(30) NOT NULL COMMENT '提醒类型：VACCINE/CHECKUP/DEWORMING/MEDICINE/CUSTOM',
  `title` VARCHAR(200) NOT NULL COMMENT '提醒标题',
  `description` TEXT COMMENT '提醒详情',
  `remind_date` DATE NOT NULL COMMENT '提醒日期',
  `is_sent` TINYINT DEFAULT 0 COMMENT '是否已推送：0-未推送，1-已推送',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_user_remind (`user_id`, `remind_date`),
  INDEX idx_pet_id (`pet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物提醒表';
