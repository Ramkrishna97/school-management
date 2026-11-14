INSERT INTO timetable_entries (id, day_of_week, period_number, class_name, section, subject_code, teacher_id) VALUES
  (1, 'MONDAY', 1, '7', 'A', 'MATH', 1),
  (2, 'MONDAY', 2, '7', 'A', 'ENGLISH', 5),
  (3, 'MONDAY', 3, '7', 'A', 'SCIENCE', 3),
  (4, 'MONDAY', 4, '7', 'A', 'HISTORY', 6),
  (5, 'MONDAY', 5, '7', 'A', 'MATH', 2),
  (6, 'MONDAY', 6, '7', 'A', 'CHEMISTRY', 4),
  (7, 'MONDAY', 7, '7', 'A', 'ENGLISH', 5),
  (8, 'MONDAY', 8, '7', 'A', 'SCIENCE', 3);
ALTER TABLE timetable_entries ALTER COLUMN id RESTART WITH 100;
