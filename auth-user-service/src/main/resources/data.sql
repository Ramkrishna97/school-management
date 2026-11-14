INSERT INTO user_accounts (id, username, password_hash, role, teacher_id, student_id, enabled) VALUES
  (1, 'admin', 'BOOTSTRAP', 'SUPER_ADMIN', NULL, NULL, TRUE),
  (2, 'principal', 'BOOTSTRAP', 'PRINCIPAL', NULL, NULL, TRUE),
  (3, 'teacher', 'BOOTSTRAP', 'TEACHER', 2, NULL, TRUE),
  (4, 'accountant', 'BOOTSTRAP', 'ACCOUNTANT', NULL, NULL, TRUE),
  (5, 'librarian', 'BOOTSTRAP', 'LIBRARIAN', NULL, NULL, TRUE),
  (6, 'storemanager', 'BOOTSTRAP', 'STORE_MANAGER', NULL, NULL, TRUE),
  (7, 'student', 'BOOTSTRAP', 'STUDENT', NULL, 1, TRUE),
  (8, 'parent', 'BOOTSTRAP', 'PARENT', NULL, NULL, TRUE);
INSERT INTO parent_student_links (id, parent_user_id, student_id) VALUES (1, 8, 1);
ALTER TABLE user_accounts ALTER COLUMN id RESTART WITH 100;
ALTER TABLE parent_student_links ALTER COLUMN id RESTART WITH 100;
