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
