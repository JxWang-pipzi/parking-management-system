# 智慧停车信息管理系统

基于 Spring Boot + Vue 3 + 微信小程序的三端架构智慧停车信息管理系统，提供停车场管理、车位预约、在线支付、传感器监控、数据统计等完整功能。

## 技术栈

**后端**

- Java 17
- Spring Boot 2.5
- MyBatis-Plus
- MySQL 8.0
- Redis
- Spring Security + JWT + BCrypt
- WebSocket

**前端**

- Vue 3
- Pinia
- Element Plus
- Vite
- WebSocket 客户端

**微信小程序**

- 原生微信小程序框架
- 自定义 TabBar

## 系统架构

系统采用三端分离架构：

| 端 | 端口 | 说明 |
|------|------|------|
| 后端服务 | 8081 | Spring Boot 应用，context-path 为 `/api`，提供 RESTful API 和 WebSocket 推送 |
| 前端管理 | 5173 | Vue 3 + Element Plus 管理后台，面向管理员和普通用户 |
| 微信小程序 | - | 原生小程序，面向车主用户，支持移动端停车预约与支付 |

认证方式：JWT Token + Spring Security + BCrypt 密码加密。

## 功能特性

- 停车场管理：停车场的增删改查、状态监控
- 车位管理：车位信息维护、实时状态更新
- 预约停车：在线预约车位、预约记录管理
- 在线支付：支付接口对接、支付记录查询
- 订单管理：订单生成、查询、状态流转
- 车辆进出记录：车辆入场/出场记录、车牌识别
- 传感器监控：车位传感器数据采集与监控
- 数据统计：停车场使用率、收入统计、趋势分析
- 实时推送：WebSocket 实时车位状态变更推送
- 地图导航：停车场定位与路线规划
- 智能推荐：基于用户行为的停车场推荐

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis
- Node.js 16+
- npm
- 微信开发者工具（小程序端）

### 后端启动

1. 创建 MySQL 数据库，执行 `parking-system/backend/src/main/resources/full_schema.sql` 初始化表结构
2. 修改 `application.yml` 中的数据库和 Redis 连接配置
3. 启动后端服务：

```bash
cd parking-system/backend
mvn spring-boot:run
```

后端服务启动后访问 `http://localhost:8081/api`

### 前端启动

```bash
cd parking-system/frontend
npm install
npm run dev
```

前端页面访问 `http://localhost:5173`

### 微信小程序

使用微信开发者工具打开 `parking-system/miniprogram` 目录，配置 AppID 后即可预览和调试。

## 项目结构

```
parking-system/
├── backend/                    # 后端服务
│   ├── src/main/java/com/parking/system/
│   │   ├── config/             # 配置类（Security、Redis、WebSocket等）
│   │   ├── controller/         # 控制器层
│   │   ├── entity/             # 实体类
│   │   ├── exception/          # 全局异常处理
│   │   ├── filter/             # JWT过滤器
│   │   ├── mapper/             # MyBatis-Plus Mapper
│   │   ├── service/            # 业务逻辑层
│   │   ├── util/               # 工具类（JwtUtil等）
│   │   └── ParkingSystemApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml     # 应用配置
│   │   └── full_schema.sql     # 数据库初始化脚本
│   └── pom.xml
├── frontend/                   # 前端管理后台
│   ├── src/
│   │   ├── components/         # 公共组件
│   │   ├── router/             # 路由配置
│   │   ├── store/              # Pinia状态管理
│   │   ├── styles/             # 样式文件
│   │   ├── utils/              # 工具函数（API、WebSocket等）
│   │   └── views/              # 页面视图
│   │       └── admin/          # 管理后台页面
│   ├── package.json
│   └── vite.config.js
└── miniprogram/                # 微信小程序
    ├── pages/                  # 小程序页面
    ├── utils/                  # 工具函数
    ├── images/                 # 图片资源
    ├── custom-tab-bar/         # 自定义TabBar
    ├── app.js
    └── app.json
```

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 普通用户 | user | 123456 |

## License

MIT
