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

INSERT INTO users(username, password_hash, role, full_name, email) VALUES
('admin', SHA2('admin123', 256), 'ADMIN', 'System Admin', 'admin@college.local'),
('faculty1', SHA2('pass123', 256), 'FACULTY', 'Prof. Smith', 'smith@college.local'),
('faculty2', SHA2('pass123', 256), 'FACULTY', 'Prof. Doe', 'doe@college.local'),
('student1', SHA2('pass123', 256), 'STUDENT', 'Alice Student', 'alice@college.local'),
('student2', SHA2('pass123', 256), 'STUDENT', 'Bob Student', 'bob@college.local');

-- Total 30 entries across tables
-- 10 notices
INSERT INTO notices(title, body, posted_by) VALUES
('Welcome to the New Semester', 'Classes start on Monday.', 1),
('Holiday Announcement', 'College closed on Friday.', 1),
('Exam Schedule', 'Midterms next week.', 2),
('Library Timings', 'Library open till 8PM.', 1),
('Sports Meet', 'Annual sports meet registrations open.', 1),
('Tech Fest 2026', 'Participate in coding competitions!', 3),
('Fee Payment', 'Last date for fee payment is 30th April.', 1),
('Guest Lecture', 'Guest lecture on AI on Wednesday.', 2),
('Placement Drive', 'Company XYZ visiting on 15th.', 1),
('Maintenance Downtime', 'Portal down for maintenance tonight.', 1);

-- 10 events
INSERT INTO events(title, description, event_type, event_date, start_time, end_time, location) VALUES
('DSA Lecture', 'Weekly DSA lecture', 'CLASS', '2026-04-20', '09:00:00', '10:00:00', 'Room 101'),
('OS Lab', 'Operating Systems practical', 'CLASS', '2026-04-22', '11:00:00', '13:00:00', 'Lab 2'),
('Midterm Exam', 'Midterm examination (Unit 1-3)', 'EXAM', '2026-04-25', '10:00:00', '12:00:00', 'Main Hall'),
('Tech Club Meet', 'Monthly club meetup', 'EVENT', '2026-04-27', '16:00:00', '17:00:00', 'Seminar Room'),
('DBMS Lecture', 'Database lecture', 'CLASS', '2026-04-21', '10:00:00', '11:00:00', 'Room 102'),
('CN Lab', 'Networks Lab', 'CLASS', '2026-04-23', '14:00:00', '16:00:00', 'Lab 3'),
('Final Exam', 'Final examination', 'EXAM', '2026-05-25', '10:00:00', '13:00:00', 'Main Hall'),
('Cultural Fest', 'Annual cultural festival', 'EVENT', '2026-05-15', '09:00:00', '20:00:00', 'Auditorium'),
('AI Workshop', 'Workshop on deep learning', 'EVENT', '2026-04-28', '14:00:00', '16:00:00', 'Seminar Room'),
('SE Lecture', 'Software Engineering', 'CLASS', '2026-04-24', '11:00:00', '12:00:00', 'Room 103');

-- 10 files (Need branch, year, sem, subject_tag, approval_status)
-- Year 1: Sem 1, Sem 2
-- Year 2: Sem 3, Sem 4
-- Year 3: Sem 5, Sem 6
-- Year 4: Sem 7, Sem 8
INSERT INTO files(original_name, stored_name, file_size, file_type, uploaded_by, subject_tag, branch, year_of_study, semester, approval_status) VALUES
('Syllabus_CS.pdf', 'uuid-1.pdf', 102400, 'pdf', 2, 'General', 'CS', 1, 1, 'APPROVED'),
('Math_Notes.pdf', 'uuid-2.pdf', 204800, 'pdf', 4, 'Mathematics', 'CS', 1, 2, 'APPROVED'),
('DSA_Slides.pdf', 'uuid-3.pdf', 512000, 'pdf', 2, 'Data Structures', 'CS', 2, 3, 'APPROVED'),
('OS_Manual.pdf', 'uuid-4.pdf', 409600, 'pdf', 3, 'Operating Systems', 'CS', 2, 4, 'PENDING'),
('DBMS_Assignment.docx', 'uuid-5.docx', 153600, 'docx', 5, 'DBMS', 'IT', 3, 5, 'APPROVED'),
('CN_Project.zip', 'uuid-6.zip', 1048576, 'zip', 4, 'Computer Networks', 'IT', 3, 6, 'REJECTED'),
('AI_Research.pdf', 'uuid-7.pdf', 307200, 'pdf', 3, 'Artificial Intelligence', 'CS', 4, 7, 'APPROVED'),
('ML_Dataset.csv', 'uuid-8.csv', 819200, 'csv', 5, 'Machine Learning', 'CS', 4, 8, 'APPROVED'),
('Physics_Lab.pdf', 'uuid-9.pdf', 256000, 'pdf', 2, 'Physics', 'ECE', 1, 1, 'APPROVED'),
('Circuit_Diagrams.png', 'uuid-10.png', 51200, 'png', 4, 'Electronics', 'ECE', 2, 3, 'APPROVED');

