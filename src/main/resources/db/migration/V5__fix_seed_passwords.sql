-- =============================================================================
-- V5__fix_seed_passwords.sql: Corrige el hash BCrypt de los usuarios semilla a "password"
-- =============================================================================
UPDATE users
SET password = '$2a$10$eeLtHx24BfhsUo/NatZxQupVdBuOw29ScVwEj7BBCS/FBKJDgoWTy'
WHERE password = '$2a$10$7EqJtq98hPqEX7fNZaFWoO.N41Kq09h/yWlTquu1V8hEwE7s6j9i2';
