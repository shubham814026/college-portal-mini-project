-- =============================================================
-- Schema V2 — Extends original schema with new features
-- Run AFTER schema.sql
-- =============================================================

USE college_db;

-- -----------------------------------------------
-- 1. File approval + folder structure columns
-- -----------------------------------------------
ALTER TABLE files ADD COLUMN branch VARCHAR(50) DEFAULT NULL;
ALTER TABLE files ADD COLUMN year_of_study INT DEFAULT NULL;
ALTER TABLE files ADD COLUMN semester INT DEFAULT NULL;
ALTER TABLE files ADD COLUMN approval_status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'APPROVED';
ALTER TABLE files ADD COLUMN reviewed_by INT DEFAULT NULL;
ALTER TABLE files ADD COLUMN reviewed_at DATETIME DEFAULT NULL;

-- -----------------------------------------------
-- 2. Chat Groups (invite-only, private)
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS chat_groups (
    group_id    INT PRIMARY KEY AUTO_INCREMENT,
    group_name  VARCHAR(100) NOT NULL,
    description TEXT,
    created_by  INT NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS chat_group_members (
    id        INT PRIMARY KEY AUTO_INCREMENT,
    group_id  INT NOT NULL,
    user_id   INT NOT NULL,
    role      ENUM('OWNER','MEMBER') DEFAULT 'MEMBER',
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES chat_groups(group_id),
    FOREIGN KEY (user_id)  REFERENCES users(user_id),
    UNIQUE KEY uq_chat_grp_member (group_id, user_id)
);

CREATE TABLE IF NOT EXISTS chat_group_messages (
    message_id INT PRIMARY KEY AUTO_INCREMENT,
    group_id   INT NOT NULL,
    sender_id  INT NOT NULL,
    content    TEXT NOT NULL,
    sent_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id)  REFERENCES chat_groups(group_id),
    FOREIGN KEY (sender_id) REFERENCES users(user_id)
);

-- -----------------------------------------------
-- 3. Notice Groups (invite / request-join)
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS notice_groups (
    group_id    INT PRIMARY KEY AUTO_INCREMENT,
    group_name  VARCHAR(100) NOT NULL,
    description TEXT,
    created_by  INT NOT NULL,
    join_policy ENUM('INVITE_ONLY','REQUEST_JOIN') DEFAULT 'INVITE_ONLY',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS notice_group_members (
    id        INT PRIMARY KEY AUTO_INCREMENT,
    group_id  INT NOT NULL,
    user_id   INT NOT NULL,
    role      ENUM('OWNER','ADMIN','MEMBER') DEFAULT 'MEMBER',
    status    ENUM('ACTIVE','PENDING') DEFAULT 'ACTIVE',
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES notice_groups(group_id),
    FOREIGN KEY (user_id)  REFERENCES users(user_id),
    UNIQUE KEY uq_notice_grp_member (group_id, user_id)
);

CREATE TABLE IF NOT EXISTS group_notices (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    group_id   INT NOT NULL,
    title      VARCHAR(200) NOT NULL,
    body       TEXT NOT NULL,
    posted_by  INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id)  REFERENCES notice_groups(group_id),
    FOREIGN KEY (posted_by) REFERENCES users(user_id)
);

-- -----------------------------------------------
-- 4. Group file sharing (files shared in chat groups)
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS group_files (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    group_id        INT NOT NULL,
    file_id         INT NOT NULL,
    shared_by       INT NOT NULL,
    approval_status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    reviewed_by     INT DEFAULT NULL,
    reviewed_at     DATETIME DEFAULT NULL,
    shared_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id)  REFERENCES chat_groups(group_id),
    FOREIGN KEY (file_id)   REFERENCES files(file_id),
    FOREIGN KEY (shared_by) REFERENCES users(user_id),
    UNIQUE KEY uq_group_file (group_id, file_id)
);
