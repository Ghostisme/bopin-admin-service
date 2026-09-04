# bopin-admin-service

播聘管理后台 Java 服务独立项目，Java 17 + Spring Boot 3.3 + JDBC。小程序的业务接口统一由本服务提供，接口前缀为 `/api/v1`。

## 数据库模式

- 默认模式仍然是本地 H2 文件，方便没有数据库配置时启动和开发。
- **MySQL 模式已经内置**：`application-mysql.yml`、`schema-mysql.sql` 和 MySQL JDBC 驱动均已加入项目。启用 `mysql` profile 后，用户、模卡、岗位、消息、订单、钱包、服务权限、提现等数据都会直接写入 MySQL。
- 正式 MySQL 默认只建表，不写入演示账号或演示岗位。`data-mysql.sql` 仅供本地演示，需要时通过 `MYSQL_DATA_LOCATIONS=classpath:data-mysql.sql` 显式启用；该文件使用 MySQL upsert，重复启动不会重复插入。

## MySQL 初始化

先在 MySQL 中创建数据库和业务账号（密码请替换为自己的强密码）：

```sql
CREATE DATABASE bopin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'bopin'@'localhost' IDENTIFIED BY 'CHANGE_ME';
GRANT ALL PRIVILEGES ON bopin.* TO 'bopin'@'localhost';
FLUSH PRIVILEGES;
```

Windows PowerShell 启动 MySQL profile：

```powershell
$env:SPRING_PROFILES_ACTIVE = "mysql"
$env:MYSQL_URL = "jdbc:mysql://127.0.0.1:3306/bopin?useUnicode=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
$env:MYSQL_USERNAME = "bopin"
$env:MYSQL_PASSWORD = "CHANGE_ME"
$env:MYSQL_INIT_MODE = "always"   # 自动创建缺失表；已有表不会重建
# 本地默认密钥只用于开发，正式环境必须覆盖为至少 32 字节的随机值
$env:JWT_SECRET = "CHANGE_TO_A_RANDOM_SECRET_WITH_AT_LEAST_32_BYTES"
# 可选：默认 30 天；单位为毫秒
$env:JWT_EXPIRATION_MS = "2592000000"
mvn spring-boot:run
```

如果只是本机演示，需要自动加入演示主播、岗位、课程等数据，再额外设置：

```powershell
$env:MYSQL_DATA_LOCATIONS = "classpath:data-mysql.sql"
```

正式环境不要设置 `MYSQL_DATA_LOCATIONS`。如果数据库结构已经由独立迁移系统维护，也可以将 `MYSQL_INIT_MODE` 设为 `never`。

也可以构建后启动：

```powershell
mvn clean package -DskipTests
java -jar target/bopin-admin-server-0.1.0.jar --spring.profiles.active=mysql
```

默认监听 `http://localhost:8080`，健康检查为 `http://localhost:8080/actuator/health`。启动后可看到 `{"status":"UP"}`，再从小程序访问 `/api/v1`。

本地网页调试允许 `localhost`、`127.0.0.1` 和 `198.18.0.1` 的任意开发端口，避免 H5/管理端更换端口后登录预检请求被 CORS 拒绝；正式环境应由网关限制为实际 HTTPS 域名。

## JWT 登录与通告分页

登录和注册成功后，服务端会返回一个由服务端 HMAC 密钥签名的 JWT access token（包含用户 ID、角色、签发时间、过期时间和 `jti`），同时返回 `tokenType=Bearer` 和 `expiresIn`（秒）。需要登录的接口统一使用标准请求头：

```http
Authorization: Bearer <access-token>
```

服务端会校验签名、issuer 和过期时间；缺少、伪造或过期 token 会返回 HTTP 401。旧版本已经写入 `auth_session` 的随机 token 仍可在迁移期间兼容读取，但新登录不会再生成旧 token。生产部署必须设置 `JWT_SECRET`（至少 32 字节、不要提交到代码仓库），并按需要设置 `JWT_EXPIRATION_MS` 和 `JWT_ISSUER`。

岗位通告接口已经改为 MySQL 服务端分页，避免客户端一次性拉取整张表：

```http
GET /api/v1/notices?page=1&pageSize=20&sort=recommend&city=杭州&category=live-commerce&jobType=full-time&keyword=女装
```

`page` 从 1 开始，`pageSize` 范围为 1–100（缺省 20）；`size` 参数可作为 `pageSize` 的兼容别名。排序支持 `recommend`、`nearby`、`latest`。返回结构：

```json
{
  "page": 1,
  "pageSize": 20,
  "size": 20,
  "total": 125,
  "totalPages": 7,
  "hasNext": true,
  "items": []
}
```

筛选、总数统计、排序、`LIMIT/OFFSET` 均在 MySQL 执行，管理端旧地址 `/api/admin/notices?page=1&size=20` 也保留并返回分页结果。

## 小程序连接地址

开发者工具本机调试可以使用 `http://localhost:8080/api/v1`。真机或上线环境不能使用 localhost，需要将小程序的 `TARO_APP_API_BASE_URL` 配置为已备案并启用 HTTPS 的公网域名，例如：

```text
https://api.example.com/api/v1
```

当前小程序运行配置已关闭 mock（`USE_MOCK=false`），因此 Java 服务不可用时页面会显示接口错误，而不会悄悄使用假数据。

## 构建

```powershell
mvn clean package -DskipTests
java -jar target/bopin-admin-server-0.1.0.jar
```

支付、AI、汇率和 EOR 的业务记录目前会真实写入数据库，但外部支付、模型、汇率和 EOR 通道仍是本地沙箱适配器；接入正式供应商时只需替换对应适配器，不影响 MySQL 数据结构和小程序接口。
