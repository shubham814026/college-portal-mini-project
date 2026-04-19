CREATE DATABASE IF NOT EXISTS college_db;
USE college_db;

CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(256) NOT NULL,
    role ENUM('STUDENT', 'ADMIN', 'FACULTY') NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    failed_attempts INT DEFAULT 0,
    locked_until DATETIME DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE users MODIFY role ENUM('STUDENT', 'ADMIN', 'FACULTY') NOT NULL;

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

CREATE TABLE IF NOT EXISTS events (
    event_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    event_type ENUM('CLASS', 'EXAM', 'EVENT') NOT NULL,
    event_date DATE NOT NULL,
    start_time TIME DEFAULT NULL,
    end_time TIME DEFAULT NULL,
    location VARCHAR(120) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_event (title, event_date, start_time)
);

INSERT INTO users(username, password_hash, role, full_name, email)
VALUES
('admin', SHA2('admin123', 256), 'ADMIN', 'System Admin', 'admin@college.local'),
('rahul', SHA2('rahul123', 256), 'STUDENT', 'Rahul Sharma', 'rahul@college.local'),
('priya', SHA2('priya123', 256), 'STUDENT', 'Priya Verma', 'priya@college.local'),
('faculty', SHA2('faculty123', 256), 'FACULTY', 'Faculty Member', 'faculty@college.local')
ON DUPLICATE KEY UPDATE username = username;

INSERT INTO events(title, description, event_type, event_date, start_time, end_time, location)
VALUES
('DSA Lecture', 'Weekly DSA lecture', 'CLASS', '2026-04-20', '09:00:00', '10:00:00', 'Room 101'),
('OS Lab', 'Operating Systems practical', 'CLASS', '2026-04-22', '11:00:00', '13:00:00', 'Lab 2'),
('Midterm Exam', 'Midterm examination (Unit 1-3)', 'EXAM', '2026-04-25', '10:00:00', '12:00:00', 'Main Hall'),
('Tech Club Meet', 'Monthly club meetup', 'EVENT', '2026-04-27', '16:00:00', '17:00:00', 'Seminar Room')
ON DUPLICATE KEY UPDATE title = title;
