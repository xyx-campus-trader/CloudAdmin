-- ========================================
-- CloudAdmin 微服务后台管理系统 数据库
-- ========================================
CREATE DATABASE IF NOT EXISTS mate_admin DEFAULT CHARSET utf8mb4;
USE mate_admin;

-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(128) NOT NULL,
    nickname VARCHAR(64),
    email VARCHAR(128),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    status TINYINT DEFAULT 1 COMMENT '1启用 0禁用',
    dept_id BIGINT COMMENT '部门ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0 COMMENT '0未删 1已删',
    UNIQUE KEY uk_username (username)
) COMMENT '用户表';

-- 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(64) NOT NULL,
    role_code VARCHAR(64) NOT NULL,
    status TINYINT DEFAULT 1 COMMENT '1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_code (role_code)
) COMMENT '角色表';

-- 用户-角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
) COMMENT '用户角色关联';

-- 菜单表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    name VARCHAR(64) NOT NULL COMMENT '菜单名称',
    path VARCHAR(128) COMMENT '前端路由',
    component VARCHAR(128) COMMENT '前端组件',
    icon VARCHAR(64) COMMENT '图标',
    sort INT DEFAULT 0,
    type TINYINT DEFAULT 1 COMMENT '1目录 2菜单 3按钮',
    permission VARCHAR(128) COMMENT '权限标识',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    INDEX idx_parent (parent_id),
    INDEX idx_sort (sort)
) COMMENT '菜单表';

-- 角色-菜单关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
) COMMENT '角色菜单关联';

-- 部门表
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(64) NOT NULL,
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent (parent_id),
    INDEX idx_status (status)
) COMMENT '部门表';

-- 操作日志表
DROP TABLE IF EXISTS sys_log;
CREATE TABLE sys_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64),
    operation VARCHAR(64) COMMENT '操作名称',
    method VARCHAR(255) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(64),
    execution_time BIGINT COMMENT '执行耗时(ms)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_create_time (create_time),
    INDEX idx_username (username)
) COMMENT '操作日志表';

-- ========= 初始数据 =========
-- 默认用户: admin / 123456 (BCrypt)
INSERT INTO sys_user (username, password, nickname, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '系统管理员', 1);

-- 默认角色
INSERT INTO sys_role (role_name, role_code) VALUES ('超级管理员', 'admin'), ('普通用户', 'user');
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 默认菜单
INSERT INTO sys_menu (id, parent_id, name, path, component, type, permission, sort) VALUES
(1, 0, '系统管理', '/system', '', 1, '', 1),
(2, 1, '用户管理', '/system/user', '', 2, 'system:user:list', 1),
(3, 1, '角色管理', '/system/role', '', 2, 'system:role:list', 2),
(4, 1, '菜单管理', '/system/menu', '', 2, 'system:menu:list', 3),
(5, 1, '部门管理', '/system/dept', '', 2, 'system:dept:list', 4);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5);

-- 默认部门
INSERT INTO sys_dept (id, parent_id, name, sort) VALUES
(1, 0, '总公司', 1),
(2, 1, '研发部', 1),
(3, 1, '市场部', 2);
