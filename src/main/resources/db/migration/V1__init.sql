-- =============================================================================
-- V1__init.sql: Inicialización de Extensiones, Esquema y Datos de Prueba (Seed)
-- =============================================================================

-- 1. Extensiones requeridas
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "postgis";

-- 2. Tabla de Usuarios
CREATE TABLE IF NOT EXISTS users (
    id_user UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Tabla de Reportes con soporte espacial PostGIS (EPSG:4326)
CREATE TABLE IF NOT EXISTS reports (
    id_report UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_user UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    urgent BOOLEAN DEFAULT FALSE NOT NULL,
    date_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    location GEOMETRY(Point, 4326) NOT NULL,
    address_reference VARCHAR(255),
    state VARCHAR(50) DEFAULT 'ENVIADO' NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reports_user
        FOREIGN KEY (id_user)
        REFERENCES users(id_user)
        ON DELETE CASCADE
);

-- 4. Índices para rendimiento
CREATE INDEX IF NOT EXISTS idx_reports_location ON reports USING GIST (location);
CREATE INDEX IF NOT EXISTS idx_reports_user ON reports (id_user);
CREATE INDEX IF NOT EXISTS idx_reports_state ON reports (state);
CREATE INDEX IF NOT EXISTS idx_reports_category ON reports (category);
CREATE INDEX IF NOT EXISTS idx_reports_date ON reports (date_time DESC);

-- =============================================================================
-- 5. Data Set Inicial (Seed Data)
-- =============================================================================

-- Inserción de Usuarios de prueba
INSERT INTO users (id_user, username, email, password, rol, first_name, last_name)
VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'carlos_vialidad', 'carlos.mendoza@alertcity.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.N41Kq09h/yWlTquu1V8hEwE7s6j9i2', 'VIALIDAD', 'Carlos', 'Mendoza'),
    ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'maria_alumbrado', 'maria.gonzalez@alertcity.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.N41Kq09h/yWlTquu1V8hEwE7s6j9i2', 'ALUMBRADO', 'María', 'González'),
    ('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'roberto_agua', 'roberto.torres@alertcity.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.N41Kq09h/yWlTquu1V8hEwE7s6j9i2', 'AGUA_DRENAJE', 'Roberto', 'Torres'),
    ('d3eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'ana_limpieza', 'ana.lopez@alertcity.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.N41Kq09h/yWlTquu1V8hEwE7s6j9i2', 'LIMPIEZA', 'Ana', 'López'),
    ('e4eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'jorge_seguridad', 'jorge.hernandez@alertcity.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.N41Kq09h/yWlTquu1V8hEwE7s6j9i2', 'SEGURIDAD', 'Jorge', 'Hernández'),
    ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'pedro_ciudadano', 'pedro.ramirez@alertcity.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.N41Kq09h/yWlTquu1V8hEwE7s6j9i2', 'CIUDADANO', 'Pedro', 'Ramírez')
ON CONFLICT (id_user) DO NOTHING;

-- Inserción de Reportes con coordenadas PostGIS (ST_SetSRID(ST_MakePoint(Longitud, Latitud), 4326))
INSERT INTO reports (id_report, id_user, title, description, category, urgent, date_time, location, address_reference, state)
VALUES
    (
        'f5eebc99-9c0b-4ef8-bb6d-6bb9bd380b01',
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        'Bache profundo en carril de alta velocidad',
        'Bache de gran tamaño con bordes filosos que ha provocado daños en neumáticos de vehículos particulares y autobuses.',
        'VIALIDAD',
        true,
        NOW() - INTERVAL '2 days',
        ST_SetSRID(ST_MakePoint(-101.1923, 20.1458), 4326),
        'Av. Juárez, pasando el puente peatonal frente a la tienda de conveniencia',
        'EN_PROCESO'
    ),
    (
        'f5eebc99-9c0b-4ef8-bb6d-6bb9bd380b02',
        'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22',
        'Poste de alumbrado público sin luz y con cables expuestos',
        'La luminaria lleva apagada más de tres semanas, dejando el callejón en total oscuridad y con cables sueltos al alcance peatonal.',
        'ALUMBRADO',
        true,
        NOW() - INTERVAL '1 day',
        ST_SetSRID(ST_MakePoint(-101.1955, 20.1482), 4326),
        'Calle Morelos esq. Niños Héroes, Col. Centro',
        'ENVIADO'
    ),
    (
        'f5eebc99-9c0b-4ef8-bb6d-6bb9bd380b03',
        'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33',
        'Fuga constante de agua potable en banqueta',
        'Fuga considerable de agua limpia brotando de una tubería subterránea, genera encharcamiento constante y reblandecimiento del suelo.',
        'AGUA',
        false,
        NOW() - INTERVAL '3 days',
        ST_SetSRID(ST_MakePoint(-101.1890, 20.1415), 4326),
        'Calle Hidalgo #245, afuera de la panadería El Sol',
        'EN_PROCESO'
    ),
    (
        'f5eebc99-9c0b-4ef8-bb6d-6bb9bd380b04',
        'd3eebc99-9c0b-4ef8-bb6d-6bb9bd380a44',
        'Basurero clandestino y maleza sobre banqueta',
        'Acumulación excesiva de ramas secas, escombros y bolsas de basura obstaculizando el libre paso de peatones y personas con discapacidad.',
        'LIMPIEZA',
        false,
        NOW() - INTERVAL '5 days',
        ST_SetSRID(ST_MakePoint(-101.1980, 20.1510), 4326),
        'Prolongación Zaragoza s/n, lote baldío junto a la escuela primaria',
        'RESUELTO'
    ),
    (
        'f5eebc99-9c0b-4ef8-bb6d-6bb9bd380b05',
        'e4eebc99-9c0b-4ef8-bb6d-6bb9bd380a55',
        'Semáforo desincronizado en crucero conflictivo',
        'Los semáforos se encuentran intermitentes o cambian simultáneamente en verde, provocando riesgos severos de choques vehiculares.',
        'VIALIDAD',
        true,
        NOW() - INTERVAL '6 hours',
        ST_SetSRID(ST_MakePoint(-101.1912, 20.1430), 4326),
        'Cruce de Calzada de los Héroes con Blvd. Independencia',
        'ENVIADO'
    ),
    (
        'f5eebc99-9c0b-4ef8-bb6d-6bb9bd380b06',
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        'Árbol caído obstruyendo parque público',
        'Debido a las lluvias pasadas, un árbol de gran altura se desplomó sobre los juegos infantiles y la pista de correr.',
        'AREAS_VERDES',
        false,
        NOW() - INTERVAL '4 days',
        ST_SetSRID(ST_MakePoint(-101.1940, 20.1465), 4326),
        'Parque Alameda Central, área norponiente',
        'RESUELTO'
    ),
    (
        'f5eebc99-9c0b-4ef8-bb6d-6bb9bd380b07',
        'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33',
        'Alcantarilla pluvial sin tapa ni señalamiento',
        'Peligro inminente para ciclistas y peatones por boca de tormenta descubierta sin rejilla protectora.',
        'AGUA',
        true,
        NOW() - INTERVAL '12 hours',
        ST_SetSRID(ST_MakePoint(-101.1875, 20.1402), 4326),
        'Calle 5 de Mayo, esquina con Mina',
        'ENVIADO'
    )
ON CONFLICT (id_report) DO NOTHING;
