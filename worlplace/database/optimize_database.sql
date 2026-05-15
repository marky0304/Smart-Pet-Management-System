-- 智慧宠物管理系统 - 数据库优化脚本
USE smart_pet_system;

-- ============================================
-- 1. 添加复合索引优化查询性能
-- ============================================

-- 用户表：按角色和状态查询
CREATE INDEX IF NOT EXISTS idx_role_status ON user(role, status);

-- 宠物表：按用户和类型查询
CREATE INDEX IF NOT EXISTS idx_user_type ON pet(user_id, type, status);

-- 健康记录：按宠物和日期查询
CREATE INDEX IF NOT EXISTS idx_pet_date ON health_record(pet_id, record_date DESC);

-- 健康记录：按记录类型查询
CREATE INDEX IF NOT EXISTS idx_record_type ON health_record(record_type, record_date DESC);

-- 服务表：按分类和状态查询
CREATE INDEX IF NOT EXISTS idx_category_status ON service(category, status);

-- 预约表：按用户和状态查询
CREATE INDEX IF NOT EXISTS idx_user_status_appt ON appointment(user_id, status, appointment_date DESC);

-- 预约表：按服务和状态查询
CREATE INDEX IF NOT EXISTS idx_service_status ON appointment(service_id, status);

-- 预约表：按预约日期查询
CREATE INDEX IF NOT EXISTS idx_appointment_date_status ON appointment(appointment_date, status);

-- 商品表：按分类和状态查询
CREATE INDEX IF NOT EXISTS idx_category_status_prod ON product(category, status);

-- 商品表：按销量排序
CREATE INDEX IF NOT EXISTS idx_sales ON product(sales DESC, status);

-- 订单表：按用户和状态查询
CREATE INDEX IF NOT EXISTS idx_user_status_order ON `order`(user_id, status, create_time DESC);

-- 订单表：按订单号查询
CREATE INDEX IF NOT EXISTS idx_order_no ON `order`(order_no);

-- 订单详情：按订单查询
CREATE INDEX IF NOT EXISTS idx_order_id ON order_item(order_id);

-- 动态表：按创建时间和状态查询（热门动态）
CREATE INDEX IF NOT EXISTS idx_create_status ON post(create_time DESC, status);

-- 动态表：按用户查询
CREATE INDEX IF NOT EXISTS idx_user_status_post ON post(user_id, status, create_time DESC);

-- 动态表：按话题查询
CREATE INDEX IF NOT EXISTS idx_topic ON post(topic, status, create_time DESC);

-- 评论表：按动态查询
CREATE INDEX IF NOT EXISTS idx_post_status ON comment(post_id, status, create_time DESC);

-- 评论表：按父评论查询
CREATE INDEX IF NOT EXISTS idx_parent ON comment(parent_id, status);

-- 点赞表：按目标类型和ID查询
CREATE INDEX IF NOT EXISTS idx_target ON like_record(target_type, target_id);

-- ============================================
-- 2. 分析表以更新统计信息
-- ============================================

ANALYZE TABLE user;
ANALYZE TABLE pet;
ANALYZE TABLE health_record;
ANALYZE TABLE service;
ANALYZE TABLE appointment;
ANALYZE TABLE product;
ANALYZE TABLE `order`;
ANALYZE TABLE order_item;
ANALYZE TABLE post;
ANALYZE TABLE comment;
ANALYZE TABLE follow;
ANALYZE TABLE like_record;

-- ============================================
-- 3. 优化表（整理碎片）
-- ============================================

OPTIMIZE TABLE user;
OPTIMIZE TABLE pet;
OPTIMIZE TABLE health_record;
OPTIMIZE TABLE service;
OPTIMIZE TABLE appointment;
OPTIMIZE TABLE product;
OPTIMIZE TABLE `order`;
OPTIMIZE TABLE order_item;
OPTIMIZE TABLE post;
OPTIMIZE TABLE comment;
OPTIMIZE TABLE follow;
OPTIMIZE TABLE like_record;

-- ============================================
-- 4. 查看索引使用情况
-- ============================================

SELECT 
    TABLE_NAME AS '表名',
    INDEX_NAME AS '索引名',
    COLUMN_NAME AS '列名',
    SEQ_IN_INDEX AS '列序号',
    CARDINALITY AS '基数',
    INDEX_TYPE AS '索引类型'
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'smart_pet_system'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- ============================================
-- 5. 查看表大小和行数
-- ============================================

SELECT 
    TABLE_NAME AS '表名',
    TABLE_ROWS AS '行数',
    ROUND(DATA_LENGTH / 1024 / 1024, 2) AS '数据大小(MB)',
    ROUND(INDEX_LENGTH / 1024 / 1024, 2) AS '索引大小(MB)',
    ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) AS '总大小(MB)'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'smart_pet_system'
ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC;

SELECT '数据库优化完成！' AS message;
