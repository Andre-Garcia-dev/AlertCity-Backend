-- =============================================================================
-- V4__seed_admin_user.sql: Inserción de Usuario Administrador por defecto
-- =============================================================================
INSERT INTO users (id_user, username, email, password, rol, first_name, last_name)
VALUES (
    '00000000-0000-4000-8000-000000000001',
    'admin',
    'admin@alertcity.com',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoO.N41Kq09h/yWlTquu1V8hEwE7s6j9i2',
    'ADMIN',
    'Admin',
    'AlertCity'
)
ON CONFLICT (username) DO NOTHING;
