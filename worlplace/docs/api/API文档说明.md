# 智慧宠物管理系统 - API文档

## 文档信息

**版本**: v1.0.0  
**基础URL**: `http://localhost:8080/api`  
**认证方式**: JWT Token  
**更新日期**: 2025-12-09

---

## 认证说明

### 获取Token
所有需要认证的接口都需要在请求头中携带Token：

```http
Authorization: Bearer {token}
```

### 登录获取Token
```http
POST /api/user/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "nickname": "系统管理员",
      "role": "ADMIN"
    }
  }
}
```

---

## 通用响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

### 失败响应
```json
{
  "code": 400,
  "message": "错误信息",
  "data": null
}
```

### 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

---

## 一、用户模块

### 1.1 用户注册
```http
POST /api/user/register
Content-Type: application/json
```

**请求参数**:
```json
{
  "username": "zhangsan",
  "password": "123456",
  "nickname": "张三",
  "phone": "13800138001",
  "email": "zhangsan@example.com"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 2,
    "username": "zhangsan",
    "nickname": "张三"
  }
}
```

### 1.2 用户登录
```http
POST /api/user/login
Content-Type: application/json
```

**请求参数**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

### 1.3 获取当前用户信息
```http
GET /api/user/info
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "系统管理员",
    "avatar": "/images/avatar.jpg",
    "phone": "13800138000",
    "email": "admin@example.com",
    "role": "ADMIN"
  }
}
```

### 1.4 更新用户信息
```http
PUT /api/user/update
Authorization: Bearer {token}
Content-Type: application/json
```

**请求参数**:
```json
{
  "nickname": "新昵称",
  "avatar": "/images/new-avatar.jpg",
  "phone": "13800138001",
  "email": "newemail@example.com"
}
```

### 1.5 修改密码
```http
PUT /api/user/password
Authorization: Bearer {token}
Content-Type: application/json
```

**请求参数**:
```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

### 1.6 用户列表（管理员）
```http
GET /api/user/list?pageNum=1&pageSize=10&keyword=张三&role=USER&status=1
Authorization: Bearer {token}
```

**查询参数**:
- `pageNum`: 页码（默认1）
- `pageSize`: 每页数量（默认10）
- `keyword`: 搜索关键词（可选）
- `role`: 角色筛选（可选）
- `status`: 状态筛选（可选）

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "records": [
      {
        "id": 2,
        "username": "zhangsan",
        "nickname": "张三",
        "phone": "13800138001",
        "email": "zhangsan@example.com",
        "role": "USER",
        "status": 1,
        "createTime": "2024-12-09 10:00:00"
      }
    ],
    "total": 100,
    "current": 1,
    "size": 10
  }
}
```

---

## 二、宠物模块

### 2.1 添加宠物
```http
POST /api/pet/add
Authorization: Bearer {token}
Content-Type: application/json
```

**请求参数**:
```json
{
  "name": "旺财",
  "type": "DOG",
  "breed": "金毛",
  "gender": 1,
  "birthDate": "2020-05-15",
  "color": "金黄色",
  "weight": 28.5,
  "chipNumber": "CHIP001",
  "allergy": "无",
  "specialNotes": "性格温顺"
}
```

### 2.2 我的宠物列表
```http
GET /api/pet/my
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "name": "旺财",
      "type": "DOG",
      "typeName": "狗",
      "breed": "金毛",
      "gender": 1,
      "genderName": "公",
      "birthDate": "2020-05-15",
      "age": 4,
      "color": "金黄色",
      "weight": 28.5,
      "photo": "/images/pet1.jpg",
      "chipNumber": "CHIP001"
    }
  ]
}
```

### 2.3 宠物详情
```http
GET /api/pet/{id}
Authorization: Bearer {token}
```

### 2.4 更新宠物信息
```http
PUT /api/pet/update
Authorization: Bearer {token}
Content-Type: application/json
```

### 2.5 删除宠物
```http
DELETE /api/pet/{id}
Authorization: Bearer {token}
```

---

## 三、健康管理模块

### 3.1 添加健康记录
```http
POST /api/health/add
Authorization: Bearer {token}
Content-Type: application/json
```

**请求参数**:
```json
{
  "petId": 1,
  "recordType": "CHECKUP",
  "recordDate": "2024-12-09 10:00:00",
  "weight": 28.5,
  "temperature": 38.5,
  "symptom": "定期体检",
  "diagnosis": "健康状况良好",
  "treatment": "无需特殊治疗",
  "hospital": "爱宠动物医院",
  "doctor": "李医生",
  "cost": 200.00,
  "notes": "建议定期复查"
}
```

### 3.2 健康记录列表
```http
GET /api/health/list?petId=1&recordType=CHECKUP&pageNum=1&pageSize=10
Authorization: Bearer {token}
```

### 3.3 健康记录详情
```http
GET /api/health/{id}
Authorization: Bearer {token}
```

### 3.4 健康趋势数据
```http
GET /api/health/trend?petId=1&startDate=2024-01-01&endDate=2024-12-31
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "weightTrend": [
      {"date": "2024-01-01", "value": 27.5},
      {"date": "2024-06-01", "value": 28.0},
      {"date": "2024-12-01", "value": 28.5}
    ],
    "temperatureTrend": [
      {"date": "2024-01-01", "value": 38.3},
      {"date": "2024-06-01", "value": 38.5},
      {"date": "2024-12-01", "value": 38.4}
    ]
  }
}
```

---

## 四、服务预约模块

### 4.1 服务列表
```http
GET /api/service/list?category=BATH&status=1&pageNum=1&pageSize=10
```

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "基础洗澡",
        "category": "BATH",
        "categoryName": "洗澡",
        "description": "包含基础清洁、吹干、梳毛服务",
        "price": 80.00,
        "duration": 60,
        "image": "/images/bath1.jpg",
        "status": 1,
        "statusName": "已上架"
      }
    ],
    "total": 10,
    "current": 1,
    "size": 10
  }
}
```

### 4.2 创建预约
```http
POST /api/appointment/create
Authorization: Bearer {token}
Content-Type: application/json
```

**请求参数**:
```json
{
  "petId": 1,
  "serviceId": 1,
  "appointmentDate": "2024-12-15 10:00:00",
  "notes": "请使用温和的洗浴产品"
}
```

### 4.3 我的预约列表
```http
GET /api/appointment/my?status=PENDING&pageNum=1&pageSize=10
Authorization: Bearer {token}
```

### 4.4 取消预约
```http
PUT /api/appointment/cancel/{id}
Authorization: Bearer {token}
```

### 4.5 评价服务
```http
POST /api/appointment/review
Authorization: Bearer {token}
Content-Type: application/json
```

**请求参数**:
```json
{
  "appointmentId": 1,
  "rating": 5,
  "review": "服务很好，狗狗很喜欢"
}
```

---

## 五、商城模块（待开发）

### 5.1 商品列表
```http
GET /api/product/list?category=FOOD&keyword=狗粮&pageNum=1&pageSize=10
```

### 5.2 商品详情
```http
GET /api/product/{id}
```

### 5.3 创建订单
```http
POST /api/order/create
Authorization: Bearer {token}
Content-Type: application/json
```

**请求参数**:
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ],
  "shippingAddress": "北京市朝阳区xx街道xx号",
  "shippingPhone": "13800138001",
  "shippingName": "张三",
  "notes": "请尽快发货"
}
```

### 5.4 我的订单列表
```http
GET /api/order/my?status=PAID&pageNum=1&pageSize=10
Authorization: Bearer {token}
```

---

## 六、社区模块（待开发）

### 6.1 发布动态
```http
POST /api/post/create
Authorization: Bearer {token}
Content-Type: application/json
```

**请求参数**:
```json
{
  "content": "今天带旺财去公园玩，它超级开心！",
  "images": ["/images/post1.jpg", "/images/post2.jpg"],
  "topic": "遛狗日常"
}
```

### 6.2 动态列表
```http
GET /api/post/list?topic=遛狗日常&pageNum=1&pageSize=10
```

### 6.3 点赞动态
```http
POST /api/post/like/{postId}
Authorization: Bearer {token}
```

### 6.4 评论动态
```http
POST /api/comment/create
Authorization: Bearer {token}
Content-Type: application/json
```

**请求参数**:
```json
{
  "postId": 1,
  "parentId": 0,
  "content": "好可爱的狗狗！"
}
```

---

## 测试工具

### Postman Collection
可以导入以下Postman Collection进行测试：
- [下载Postman Collection](链接)

### cURL示例

#### 登录
```bash
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

#### 获取用户信息
```bash
curl -X GET http://localhost:8080/api/user/info \
  -H "Authorization: Bearer {token}"
```

#### 添加宠物
```bash
curl -X POST http://localhost:8080/api/pet/add \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"name":"旺财","type":"DOG","breed":"金毛"}'
```

---

## 附录

### 数据字典

#### 宠物类型（type）
- `DOG`: 狗
- `CAT`: 猫
- `BIRD`: 鸟
- `OTHER`: 其他

#### 性别（gender）
- `1`: 公
- `2`: 母

#### 健康记录类型（recordType）
- `CHECKUP`: 体检
- `VACCINE`: 疫苗
- `ILLNESS`: 疾病
- `WEIGHT`: 体重

#### 服务分类（category）
- `BATH`: 洗澡
- `GROOM`: 美容
- `FOSTER`: 寄养
- `MEDICAL`: 医疗

#### 预约状态（status）
- `PENDING`: 待确认
- `CONFIRMED`: 已确认
- `COMPLETED`: 已完成
- `CANCELLED`: 已取消

#### 订单状态（status）
- `PENDING`: 待支付
- `PAID`: 已支付
- `SHIPPED`: 已发货
- `COMPLETED`: 已完成
- `CANCELLED`: 已取消

---

**文档维护**: 开发团队  
**最后更新**: 2025-12-09
