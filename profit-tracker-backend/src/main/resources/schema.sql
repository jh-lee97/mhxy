-- 创建数据库
CREATE DATABASE IF NOT EXISTS profit_tracker DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE profit_tracker;

-- 收益记录表
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
    created_at      VARCHAR(30)     DEFAULT NULL COMMENT '创建时间 ISO 格式',
    updated_at      VARCHAR(30)     DEFAULT NULL COMMENT '更新时间 ISO 格式',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收益记录表';

-- 插入测试数据
INSERT INTO profit_record (user_id, date, mode, activity, income, cbg_income, 道具Income, cost, remark, created_at, updated_at)
VALUES
    (1, '2025-06-07', '副本', '秘境宝图', 500000, 5000, 10000, 200000, '日常副本收益', NOW(), NOW()),
    (1, '2025-06-07', '捉鬼', '捉鬼', 300000, 0, 0, 100000, '', NOW(), NOW()),
    (1, '2025-06-06', '副本', '秘境宝图', 450000, 3000, 8000, 180000, '', NOW(), NOW()),
    (1, '2025-06-05', '刷宝图', '宝图', 200000, 0, 0, 80000, '', NOW(), NOW()),
    (1, '2025-06-04', '日常任务', '师门', 150000, 0, 5000, 50000, '', NOW(), NOW()),
    (1, '2025-06-03', '活动', '周末活动', 600000, 10000, 20000, 300000, '周末高收益', NOW(), NOW()),
    (1, '2025-06-02', '跑商', '跑商', 400000, 0, 0, 150000, '', NOW(), NOW());