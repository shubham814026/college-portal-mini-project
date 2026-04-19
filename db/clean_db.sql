USE college_db;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE group_files;
TRUNCATE TABLE group_notices;
TRUNCATE TABLE notice_group_members;
TRUNCATE TABLE notice_groups;
TRUNCATE TABLE chat_group_messages;
TRUNCATE TABLE chat_group_members;
TRUNCATE TABLE chat_groups;
TRUNCATE TABLE logs;
TRUNCATE TABLE alerts;
TRUNCATE TABLE messages;
TRUNCATE TABLE files;
TRUNCATE TABLE notices;
TRUNCATE TABLE events;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO users(username, password_hash, role, full_name, email)
VALUES
('admin', SHA2('admin123', 256), 'ADMIN', 'System Admin', 'admin@college.local'),
('rahul', SHA2('rahul123', 256), 'STUDENT', 'Rahul Sharma', 'rahul@college.local'),
('priya', SHA2('priya123', 256), 'STUDENT', 'Priya Verma', 'priya@college.local'),
('faculty', SHA2('faculty123', 256), 'FACULTY', 'Faculty Member', 'faculty@college.local');
