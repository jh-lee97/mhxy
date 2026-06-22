-- ========================================
-- RBAC 改造 - 数据库迁移脚本
-- ========================================

USE profit_tracker;

-- ========================================
-- 1. 新增权限表
-- ========================================
DROP TABLE IF EXISTS sys_permission;

CREATE TABLE sys_permission (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    parent_id       BIGINT          DEFAULT 0 COMMENT '父级权限ID，0表示顶级',
    name            VARCHAR(50)     NOT NULL COMMENT '权限名称',
    code            VARCHAR(100)    NOT NULL COMMENT '权限标识(如: record:add)',
    type            TINYINT         DEFAULT 1 COMMENT '1-菜单 2-按钮 3-接口',
    path            VARCHAR(200)    DEFAULT NULL COMMENT '路由/接口路径',
    method          VARCHAR(10)     DEFAULT NULL COMMENT 'HTTP方法(GET/POST等)',
    sort_order      INT             DEFAULT 0 COMMENT '排序',
    icon            VARCHAR(50)     DEFAULT NULL COMMENT '图标',
    status          TINYINT         DEFAULT 1 COMMENT '0-禁用 1-启用',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ========================================
-- 2. 新增角色-权限关联表
-- ========================================
DROP TABLE IF EXISTS sys_role_permission;

CREATE TABLE sys_role_permission (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    role_id         BIGINT          NOT NULL,
    permission_id   BIGINT          NOT NULL,
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_permission_id (permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_perm FOREIGN KEY (permission_id) REFERENCES sys_permission(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';

-- ========================================
-- 3. 新增用户-角色关联表
-- ========================================
DROP TABLE IF EXISTS sys_user_role;

CREATE TABLE sys_user_role (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    role_id         BIGINT          NOT NULL,
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_role_id (role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';

-- ========================================
-- 4. 迁移现有数据到新表
-- ========================================

-- 4.1 将 sys_user.role_id 迁移到 sys_user_role
INSERT INTO sys_user_role (user_id, role_id)
SELECT id, role_id FROM sys_user WHERE role_id IS NOT NULL AND role_id > 0;

-- 4.2 初始化权限数据

-- 一级菜单
INSERT INTO sys_permission (name, code, type, path, sort_order, icon) VALUES
('仪表盘', 'dashboard', 1, '/dashboard', 1, 'DashboardOutlined'),
('收益记录', 'record', 1, '/records', 2, 'DollarOutlined'),
('角色管理', 'role', 1, '/roles', 3, 'TeamOutlined'),
('用户管理', 'user', 1, '/users', 4, 'UserOutlined'),
('个人中心', 'profile', 1, '/profile', 5, 'SettingOutlined');

-- 收益记录 - 按钮权限
INSERT INTO sys_permission (name, code, type, parent_id, path, sort_order) VALUES
('新增记录', 'record:add', 2, (SELECT id FROM sys_permission WHERE code='record'), '/api/records', 1),
('编辑记录', 'record:edit', 2, (SELECT id FROM sys_permission WHERE code='record'), '/api/records', 2),
('删除记录', 'record:delete', 2, (SELECT id FROM sys_permission WHERE code='record'), '/api/records', 3),
('查看记录', 'record:view', 2, (SELECT id FROM sys_permission WHERE code='record'), '/api/records', 4),
('查看统计', 'record:stats', 2, (SELECT id FROM sys_permission WHERE code='record'), '/api/records/stats', 5),
('查看图表', 'record:chart', 2, (SELECT id FROM sys_permission WHERE code='record'), '/api/records/chart', 6);

-- 角色管理 - 按钮权限
INSERT INTO sys_permission (name, code, type, parent_id, path, sort_order) VALUES
('创建角色', 'role:create', 2, (SELECT id FROM sys_permission WHERE code='role'), '/api/roles', 1),
('编辑角色', 'role:update', 2, (SELECT id FROM sys_permission WHERE code='role'), '/api/roles', 2),
('删除角色', 'role:delete', 2, (SELECT id FROM sys_permission WHERE code='role'), '/api/roles', 3),
('查看角色', 'role:view', 2, (SELECT id FROM sys_permission WHERE code='role'), '/api/roles', 4);

-- 用户管理 - 按钮权限
INSERT INTO sys_permission (name, code, type, parent_id, path, sort_order) VALUES
('查看用户', 'user:view', 2, (SELECT id FROM sys_permission WHERE code='user'), '/api/auth/users', 1),
('禁用/启用', 'user:status', 2, (SELECT id FROM sys_permission WHERE code='user'), '/api/auth/users/:id/status', 2),
('分配角色', 'user:assign', 2, (SELECT id FROM sys_permission WHERE code='user'), '/api/auth/users/:id/role', 3),
('修改密码', 'user:resetpwd', 2, (SELECT id FROM sys_permission WHERE code='user'), '/api/auth/reset-password', 4);

-- 接口权限
INSERT INTO sys_permission (name, code, type, parent_id, path, method, sort_order) VALUES
('查看所有记录(管理员)', 'record:list:admin', 3, (SELECT id FROM sys_permission WHERE code='record'), '/api/records', 'GET', 10),
('管理用户接口', 'user:manage', 3, (SELECT id FROM sys_permission WHERE code='user'), '/api/auth/users*', 'GET|PUT', 11);

-- 4.3 分配权限给角色
-- ADMIN (id=1) 获得所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

-- USER (id=2) 获得基础权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 2, sp.id FROM sys_permission sp
WHERE sp.code IN ('dashboard', 'record:add', 'record:edit', 'record:delete', 'record:view', 'record:stats', 'record:chart', 'profile');

-- ========================================
-- 5. 清理 sys_user 中的 role_id 冗余字段（可选，保留一段时间做兼容）
-- ========================================
-- ALTER TABLE sys_user DROP COLUMN role_id;
-- ALTER TABLE sys_user DROP COLUMN role_level;
