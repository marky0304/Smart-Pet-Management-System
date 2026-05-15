# 数据库文件说明

## 目录结构

```
database/
├── smart_pet_system.sql   # 完整建库脚本（表结构 + 初始数据）
└── optimize_database.sql  # 性能优化（索引等）
```

## 快速初始化

```bash
# 一键建库（包含表结构和基础数据）
mysql -u root -p123456789 < database/smart_pet_system.sql
```

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | zhangsan / lisi / wangwu | 123456 |

## 注意事项

- 数据库连接密码默认为 `123456789`，可在 `backend/src/main/resources/application.yml` 中修改
- 所有 SQL 文件使用 UTF-8 编码
- 密码使用 Hutool BCrypt 加密，不要手动替换哈希值