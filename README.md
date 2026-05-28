# RentAHouse

RentAHouse 是一个基于 Spring Boot + Vue 的多模块租房平台，包含用户与账号、房源管理、订单、评论、消息和网关等核心能力。

## 技术栈

- Java 17
- Spring Boot 3.2.x
- Spring Cloud Gateway
- Spring Security + JWT
- MyBatis-Plus
- MySQL
- Redis
- MongoDB（GridFS 文件存储）
- RabbitMQ（订单超时流程）
- Vue 3 + Vite（前端）

## 项目结构

- `rental-gateway`：API 网关与统一鉴权过滤
- `rental-common`：公共工具、异常、认证辅助
- `rental-user`：用户注册登录、资料与后台用户管理
- `rental-house`：房源发布、查询与收藏
- `rental-order`：订单生命周期与支付模拟
- `rental-comment`：房源评论
- `rental-message`：聊天与系统消息
- `frontend`：Vue 前端工程
- `database`：数据库脚本

## 环境要求

- JDK 17
- Maven Wrapper（已提供 `mvnw.cmd`）
- MySQL
- Redis
- MongoDB
- RabbitMQ
- Node.js 18+（前端）

## 快速启动（开发环境）

1. 启动基础设施：MySQL、Redis、MongoDB、RabbitMQ。
2. 设置环境：
   - PowerShell：`$env:SPRING_PROFILES_ACTIVE='dev'`
3. 启动后端服务（建议顺序）：
   1. `rental-user`
   2. `rental-house`
   3. `rental-comment`
   4. `rental-message`
   5. `rental-order`
   6. `rental-gateway`
4. 启动前端：
   - `cd frontend`
   - `npm install`
   - `npm run dev`

## 构建与测试

- 执行全部测试：
  - `.\mvnw.cmd test`
- 打包全部模块：
  - `.\mvnw.cmd clean package -DskipTests`

## 配置与环境

- 各模块基础配置：`src/main/resources/application.yml`
- 开发环境覆盖：`application-dev.yml`
- 生产环境覆盖：`application-prod.yml`

生产环境上线前的变量与检查项请参考：

- `DEPLOYMENT_CHECKLIST.md`

## 安全说明

- 生产环境必须通过环境变量配置 JWT 密钥，且使用高强度随机值。
- 网关 CORS 必须只允许可信前端域名。
- 不要将内部服务直接暴露到公网。

## 许可证

除仓库所有者另有说明外，本项目默认用于个人学习与教学演示。
