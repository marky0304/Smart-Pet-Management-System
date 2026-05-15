package com.pet;

import cn.hutool.crypto.digest.BCrypt;

public class PasswordTest {
    public static void main(String[] args) {
        String password = "admin123";
        String hash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi";
        
        System.out.println("测试密码: " + password);
        System.out.println("密码哈希: " + hash);
        System.out.println("验证结果: " + BCrypt.checkpw(password, hash));
        
        // 生成新的哈希
        String newHash = BCrypt.hashpw(password);
        System.out.println("新生成的哈希: " + newHash);
        System.out.println("新哈希验证: " + BCrypt.checkpw(password, newHash));
    }
}
