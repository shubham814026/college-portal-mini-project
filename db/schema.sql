CREATE DATABASE IF NOT EXISTS college_db;
USE college_db;

CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(256) NOT NULL,
    role ENUM('STUDENT', 'ADMIN') NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    failed_attempts INT DEFAULT 0,
    locked_until DATETIME DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notices (
    notice_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    posted_by INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_notice_user FOREIGN KEY (posted_by) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS messages (
    message_id INT PRIMARY KEY AUTO_INCREMENT,
    sender_id INT NOT NULL,
    receiver_id INT DEFAULT NULL,
    room VARCHAR(50) DEFAULT 'general',
    content TEXT NOT NULL,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_msg_sender FOREIGN KEY (sender_id) REFERENCES users(user_id),
    CONSTRAINT fk_msg_receiver FOREIGN KEY (receiver_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS files (
    file_id INT PRIMARY KEY AUTO_INCREMENT,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL UNIQUE,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(20) NOT NULL,
    uploaded_by INT NOT NULL,
    subject_tag VARCHAR(100),
    uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_file_user FOREIGN KEY (uploaded_by) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS alerts (
    alert_id INT PRIMARY KEY AUTO_INCREMENT,
    message TEXT NOT NULL,
    sent_by INT NOT NULL,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alert_user FOREIGN KEY (sent_by) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    action VARCHAR(100) NOT NULL,
    ip_address VARCHAR(45),
    logged_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

INSERT INTO users(username, password_hash, role, full_name, email)
VALUES
('admin', SHA2('admin123', 256), 'ADMIN', 'System Admin', 'admin@college.local'),
('rahul', SHA2('rahul123', 256), 'STUDENT', 'Rahul Sharma', 'rahul@college.local'),
('priya', SHA2('priya123', 256), 'STUDENT', 'Priya Verma', 'priya@college.local')
ON DUPLICATE KEY UPDATE username = username;
