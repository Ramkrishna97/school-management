INSERT INTO teachers (id, name, department, qualification, class_teacher_of,
                      casual_leave_balance, sick_leave_balance, earned_leave_balance) VALUES
  (1, 'Asha Rao', 'Mathematics', 'M.Sc. Mathematics, B.Ed.', '7-A', 12, 10, 15),
  (2, 'Ravi Shah', 'Mathematics', 'M.Sc. Mathematics', '8-A', 12, 10, 15),
  (3, 'Meera Iyer', 'Science', 'M.Sc. Physics, B.Ed.', '9-B', 12, 10, 15),
  (4, 'Fatima Khan', 'Science', 'M.Sc. Chemistry', NULL, 12, 10, 15),
  (5, 'Arjun Nair', 'Languages', 'M.A. English, B.Ed.', '6-A', 12, 10, 15),
  (6, 'Priya Das', 'Social Studies', 'M.A. History, B.Ed.', NULL, 12, 10, 15);

INSERT INTO teacher_subjects (teacher_id, subject_code) VALUES
  (1, 'MATH'), (2, 'MATH'), (3, 'SCIENCE'), (3, 'PHYSICS'),
  (4, 'SCIENCE'), (4, 'CHEMISTRY'), (5, 'ENGLISH'), (6, 'HISTORY');

INSERT INTO teacher_leaves (id, teacher_id, start_date, end_date, type, status, reason, decision_note) VALUES
  (1, 1, DATE '2030-01-07', DATE '2030-01-07', 'SICK', 'APPROVED', 'Medical appointment', 'Approved sample leave'),
  (2, 4, DATE '2030-01-08', DATE '2030-01-09', 'CASUAL', 'PENDING', 'Family event', NULL);

ALTER TABLE teachers ALTER COLUMN id RESTART WITH 100;
ALTER TABLE teacher_leaves ALTER COLUMN id RESTART WITH 100;
