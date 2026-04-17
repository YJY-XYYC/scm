# 供应链管理系统后端运行指南

## 环境要求

1. **JDK**: 1.8或更高版本
2. **Maven**: 3.6.0或更高版本
3. **MySQL**: 5.7或8.0

## 运行步骤

### 1. 安装Maven（如果尚未安装）

访问 [Apache Maven官网](https://maven.apache.org/download.cgi) 下载并安装Maven，然后配置环境变量。

### 2. 配置数据库

- 创建数据库 `scm`
- 用户名：`root`
- 密码：`123456`
- 确保MySQL服务在端口3306上运行

### 3. 构建项目

```bash
cd c:\Users\yjydjk\Desktop\supply-chain-management-main\backend
mvn clean package
```

### 4. 运行项目

有两种运行方式：

#### 方式一：使用Maven直接运行

```bash
mvn spring-boot:run
```

#### 方式二：运行打包后的jar文件

```bash
java -jar target/supply-chain-management-1.0-SNAPSHOT.jar
```

### 5. 验证运行

项目启动后，访问 `http://localhost:8080/scm` 检查服务是否正常运行。

## 依赖问题说明

如果遇到依赖缺失问题，Maven会自动从配置的仓库（包括阿里云仓库）下载所需的所有依赖，这比手动指定classpath要简单可靠得多。

## 项目配置

主要配置文件：`src/main/resources/application.yml`
- 服务端口：8080
- 上下文路径：/scm
- 数据库配置：已设置为本地MySQL
- MyBatis Plus配置：已配置好日志和映射器位置

## 常见问题

1. **数据库连接失败**：检查MySQL服务是否运行，以及用户名密码是否正确
2. **端口冲突**：修改application.yml中的server.port配置
3. **依赖下载慢**：项目已配置阿里云仓库加速