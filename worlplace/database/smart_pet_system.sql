-- 智慧宠物管理系统数据库设计
-- 创建数据库
CREATE DATABASE IF NOT EXISTS smart_pet_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE smart_pet_system;

-- 1. 用户表
CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `avatar` VARCHAR(255) COMMENT '头像URL',
  `phone` VARCHAR(20) UNIQUE COMMENT '手机号',
  `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
  `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色：USER-普通用户，ADMIN-管理员',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_username (`username`),
  INDEX idx_phone (`phone`),
  INDEX idx_role (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 宠物表
CREATE TABLE `pet` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '宠物ID',
  `user_id` BIGINT NOT NULL COMMENT '主人ID',
  `name` VARCHAR(50) NOT NULL COMMENT '宠物名称',
  `type` VARCHAR(20) NOT NULL COMMENT '宠物类型：DOG-狗，CAT-猫，BIRD-鸟，OTHER-其他',
  `breed` VARCHAR(50) COMMENT '品种',
  `gender` TINYINT COMMENT '性别：1-公，2-母',
  `birth_date` DATE COMMENT '出生日期',
  `color` VARCHAR(50) COMMENT '毛色',
  `weight` DECIMAL(5,2) COMMENT '体重（kg）',
  `photo` VARCHAR(255) COMMENT '照片URL',
  `chip_number` VARCHAR(50) COMMENT '芯片编号',
  `allergy` TEXT COMMENT '过敏史',
  `special_notes` TEXT COMMENT '特殊说明',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_user_id (`user_id`),
  INDEX idx_type (`type`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物表';

-- 3. 健康记录表
CREATE TABLE `health_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `record_type` VARCHAR(20) NOT NULL COMMENT '记录类型：CHECKUP-体检，VACCINE-疫苗，ILLNESS-疾病，WEIGHT-体重',
  `record_date` DATETIME NOT NULL COMMENT '记录日期',
  `weight` DECIMAL(5,2) COMMENT '体重（kg）',
  `temperature` DECIMAL(4,2) COMMENT '体温（℃）',
  `symptom` TEXT COMMENT '症状描述',
  `diagnosis` TEXT COMMENT '诊断结果',
  `treatment` TEXT COMMENT '治疗方案',
  `hospital` VARCHAR(100) COMMENT '医院名称',
  `doctor` VARCHAR(50) COMMENT '医生姓名',
  `medicine` TEXT COMMENT '用药记录',
  `cost` DECIMAL(10,2) COMMENT '费用',
  `next_visit_date` DATE COMMENT '下次复诊日期',
  `notes` TEXT COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_pet_id (`pet_id`),
  INDEX idx_record_date (`record_date`),
  FOREIGN KEY (`pet_id`) REFERENCES `pet`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康记录表';

-- 4. 服务项目表
CREATE TABLE `service` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '服务ID',
  `name` VARCHAR(100) NOT NULL COMMENT '服务名称',
  `category` VARCHAR(20) NOT NULL COMMENT '服务分类：BATH-洗澡，GROOM-美容，FOSTER-寄养，MEDICAL-医疗',
  `description` TEXT COMMENT '服务描述',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `duration` INT COMMENT '时长（分钟）',
  `image` VARCHAR(255) COMMENT '服务图片',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_category (`category`),
  INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务项目表';

-- 5. 预约订单表
CREATE TABLE `appointment` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预约ID',
  `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `service_id` BIGINT NOT NULL COMMENT '服务ID',
  `appointment_date` DATETIME NOT NULL COMMENT '预约时间',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待确认，CONFIRMED-已确认，COMPLETED-已完成，CANCELLED-已取消',
  `total_price` DECIMAL(10,2) NOT NULL COMMENT '总价',
  `notes` TEXT COMMENT '备注',
  `rating` TINYINT COMMENT '评分：1-5',
  `review` TEXT COMMENT '评价内容',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_user_id (`user_id`),
  INDEX idx_pet_id (`pet_id`),
  INDEX idx_service_id (`service_id`),
  INDEX idx_status (`status`),
  INDEX idx_appointment_date (`appointment_date`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`pet_id`) REFERENCES `pet`(`id`),
  FOREIGN KEY (`service_id`) REFERENCES `service`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约订单表';

-- 6. 商品表
CREATE TABLE `product` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
  `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `category` VARCHAR(20) NOT NULL COMMENT '分类：FOOD-食品，TOY-玩具，SUPPLY-用品，MEDICINE-药品',
  `description` TEXT COMMENT '商品描述',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `stock` INT DEFAULT 0 COMMENT '库存',
  `sales` INT DEFAULT 0 COMMENT '销量',
  `image` VARCHAR(255) COMMENT '商品图片',
  `images` TEXT COMMENT '商品图片集（JSON数组）',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_category (`category`),
  INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 7. 订单表
CREATE TABLE `order` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总额',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待支付，PAID-已支付，SHIPPED-已发货，COMPLETED-已完成，CANCELLED-已取消',
  `payment_method` VARCHAR(20) COMMENT '支付方式',
  `payment_time` DATETIME COMMENT '支付时间',
  `shipping_address` VARCHAR(255) COMMENT '收货地址',
  `shipping_phone` VARCHAR(20) COMMENT '收货电话',
  `shipping_name` VARCHAR(50) COMMENT '收货人',
  `tracking_number` VARCHAR(100) COMMENT '物流单号',
  `notes` TEXT COMMENT '订单备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_user_id (`user_id`),
  INDEX idx_status (`status`),
  INDEX idx_order_no (`order_no`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 8. 订单详情表
CREATE TABLE `order_item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单详情ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `product_image` VARCHAR(255) COMMENT '商品图片',
  `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
  `quantity` INT NOT NULL COMMENT '数量',
  `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_order_id (`order_id`),
  INDEX idx_product_id (`product_id`),
  FOREIGN KEY (`order_id`) REFERENCES `order`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`product_id`) REFERENCES `product`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单详情表';

-- 9. 动态表
CREATE TABLE `post` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '动态ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `content` TEXT NOT NULL COMMENT '动态内容',
  `images` TEXT COMMENT '图片URL集（JSON数组）',
  `topic` VARCHAR(100) COMMENT '话题标签',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT DEFAULT 0 COMMENT '评论数',
  `share_count` INT DEFAULT 0 COMMENT '转发数',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-已删除，1-正常，2-待审核',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_user_id (`user_id`),
  INDEX idx_status (`status`),
  INDEX idx_create_time (`create_time`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态表';

-- 10. 评论表
CREATE TABLE `comment` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
  `post_id` BIGINT NOT NULL COMMENT '动态ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID（0表示一级评论）',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_post_id (`post_id`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_parent_id (`parent_id`),
  FOREIGN KEY (`post_id`) REFERENCES `post`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 11. 关注表
CREATE TABLE `follow` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关注ID',
  `follower_id` BIGINT NOT NULL COMMENT '关注者ID',
  `following_id` BIGINT NOT NULL COMMENT '被关注者ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_follow (`follower_id`, `following_id`),
  INDEX idx_follower (`follower_id`),
  INDEX idx_following (`following_id`),
  FOREIGN KEY (`follower_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`following_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注表';

-- 12. 点赞表
CREATE TABLE `like_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '点赞ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `target_type` VARCHAR(20) NOT NULL COMMENT '目标类型：POST-动态，COMMENT-评论',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_like (`user_id`, `target_type`, `target_id`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_target (`target_type`, `target_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- 插入初始管理员账号（密码：admin123，Hutool BCrypt加密）
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`) 
VALUES ('admin', '$2a$10$fGH0bRqLz6q894trEMVmpeB0aOgnzvBZU5tJey6IHi/ad0Ir2Wct.', '系统管理员', 'ADMIN', 1);

-- 插入测试服务项目
INSERT INTO `service` (`name`, `category`, `description`, `price`, `duration`, `status`) VALUES
('基础洗澡', 'BATH', '包含洗澡、吹干、梳毛', 80.00, 60, 1),
('精致美容', 'GROOM', '包含洗澡、造型、修剪、染色', 200.00, 120, 1),
('短期寄养', 'FOSTER', '提供舒适的寄养环境，每日三餐', 100.00, 1440, 1),
('健康体检', 'MEDICAL', '全面健康检查，包含血液检测', 300.00, 90, 1);

-- 插入测试商品
INSERT INTO `product` (`name`, `category`, `description`, `price`, `stock`, `status`) VALUES
('皇家狗粮 10kg', 'FOOD', '适合成年犬的营养均衡狗粮', 299.00, 100, 1),
('猫咪逗猫棒', 'TOY', '互动玩具，增进感情', 29.90, 200, 1),
('宠物自动饮水机', 'SUPPLY', '循环过滤，保持水质新鲜', 159.00, 50, 1),
('宠物驱虫药', 'MEDICINE', '体内外驱虫，安全有效', 89.00, 80, 1);
