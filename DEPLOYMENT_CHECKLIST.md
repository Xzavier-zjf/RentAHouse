# RentAHouse 部署检查清单

## 1. 必需环境变量

### 通用安全配置
- `SPRING_PROFILES_ACTIVE=prod`
- `RENTAL_JWT_SECRET`（强随机密钥，建议至少 32 位）
- `RENTAL_JWT_EXPIRE_SECONDS`（例如 `7200`）

### 数据服务
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `MONGODB_URI`（`rental-user` 与 `rental-house` 需要）

### 消息队列
- `RABBITMQ_HOST`
- `RABBITMQ_PORT`
- `RABBITMQ_USERNAME`
- `RABBITMQ_PASSWORD`

### 网关路由
- `RENTAL_USER_URL`
- `RENTAL_HOUSE_URL`
- `RENTAL_ORDER_URL`
- `RENTAL_COMMENT_URL`
- `RENTAL_MESSAGE_URL`
- `RENTAL_CORS_ALLOWED_ORIGINS`（逗号分隔的精确来源）

## 2. 服务启动顺序

1. MySQL / Redis / MongoDB / RabbitMQ
2. `rental-user`
3. `rental-house`
4. `rental-comment`
5. `rental-message`
6. `rental-order`
7. `rental-gateway`

## 3. 构建与验证

1. 执行测试：
   - `.\mvnw.cmd test`
2. 生成构建产物：
   - `.\mvnw.cmd clean package -DskipTests`
3. 以 `prod` 配置启动各服务并检查健康状态和日志。

## 4. 上线前安全核验

- 确认未使用默认弱密钥。
- 确认 CORS 仅允许受信任的前端来源。
- 确认后台接口端到端需要管理员权限。
- 确认文件上传会拒绝非图片和超限文件。
- 确认日志中不输出令牌值和密码等敏感信息。

## 5. 冒烟测试场景

- 用户注册、登录、资料更新
- 房东发布房源并更新状态
- 用户浏览、收藏并创建订单
- 用户支付订单，房东更新订单状态
- 管理端用户/房源/评论/订单/消息管理
