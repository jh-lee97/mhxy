-- 创建数据库
CREATE DATABASE IF NOT EXISTS profit_tracker DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE profit_tracker;

-- ========================================
-- 用户表
-- ========================================
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username        VARCHAR(50)     NOT NULL COMMENT '用户名',
    password        VARCHAR(255)    NOT NULL COMMENT '密码（BCrypt加密）',
    nickname        VARCHAR(50)     DEFAULT NULL COMMENT '昵称',
    phone           VARCHAR(20)     DEFAULT NULL COMMENT '手机号',
    email           VARCHAR(100)    DEFAULT NULL COMMENT '邮箱',
    avatar          VARCHAR(500)    DEFAULT NULL COMMENT '头像 URL',
    role_id         BIGINT          DEFAULT 1 COMMENT '角色ID',
    status          TINYINT         DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ========================================
-- 角色表
-- ========================================
DROP TABLE IF EXISTS sys_role;

CREATE TABLE sys_role (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_name       VARCHAR(50)     NOT NULL COMMENT '角色名称',
    role_code       VARCHAR(50)     NOT NULL COMMENT '角色编码',
    role_level      INT             DEFAULT 1 COMMENT '角色等级：数字越大权限越高',
    description     VARCHAR(255)    DEFAULT NULL COMMENT '描述',
    status          TINYINT         DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- ========================================
-- 收益记录表
-- ========================================
DROP TABLE IF EXISTS profit_record;

CREATE TABLE profit_record (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id         BIGINT          NOT NULL COMMENT '用户ID',
    date            VARCHAR(10)     NOT NULL COMMENT '日期 yyyy-MM-dd',
    mode            VARCHAR(20)     DEFAULT NULL COMMENT '玩法模式',
    activity        VARCHAR(100)    DEFAULT NULL COMMENT '活动名称',
    income          DECIMAL(10,2)   DEFAULT 0.00 COMMENT '常规收入(梦幻币)',
    cbg_income      DECIMAL(10,2)   DEFAULT 0.00 COMMENT '藏宝阁变现',
    道具Income      DECIMAL(10,2)   DEFAULT 0.00 COMMENT '道具/装备价值',
    cost            DECIMAL(10,2)   DEFAULT 0.00 COMMENT '总成本',
    remark          VARCHAR(500)    DEFAULT NULL COMMENT '备注',
    deleted         TINYINT         DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收益记录表';

-- ========================================
-- 游戏攻略表
-- ========================================
DROP TABLE IF EXISTS game_guide;

CREATE TABLE game_guide (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    category        VARCHAR(50)     NOT NULL COMMENT '分类（如：日常任务、副本、活动）',
    title           VARCHAR(200)    NOT NULL COMMENT '攻略标题',
    summary         VARCHAR(500)    DEFAULT NULL COMMENT '摘要',
    content         TEXT            DEFAULT NULL COMMENT '正文内容',
    sort_order      INT             DEFAULT 0 COMMENT '排序权重：数字越小越靠前',
    status          TINYINT         DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_category (category),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏攻略表';

-- ========================================
-- 初始化数据
-- ========================================

-- 默认管理员角色
INSERT INTO sys_role (role_name, role_code, role_level, description) VALUES ('超级管理员', 'ADMIN', 100, '拥有所有操作权限，可管理所有用户记录和系统配置');
INSERT INTO sys_role (role_name, role_code, role_level, description) VALUES ('普通用户', 'USER', 1, '仅可操作自己的收益记录');

-- 默认管理员用户（密码: admin123，BCrypt 加密），手机号用于验证码测试
INSERT INTO sys_user (username, password, nickname, phone, role_id) VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHmM8lE9lBOsl7iKTVKIUi', '管理员', '13800138000', 1);

-- ========================================
-- RBAC 权限系统（内置于 schema.sql）
-- ========================================

-- 权限表
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    parent_id       BIGINT          DEFAULT 0 COMMENT '父级权限ID，0表示顶级',
    name            VARCHAR(50)     NOT NULL COMMENT '权限名称',
    code            VARCHAR(100)    NOT NULL COMMENT '权限标识',
    type            TINYINT         DEFAULT 1 COMMENT '1-菜单 2-按钮 3-接口',
    path            VARCHAR(200)    DEFAULT NULL,
    method          VARCHAR(10)     DEFAULT NULL,
    sort_order      INT             DEFAULT 0,
    icon            VARCHAR(50)     DEFAULT NULL,
    status          TINYINT         DEFAULT 1,
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色-权限关联表
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    role_id         BIGINT          NOT NULL,
    permission_id   BIGINT          NOT NULL,
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';

-- 用户-角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    role_id         BIGINT          NOT NULL,
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';

-- 迁移现有 role_id 到 sys_user_role
INSERT INTO sys_user_role (user_id, role_id)
SELECT id, role_id FROM sys_user WHERE role_id IS NOT NULL AND role_id > 0;

-- 初始化权限数据
-- 一级菜单
SET @rec_id = (SELECT id FROM sys_permission WHERE code='record' LIMIT 1);
SET @guide_id = (SELECT id FROM sys_permission WHERE code='guide' LIMIT 1);
SET @task_id = (SELECT id FROM sys_permission WHERE code='task' LIMIT 1);
SET @user_id = (SELECT id FROM sys_permission WHERE code='user' LIMIT 1);

INSERT INTO sys_permission (name, code, type, path, sort_order, icon) VALUES
('仪表盘', 'dashboard', 1, '/', 1, 'DashboardOutlined'),
('收益记录', 'record', 1, '/records', 2, 'DollarOutlined'),
('游戏攻略', 'guide', 1, '/guides', 3, 'BookOutlined'),
('任务清单', 'task', 1, '/tasks', 4, 'ListOutlined'),
('用户管理', 'user', 1, '/users', 5, 'UserOutlined');

-- 收益记录 - 按钮/接口权限
INSERT INTO sys_permission (name, code, type, parent_id, path, method, sort_order) VALUES
('新增记录', 'record:add', 2, (SELECT id FROM sys_permission WHERE code='record'), '/api/records', 'POST', 1),
('编辑记录', 'record:edit', 2, (SELECT id FROM sys_permission WHERE code='record'), '/api/records', 'PUT', 2),
('删除记录', 'record:delete', 2, (SELECT id FROM sys_permission WHERE code='record'), '/api/records/{id}', 'DELETE', 3),
('查看记录', 'record:view', 2, (SELECT id FROM sys_permission WHERE code='record'), '/api/records', 'GET', 4),
('查看统计', 'record:stats', 2, (SELECT id FROM sys_permission WHERE code='record'), '/api/records/stats', 'GET', 5),
('查看图表', 'record:chart', 2, (SELECT id FROM sys_permission WHERE code='record'), '/api/records/chart', 'GET', 6);

-- 游戏攻略 - 按钮权限
INSERT INTO sys_permission (name, code, type, parent_id, path, sort_order) VALUES
('攻略管理', 'guide:manage', 2, (SELECT id FROM sys_permission WHERE code='guide'), '/api/admin/guides', 1);

-- 任务清单 - 按钮/接口权限
INSERT INTO sys_permission (name, code, type, parent_id, path, method, sort_order) VALUES
('查看任务', 'task:view', 2, (SELECT id FROM sys_permission WHERE code='task'), '/api/tasks', 'GET', 1),
('完成任务', 'task:edit', 2, (SELECT id FROM sys_permission WHERE code='task'), '/api/tasks/completion', 'POST', 2),
('创建计划', 'task:add', 2, (SELECT id FROM sys_permission WHERE code='task'), '/api/tasks/plan', 'POST', 3),
('删除计划', 'task:delete', 2, (SELECT id FROM sys_permission WHERE code='task'), '/api/tasks/plan/{id}', 'DELETE', 4),
('查看统计', 'task:stats', 2, (SELECT id FROM sys_permission WHERE code='task'), '/api/tasks/stats', 'GET', 5);

-- 用户管理 - 按钮权限
INSERT INTO sys_permission (name, code, type, parent_id, path, sort_order) VALUES
('用户管理', 'user:manage', 2, (SELECT id FROM sys_permission WHERE code='user'), '/api/admin/users', 1);

-- 分配所有权限给 ADMIN 角色
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

-- 分配基础权限给 USER 角色
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 2, id FROM sys_permission
WHERE code IN ('dashboard', 'record', 'guide', 'task',
               'record:add', 'record:edit', 'record:delete', 'record:view', 'record:stats', 'record:chart',
               'guide:manage',
               'task:view', 'task:edit', 'task:add', 'task:delete', 'task:stats');
INSERT INTO profit_record (user_id, date, mode, activity, income, cbg_income, 道具Income, cost, remark, created_at, updated_at)
VALUES
    (1, '2025-06-07', '副本', '秘境宝图', 500000, 5000, 10000, 200000, '日常副本收益', NOW(), NOW()),
    (1, '2025-06-07', '捉鬼', '捉鬼', 300000, 0, 0, 100000, '', NOW(), NOW()),
    (1, '2025-06-06', '副本', '秘境宝图', 450000, 3000, 8000, 180000, '', NOW(), NOW()),
    (1, '2025-06-05', '刷宝图', '宝图', 200000, 0, 0, 80000, '', NOW(), NOW()),
    (1, '2025-06-04', '日常任务', '师门', 150000, 0, 5000, 50000, '', NOW(), NOW()),
    (1, '2025-06-03', '活动', '周末活动', 600000, 10000, 20000, 300000, '周末高收益', NOW(), NOW()),
    (1, '2025-06-02', '跑商', '跑商', 400000, 0, 0, 150000, '', NOW(), NOW());

-- ========================================
-- 游戏攻略初始化数据
-- ========================================
INSERT INTO game_guide (category, title, summary, content, sort_order, status) VALUES
('日常任务', '师门任务高效攻略', '每天20次师门，稳定收益', '1. 准备足够的药品和暗器\n2. NPC位置：长安城（212,12）\n3. 战斗技巧：优先击杀小怪，最后打BOSS\n4. 预计收益：约15万梦幻币/次', 1, 1),
('日常任务', '捉鬼任务全解析', '每日必做，高额经验奖励', '1. 找阴间判官（建邺城）领鬼符\n2. 进入鬼王洞府\n3. 队伍配置：至少2法系+1物理+1辅助\n4. 速杀技巧：封印系优先出手\n5. 预计收益：约30万梦幻币/轮', 2, 1),
('副本', '秘境寻宝入门指南', '每日3次秘境，丰厚奖励', '1. 找长安城杂货商（200,150）进入\n2. 每关选择正确路线获得额外奖励\n3. 推荐路线：先左后右再上\n4. 通关奖励：经验、金钱、道具', 3, 1),
('副本', '英雄冢副本打法', '高难度副本，高回报', '1. 需要5人组队\n2. 第一层：清理小怪\n3. 第二层：注意躲避陷阱\n4. BOSS战：坦克顶住，治疗加血\n5. 推荐装备：防御>=5000', 4, 1),
('活动', '周末活动合集', '周六周日限定活动', '1. 华山论剑：PVP竞技\n2. 群雄逐鹿：帮派大战\n3. 科举大赛：知识问答\n4. 建议提前组队，准备药品', 5, 1),
('装备打造', '装备打造与修炼', '提升战力的关键途径', '1. 找铁匠铺打造装备\n2. 使用百炼精铁提高品质\n3. 装备附魔：找魔法学徒\n4. 宝石镶嵌：提升属性\n5. 推荐优先强化武器和衣服', 6, 1);
