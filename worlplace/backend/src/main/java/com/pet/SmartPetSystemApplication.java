package com.pet;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智慧宠物管理系统启动类
 */
@SpringBootApplication
@EnableScheduling
@MapperScan({"com.pet.mapper", "com.pet.agent.mapper"})
public class SmartPetSystemApplication {

    private static final Logger log = LoggerFactory.getLogger(SmartPetSystemApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SmartPetSystemApplication.class, args);
        log.info("========================================");
        log.info("智慧宠物管理系统启动成功！");
        log.info("访问地址：http://localhost:8080/api");
        log.info("========================================");
    }
}
