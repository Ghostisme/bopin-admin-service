# bopin-admin-service

播聘管理后台 Java 服务独立项目，Java 17 + Spring Boot 3.3 + JDBC + H2。

## 启动

```powershell
mvn spring-boot:run
```

默认监听 `http://localhost:8080`，健康检查为 `http://localhost:8080/actuator/health`，业务接口前缀为 `/api/v1`。

首次启动会在 `data/` 创建本地 H2 文件。`data/` 和 `target/` 均不会提交到 Git。

## 构建

```powershell
mvn clean package -DskipTests
java -jar target/bopin-admin-server-0.1.0.jar
```

当前支付、AI、汇率和 EOR 使用本地沙箱适配器，业务记录会真实落库，后续可独立替换为正式服务。
