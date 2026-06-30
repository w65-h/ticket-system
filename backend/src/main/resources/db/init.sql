-- 工单管理系统数据库初始化脚本
CREATE DATABASE IF NOT EXISTS ticket_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ticket_system;

-- 部门表
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    ancestors VARCHAR(500) DEFAULT '' COMMENT '祖级列表',
    sort_order INT DEFAULT 0 COMMENT '排序',
    leader VARCHAR(50) DEFAULT NULL COMMENT '负责人',
    phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_parent_id (parent_id)
) COMMENT '部门表';

-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    email VARCHAR(100) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    dept_id BIGINT DEFAULT NULL COMMENT '部门ID',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_dept_id (dept_id)
) COMMENT '用户表';

-- 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) COMMENT '角色表';

-- 权限表
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    perm_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    perm_code VARCHAR(50) NOT NULL UNIQUE COMMENT '权限编码',
    perm_type TINYINT DEFAULT 1 COMMENT '1-菜单 2-按钮',
    parent_id BIGINT DEFAULT 0,
    path VARCHAR(200) DEFAULT NULL,
    component VARCHAR(200) DEFAULT NULL,
    icon VARCHAR(50) DEFAULT NULL,
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_parent_id (parent_id)
) COMMENT '权限表';

-- 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id)
) COMMENT '用户角色关联表';

-- 角色权限关联表
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    perm_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_perm (role_id, perm_id)
) COMMENT '角色权限关联表';

-- 工单表
DROP TABLE IF EXISTS ticket;
CREATE TABLE ticket (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_no VARCHAR(30) NOT NULL UNIQUE COMMENT '工单编号',
    title VARCHAR(200) NOT NULL COMMENT '工单标题',
    content TEXT COMMENT '工单内容',
    status TINYINT DEFAULT 1 COMMENT '1-待处理 2-处理中 3-待验收 4-已完成 5-已驳回',
    priority TINYINT DEFAULT 1 COMMENT '1-低 2-中 3-高 4-紧急',
    creator_id BIGINT COMMENT '创建人ID',
    assignee_id BIGINT COMMENT '指派人ID',
    dept_id BIGINT COMMENT '所属部门ID',
    deadline DATETIME COMMENT '截止时间',
    finish_time DATETIME COMMENT '完成时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_ticket_no (ticket_no),
    INDEX idx_status (status),
    INDEX idx_creator (creator_id),
    INDEX idx_assignee (assignee_id),
    INDEX idx_dept (dept_id),
    INDEX idx_create_time (create_time)
) COMMENT '工单表';

-- 工单操作记录表
DROP TABLE IF EXISTS ticket_record;
CREATE TABLE ticket_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL COMMENT '工单ID',
    operation VARCHAR(50) NOT NULL COMMENT '操作',
    from_status TINYINT COMMENT '变更前状态',
    to_status TINYINT COMMENT '变更后状态',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人名称',
    remark VARCHAR(500) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ticket_id (ticket_id)
) COMMENT '工单操作记录表';

-- 通知表
DROP TABLE IF EXISTS sys_notification;
CREATE TABLE sys_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    is_read TINYINT DEFAULT 0 COMMENT '0-未读 1-已读',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    max_retry INT DEFAULT 3 COMMENT '最大重试次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read)
) COMMENT '通知表';

-- ==================== 初始数据 ====================

-- 部门
INSERT INTO sys_dept (id, dept_name, parent_id, ancestors, sort_order, leader) VALUES
(1, '总公司', 0, '0', 1, '张总'),
(2, '研发部', 1, '0,1', 2, '李经理'),
(3, '测试部', 1, '0,1', 3, '王经理'),
(4, '运维部', 1, '0,1', 4, '赵经理'),
(5, '前端组', 2, '0,1,2', 1, NULL),
(6, '后端组', 2, '0,1,2', 2, NULL);

-- 角色
INSERT INTO sys_role (id, role_name, role_code, description) VALUES
(1, '超级管理员', 'admin', '系统超级管理员，拥有所有权限'),
(2, '部门经理', 'manager', '部门经理，可查看本部门及下级部门工单'),
(3, '普通员工', 'employee', '普通员工，可查看和处理自己的工单');

-- 权限
INSERT INTO sys_permission (id, perm_name, perm_code, perm_type, parent_id, path, component, icon, sort_order) VALUES
(1, '仪表盘', 'dashboard', 1, 0, '/dashboard', 'dashboard/index', 'Dashboard', 1),
(2, '数据看板', 'dashboard:view', 2, 1, NULL, NULL, NULL, 1),
(3, '工单管理', 'ticket', 1, 0, '/ticket', 'ticket/index', 'Ticket', 2),
(4, '工单列表', 'ticket:list', 2, 3, NULL, NULL, NULL, 1),
(5, '创建工单', 'ticket:create', 2, 3, NULL, NULL, NULL, 2),
(6, '工单处理', 'ticket:process', 2, 3, NULL, NULL, NULL, 3),
(7, '系统管理', 'system', 1, 0, '/system', 'system/index', 'Setting', 3),
(8, '用户管理', 'user:manage', 2, 7, NULL, NULL, NULL, 1),
(9, '角色管理', 'role:manage', 2, 7, NULL, NULL, NULL, 2);

-- 用户 (密码均为 123456 的 BCrypt 加密值)
INSERT INTO sys_user (id, username, password, real_name, dept_id, status) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 1, 1),
(2, 'zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', 2, 1),
(3, 'lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', 3, 1),
(4, 'wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王五', 5, 1);

-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 3);

-- 角色权限关联
INSERT INTO sys_role_permission (role_id, perm_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9),
(2, 1), (2, 2), (2, 3), (2, 4), (2, 6),
(3, 1), (3, 2), (3, 3), (3, 4), (3, 5);

-- 示例工单数据
INSERT INTO ticket (ticket_no, title, content, status, priority, creator_id, assignee_id, dept_id, deadline, finish_time, create_time) VALUES
('TK-20250601-0001', '修复登录页面样式问题', '登录页面在移动端显示异常，需要适配响应式布局', 4, 2, 4, 2, 5, '2025-06-05 18:00:00', '2025-06-02 15:30:00', '2025-06-01 09:00:00'),
('TK-20250601-0002', '数据库慢查询优化', '订单列表查询超过3秒，需要优化SQL和索引', 4, 3, 4, 2, 6, '2025-06-03 18:00:00', '2025-06-02 10:00:00', '2025-06-01 10:00:00'),
('TK-20250602-0003', '新增用户批量导入功能', '运营需要支持Excel批量导入用户数据', 2, 2, 2, 3, 2, '2025-06-10 18:00:00', NULL, '2025-06-02 08:30:00'),
('TK-20250602-0004', 'API接口文档更新', 'Swagger文档与实际接口不一致，需要更新', 1, 1, 3, NULL, 3, '2025-06-08 18:00:00', NULL, '2025-06-02 14:00:00'),
('TK-20250603-0005', '生产环境内存告警排查', '服务器内存使用率达到95%，需紧急排查', 2, 4, 2, 2, 4, '2025-06-04 12:00:00', NULL, '2025-06-03 09:00:00'),
('TK-20250603-0006', '数据备份脚本失败', '每日自动备份脚本连续两天执行失败', 5, 3, 4, 2, 4, '2025-06-05 18:00:00', NULL, '2025-06-03 11:00:00'),
('TK-20250604-0007', '第三方支付接口对接', '对接微信支付新版本API', 3, 2, 2, 3, 2, '2025-06-15 18:00:00', NULL, '2025-06-04 09:00:00'),
('TK-20250604-0008', '权限配置页面开发', 'RBAC角色权限管理前端页面', 4, 2, 4, 2, 5, '2025-06-12 18:00:00', '2025-06-08 17:00:00', '2025-06-04 13:00:00');