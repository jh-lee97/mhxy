-- ========================================
-- 每日任务清单模块
-- ========================================

USE profit_tracker;

-- 每周任务计划表
DROP TABLE IF EXISTS task_plan;

CREATE TABLE task_plan (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id         BIGINT          NOT NULL COMMENT '用户ID',
    plan_name       VARCHAR(100)    NOT NULL COMMENT '计划名称，如"2025年第25周"',
    year            INT             NOT NULL COMMENT '所属年份',
    week_number     INT             NOT NULL COMMENT '所属周数 (1-53)',
    start_date      VARCHAR(10)     NOT NULL COMMENT '周一日期 yyyy-MM-dd',
    end_date        VARCHAR(10)     NOT NULL COMMENT '周日日期 yyyy-MM-dd',
    status          TINYINT         DEFAULT 1 COMMENT '状态：0-停用，1-进行中，2-已完成',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_year_week (year, week_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每周任务计划表';

-- 每日任务清单表
DROP TABLE IF EXISTS task_checklist;

CREATE TABLE task_checklist (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    plan_id         BIGINT          NOT NULL COMMENT '所属计划ID',
    day_of_week     TINYINT         NOT NULL COMMENT '星期几：1=周一, 7=周日',
    task_name       VARCHAR(100)    NOT NULL COMMENT '任务名称，如"师门任务"',
    category        VARCHAR(20)     DEFAULT '日常' COMMENT '任务分类：日常/副本/活动/其他',
    estimated_income DECIMAL(10,2)  DEFAULT 0.00 COMMENT '预估梦幻币收入(万)',
    estimated_time  DECIMAL(5,2)    DEFAULT 0.00 COMMENT '预估耗时(小时)',
    description     VARCHAR(500)    DEFAULT NULL COMMENT '任务描述/备注',
    sort_order      INT             DEFAULT 0 COMMENT '排序权重',
    mandatory       TINYINT         DEFAULT 1 COMMENT '是否必做：0-选做，1-必做',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_plan_id (plan_id),
    INDEX idx_day_of_week (day_of_week)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日任务清单表';

-- 每日任务完成记录表
DROP TABLE IF EXISTS task_completion;

CREATE TABLE task_completion (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    checklist_id    BIGINT          NOT NULL COMMENT '任务清单ID',
    plan_id         BIGINT          NOT NULL COMMENT '所属计划ID',
    user_id         BIGINT          NOT NULL COMMENT '用户ID',
    complete_date   VARCHAR(10)     NOT NULL COMMENT '完成日期 yyyy-MM-dd',
    actual_income   DECIMAL(10,2)   DEFAULT 0.00 COMMENT '实际收入(万)',
    actual_time     DECIMAL(5,2)    DEFAULT 0.00 COMMENT '实际耗时(小时)',
    status          TINYINT         DEFAULT 0 COMMENT '完成状态：0-未完成，1-已完成，2-跳过',
    completed_at    DATETIME        DEFAULT NULL COMMENT '完成时间',
    remark          VARCHAR(500)    DEFAULT NULL COMMENT '备注',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_plan_id (plan_id),
    INDEX idx_user_id (user_id),
    INDEX idx_complete_date (complete_date),
    UNIQUE KEY uk_checklist_date (checklist_id, complete_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日任务完成记录表';

-- ========================================
-- 初始化示例数据
-- ========================================

-- 示例：本周任务计划（管理员创建，供参考）
INSERT INTO task_plan (user_id, plan_name, year, week_number, start_date, end_date, status)
VALUES (1, '2025年第26周', 2025, 26, '2025-06-23', '2025-06-29', 1);

-- 示例：每日任务清单
-- 周一~周五（工作日）
INSERT INTO task_checklist (plan_id, day_of_week, task_name, category, estimated_income, estimated_time, description, sort_order, mandatory) VALUES
(1, 1, '师门任务', '日常', 15.00, 0.5, '每天20次，稳定收益', 1, 1),
(1, 1, '捉鬼任务', '日常', 30.00, 1.0, '找阴间判官领鬼符，速杀', 2, 1),
(1, 1, '宝图任务', '日常', 20.00, 1.0, '打宝图出装备/藏宝图', 3, 0),
(1, 2, '师门任务', '日常', 15.00, 0.5, '', 1, 1),
(1, 2, '捉鬼任务', '日常', 30.00, 1.0, '', 2, 1),
(1, 2, '秘境寻宝', '副本', 25.00, 0.5, '每日3次', 3, 1),
(1, 3, '师门任务', '日常', 15.00, 0.5, '', 1, 1),
(1, 3, '捉鬼任务', '日常', 30.00, 1.0, '', 2, 1),
(1, 3, '跑商任务', '日常', 40.00, 1.0, '注意价格波动', 3, 0),
(1, 4, '师门任务', '日常', 15.00, 0.5, '', 1, 1),
(1, 4, '捉鬼任务', '日常', 30.00, 1.0, '', 2, 1),
(1, 4, '副本挑战', '副本', 50.00, 1.0, '英雄冢/幽冥地府', 3, 0),
(1, 5, '师门任务', '日常', 15.00, 0.5, '', 1, 1),
(1, 5, '捉鬼任务', '日常', 30.00, 1.0, '', 2, 1),
(1, 5, '宝图任务', '日常', 20.00, 1.0, '周末宝图爆率高', 3, 0),
-- 周六（周末活动日）
(1, 6, '师门任务', '日常', 15.00, 0.5, '', 1, 1),
(1, 6, '捉鬼任务', '日常', 30.00, 1.0, '', 2, 1),
(1, 6, '周末活动', '活动', 60.00, 1.5, '华山/群雄/科举', 3, 1),
(1, 6, '副本挑战', '副本', 50.00, 1.0, '高难度副本', 4, 0),
-- 周日
(1, 7, '师门任务', '日常', 15.00, 0.5, '', 1, 1),
(1, 7, '捉鬼任务', '日常', 30.00, 1.0, '', 2, 1),
(1, 7, '宝图任务', '日常', 20.00, 1.0, '', 3, 0),
(1, 7, '跑商任务', '日常', 40.00, 1.0, '整理下周计划', 4, 0);
