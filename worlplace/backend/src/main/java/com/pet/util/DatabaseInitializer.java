package com.pet.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user", Integer.class);
            log.info("用户表已存在，跳过初始化");
        } catch (Exception e) {
            log.warn("用户表不存在，开始初始化数据库...");
            initializeDatabase();
        }
        ensureCartTable();
        ensureNotificationTable();
        ensureUserFollowTable();
        ensureProductReviewTable();
    }

    private void ensureCartTable() {
        try {
            String createCartTable = "CREATE TABLE IF NOT EXISTS cart_items (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "user_id BIGINT NOT NULL," +
                "product_id BIGINT NOT NULL," +
                "quantity INT NOT NULL DEFAULT 1," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "UNIQUE KEY uk_user_product (user_id, product_id)" +
                ")";
            jdbcTemplate.execute(createCartTable);
            log.info("购物车表就绪");
        } catch (Exception e) {
            log.error("购物车表创建失败: {}", e.getMessage());
        }
    }

    private void ensureProductReviewTable() {
        try {
            String createTable = "CREATE TABLE IF NOT EXISTS product_reviews (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "product_id BIGINT NOT NULL," +
                "order_id BIGINT," +
                "user_id BIGINT NOT NULL," +
                "username VARCHAR(50)," +
                "user_avatar VARCHAR(500)," +
                "rating INT NOT NULL," +
                "content VARCHAR(1000)," +
                "images VARCHAR(1000)," +
                "is_anonymous TINYINT DEFAULT 0," +
                "likes_count INT DEFAULT 0," +
                "status TINYINT DEFAULT 1," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")";
            jdbcTemplate.execute(createTable);
            log.info("商品评价表就绪");
        } catch (Exception e) {
            log.error("商品评价表创建失败: {}", e.getMessage());
        }
    }

    private void ensureUserFollowTable() {
        try {
            String createTable = "CREATE TABLE IF NOT EXISTS user_follow (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "follower_id BIGINT NOT NULL," +
                "followee_id BIGINT NOT NULL," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "UNIQUE KEY uk_follower_followee (follower_id, followee_id)" +
                ")";
            jdbcTemplate.execute(createTable);
            log.info("用户关注表就绪");
        } catch (Exception e) {
            log.error("用户关注表创建失败: {}", e.getMessage());
        }
    }

    private void ensureNotificationTable() {
        try {
            String createTable = "CREATE TABLE IF NOT EXISTS notifications (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "user_id BIGINT NOT NULL," +
                "from_user_id BIGINT NOT NULL," +
                "from_username VARCHAR(50)," +
                "type VARCHAR(20) NOT NULL," +
                "target_type VARCHAR(20)," +
                "target_id BIGINT," +
                "content VARCHAR(500)," +
                "is_read TINYINT DEFAULT 0," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
            jdbcTemplate.execute(createTable);
            log.info("通知表就绪");
        } catch (Exception e) {
            log.error("通知表创建失败: {}", e.getMessage());
        }
    }

    private void initializeDatabase() {
        try {
            String createUserTable = "CREATE TABLE IF NOT EXISTS user (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "username VARCHAR(50) NOT NULL UNIQUE," +
                "password VARCHAR(255) NOT NULL," +
                "nickname VARCHAR(50)," +
                "avatar VARCHAR(500)," +
                "phone VARCHAR(20)," +
                "email VARCHAR(100)," +
                "gender TINYINT DEFAULT 0," +
                "role VARCHAR(20) DEFAULT 'USER'," +
                "status TINYINT DEFAULT 1," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")";

            jdbcTemplate.execute(createUserTable);
            log.info("用户表创建成功");

            String insertAdmin = "INSERT IGNORE INTO user (username, password, nickname, role, status) " +
                "VALUES ('admin', '$2a$10$fGH0bRqLz6q894trEMVmpeB0aOgnzvBZU5tJey6IHi/ad0Ir2Wct.', '系统管理员', 'ADMIN', 1)";

            jdbcTemplate.execute(insertAdmin);
            log.info("管理员用户创建成功");

            String insertUser = "INSERT IGNORE INTO user (username, password, nickname, role, status) " +
                "VALUES ('zhangsan', '$2a$10$fGH0bRqLz6q894trEMVmpeB0aOgnzvBZU5tJey6IHi/ad0Ir2Wct.', '张三', 'USER', 1)";

            jdbcTemplate.execute(insertUser);
            log.info("测试用户创建成功");

            String createCartTable = "CREATE TABLE IF NOT EXISTS cart_items (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "user_id BIGINT NOT NULL," +
                "product_id BIGINT NOT NULL," +
                "quantity INT NOT NULL DEFAULT 1," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "UNIQUE KEY uk_user_product (user_id, product_id)" +
                ")";
            jdbcTemplate.execute(createCartTable);
            log.info("购物车表创建成功");

            Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user", Integer.class);
            log.info("数据库初始化完成，用户数量: {}", userCount);

        } catch (Exception e) {
            log.error("数据库初始化失败: {}", e.getMessage(), e);
        }
    }
}
