---
name: profit-tracker-architecture
title: 梦幻西游五开收益记录项目架构
description: 梦幻西游五开收益记录项目的完整架构总结（Vue3 + Spring Boot + MyBatis-Plus + JWT + Redis）
metadata:
  type: project
---

## 项目概述
**梦幻西游 · 五开收益记录系统** —— 面向《梦幻西游》多开玩家的游戏收益管理工具。前后端分离架构。

## 技术栈
| 层级 | 技术 | 版本 |
|------|------|------|
| 前端 | Vue 3 (Composition API) + Vite + ECharts + Axios | Vue 3.5 / Vite 8 / ECharts 6.1 |
| 后端 | Spring Boot + MyBatis-Plus + Spring Security + JWT | Boot 2.7.18 / MP 3.5.5 |
| 数据库 | MySQL 8 (utf8mb4) | - |
| 缓存 | Redis (StringRedisTemplate) | 验证码存储 |
| 安全 | BCrypt 密码加密 + JWT 无状态认证 | HS256 |
| API文档 | Swagger/OpenAPI 3 (springdoc) | - |
| 构建 | Maven (后端) / npm (前端) | Java 1.8 |

## 目录结构
```
D:\aiCode\mhxy\
├── profit-tracker/                        # 前端 Vue 3 SPA
│   ├── package.json / vite.config.js
│   └── src/
│       ├── main.js                        # Vue 挂载入口
│       ├── style.css                      # 全局样式
│       ├── App.vue                        # 根组件（页面布局）
│       ├── stores/profitStore.js          # 轻量级 Store（Axios API 封装）
│       └── components/
│           ├── Dashboard.vue              # 📊 统计卡片（今日/本周/累计）
│           ├── ProfitChart.vue            # 📈 ECharts 7日趋势图
│           ├── AddRecordForm.vue          # ➕ 新增记录弹窗（成本自动计算）
│           └── RecordList.vue             # 📋 记录表格 + 删除确认
│
└── profit-tracker-backend/                # 后端 Spring Boot
    ├── pom.xml
    └── src/main/
        ├── java/com/profit/track/
        │   ├── ProfitTrackerApplication.java   # 启动类 @MapperScan
        │   ├── config/
        │   │   ├── MybatisPlusConfig.java      # 分页/自动填充/CORS
        │   │   ├── SecurityConfig.java         # SecurityFilterChain + BCrypt
        │   │   ├── JwtAuthenticationFilter.java # JWT OncePerRequestFilter
        │   │   └── RedisConfig.java            # StringRedisTemplate
        │   ├── controller/
        │   │   ├── AuthController.java         # 🔐 登录/注册/改密/验证码
        │   │   ├── RoleController.java         # 👥 角色 CRUD
        │   │   └── ProfitRecordController.java # 💰 收益记录 CRUD/统计/图表
        │   ├── service/ + impl/
        │   │   ├── ProfitRecordService/Impl    # 记录业务 + 统计聚合
        │   │   ├── SysUserService/Impl         # 认证 + 密码管理
        │   │   └── SysRoleService/Impl         # 角色业务
        │   ├── mapper/
        │   │   ├── ProfitRecordMapper          # extends BaseMapper<ProfitRecord>
        │   │   ├── SysUserMapper               # extends BaseMapper<SysUser>
        │   │   └── SysRoleMapper               # extends BaseMapper<SysRole>
        │   ├── entity/
        │   │   ├── ProfitRecord.java           # 收益记录实体
        │   │   ├── SysUser.java                # 用户实体
        │   │   └── SysRole.java                # 角色实体
        │   ├── dto/                            # 统一 Result<T> + 各请求/响应 DTO
        │   └── util/
        │       ├── JwtUtil.java                # JWT 生成/解析/验证
        │       └── VerificationCodeUtil.java   # 验证码 Redis 存取/验证
        └── resources/
            ├── application.yml                 # 端口8080 / MySQL / MyBatis-Plus
            └── schema.sql                      # 3表DDL + 初始化数据
```

## 认证授权架构
```
请求 → SecurityFilterChain → 白名单放行(/api/auth/login/register/send-code/reset-password)
                          → JwtAuthenticationFilter 解析 Token → request.setAttribute(userId/username)
                          → 其余请求 require authenticated()
Controller 从 request.getAttribute("userId") 获取当前用户
```

**JWT 流程：**
1. 登录 → JwtUtil.generateToken(userId, username, roleName) → HS256 签名
2. 客户端存 Token，后续请求带 `Authorization: Bearer <token>`
3. 过滤器解析 Token，注入 userId 到 request 属性

## API 接口汇总

### 认证模块 (/api/auth)
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | /api/auth/login | 用户名密码登录，返回 JWT | 匿名 |
| POST | /api/auth/register | 手机号+验证码注册 | 匿名 |
| GET | /api/auth/me | 获取当前用户信息 | JWT |
| PUT | /api/auth/change-password | 修改密码（需旧密码） | JWT |
| POST | /api/auth/reset-password | 手机号+验证码重置密码 | 匿名 |
| POST | /api/auth/send-code | 发送登录验证码 | 匿名 |
| POST | /api/auth/send-register-code | 发送注册验证码 | 匿名 |

### 角色模块 (/api/roles)
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| GET | /api/roles | 获取所有角色列表 | JWT |
| POST | /api/roles | 创建角色 | JWT |
| PUT | /api/roles | 更新角色 | JWT |
| DELETE | /api/roles/{id} | 删除角色 | JWT |

### 收益记录模块 (/api/records)
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| GET | /api/records?userId=1 | 记录列表 | JWT |
| POST | /api/records | 新增记录 | JWT |
| PUT | /api/records | 更新记录 | JWT |
| DELETE | /api/records/{id}?userId=1 | 删除记录 | JWT |
| GET | /api/records/stats?userId=1 | 统计（今日/本周/累计） | JWT |
| GET | /api/records/chart?userId=1 | 近7日趋势数据 | JWT |

## 数据库设计
```
┌──────────────┐       ┌──────────────────┐       ┌─────────────────┐
│   sys_user   │       │  profit_record   │       │    sys_role     │
├──────────────┤       ├──────────────────┤       ├─────────────────┤
│ id (PK)      │──────▶│ user_id (FK)     │       │ id (PK)         │
│ username (UQ)│       │ date             │       │ role_name       │
│ password     │       │ mode             │       │ role_code (UQ)  │
│ nickname     │       │ activity         │       │ description     │
│ phone        │       │ income           │       │ status          │
│ email        │       │ cbg_income       │       │ created_at      │
│ avatar       │       │ prop_income      │       │ updated_at      │
│ role_id (FK) │◀──────│ cost             │       └─────────────────┘
│ status       │       │ remark           │
│ created_at   │       │ created_at      │
│ updated_at   │       │ updated_at      │
└──────────────┘       └──────────────────┘
```

## 核心业务逻辑

### 成本计算（前端 AddRecordForm.vue）
```
成本 = 在线小时 × 6(每回合6个号) × 账号数 × 点卡单价(默认1.3万/小时/号)
总收入 = 梦幻币收入 + 物品收入
净利润 = 总收入 - 成本
```

### 统计聚合（后端 ProfitRecordServiceImpl.getStats）
```
遍历全部记录，按日期维度聚合：
  今日：date == today
  本周：date >= 本周一
  累计：所有记录
收入 = income + cbgIncome + propIncome
利润 = 收入 - cost
```

### 验证码机制
```
Redis Key: mhxy:code:{phone}
TTL: 5 分钟
策略: 一次性使用（验证后立即删除，防重放）
传输: 打印到控制台（开发模式，未接短信/邮件服务）
```

## 前端状态管理
```javascript
// profitStore.js — 轻量级函数式 Store
// 不使用 Pinia/Vuex，纯函数封装
// - 无全局状态树，每次操作独立调用 API
// - 组件通过 ref 管理本地状态
// - 金额格式化：>=1万显示"万"单位
// ⚠️ 待改造：当前硬编码 userId=1，未集成 JWT 认证
```

## 架构特点总结
| 维度 | 状态 |
|------|------|
| 架构风格 | 经典三层 + 前后端分离 ✅ |
| 安全方案 | Spring Security + JWT 无状态认证 ✅ |
| 密码存储 | BCrypt 加密 ✅ |
| API 规范 | 统一 Result<T> + Swagger 注解 ✅ |
| ORM | MyBatis-Plus 简化 CRUD ✅ |
| 验证码 | Redis 存储，控制台输出（开发模式）⚠️ |
| 前端状态 | 轻量函数式 Store（非 Pinia）⚠️ |
| 多租户 | 单用户设计，userId 硬编码 ❌ |
| 接口权限 | 有 RBAC 基础，但未做接口级权限控制 ⚠️ |
| 测试覆盖 | 无单元测试 ❌ |
| 配置安全 | 数据库密码明文在 application.yml ❌ |
| 数据库规范 | 字段 `道具Income` 中文字段名不规范 ⚠️ |

## 演进方向
1. 前端迁移到 Pinia 状态管理
2. 前端集成 JWT Token 认证，替换硬编码 userId
3. Controller 层加入 `@PreAuthorize` 接口级权限控制
4. 验证码接入短信/邮件服务
5. 敏感配置移至环境变量
6. 修复中文字段名规范
7. 补充 Service 层单元测试
8. Vite 生产构建优化（API 代理等）
