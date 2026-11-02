-- =============================================================================
-- V2__seed_test_data.sql: Dataset Extendido de Pruebas y Demostración
-- =============================================================================
-- Este script inserta usuarios adicionales y un conjunto amplio y diverso de
-- reportes ciudadanos con ubicaciones espaciales PostGIS (SRID 4326), distintas
-- categorías, niveles de urgencia y estados de atención.

-- -----------------------------------------------------------------------------
-- 1. Inserción de Usuarios de Prueba Adicionales
-- -----------------------------------------------------------------------------
INSERT INTO users (id_user, username, email, password, rol, first_name, last_name)
VALUES
    ('11111111-1111-4111-a111-111111111111', 'laura_ciudadana', 'laura.sanchez@gmail.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.N41Kq09h/yWlTquu1V8hEwE7s6j9i2', 'CIUDADANO', 'Laura', 'Sánchez'),
    ('22222222-2222-4222-a222-222222222222', 'miguel_vecino', 'miguel.angel.castro@gmail.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.N41Kq09h/yWlTquu1V8hEwE7s6j9i2', 'CIUDADANO', 'Miguel Ángel', 'Castro'),
    ('33333333-3333-4333-a333-333333333333', 'patricia_verde', 'patricia.morales@alertcity.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.N41Kq09h/yWlTquu1V8hEwE7s6j9i2', 'AREAS_VERDES', 'Patricia', 'Morales'),
    ('44444444-4444-4444-a444-444444444444', 'esteban_animales', 'esteban.ramirez@alertcity.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.N41Kq09h/yWlTquu1V8hEwE7s6j9i2', 'ANIMALES', 'Esteban', 'Ramírez'),
    ('55555555-5555-4555-a555-555555555555', 'fernanda_agua', 'fernanda.diaz@alertcity.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.N41Kq09h/yWlTquu1V8hEwE7s6j9i2', 'AGUA_DRENAJE', 'Fernanda', 'Díaz'),
    ('66666666-6666-4666-a666-666666666666', 'david_alumbrado', 'david.reyes@alertcity.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.N41Kq09h/yWlTquu1V8hEwE7s6j9i2', 'ALUMBRADO', 'David', 'Reyes'),
    ('77777777-7777-4777-a777-777777777777', 'gabriela_limpieza', 'gabriela.flores@alertcity.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.N41Kq09h/yWlTquu1V8hEwE7s6j9i2', 'LIMPIEZA', 'Gabriela', 'Flores'),
    ('88888888-8888-4888-a888-888888888888', 'hugo_ciudadano', 'hugo.vargas@yahoo.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.N41Kq09h/yWlTquu1V8hEwE7s6j9i2', 'CIUDADANO', 'Hugo', 'Vargas'),
    ('99999999-9999-4999-a999-999999999999', 'valeria_ciudadana', 'valeria.ortiz@hotmail.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.N41Kq09h/yWlTquu1V8hEwE7s6j9i2', 'CIUDADANO', 'Valeria', 'Ortiz'),
    ('aaaaaaaa-aaaa-4aaa-aaaa-aaaaaaaaaaaa', 'rodrigo_ciudadano', 'rodrigo.navarro@gmail.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.N41Kq09h/yWlTquu1V8hEwE7s6j9i2', 'CIUDADANO', 'Rodrigo', 'Navarro'),
    ('bbbbbbbb-bbbb-4bbb-bbbb-bbbbbbbbbbbb', 'sofia_ciudadana', 'sofia.castillo@gmail.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.N41Kq09h/yWlTquu1V8hEwE7s6j9i2', 'CIUDADANO', 'Sofía', 'Castillo')
ON CONFLICT (id_user) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 2. Inserción de Reportes Urbanos Extensos (PostGIS Point: X=Longitud, Y=Latitud)
-- -----------------------------------------------------------------------------
INSERT INTO reports (id_report, id_user, title, description, category, urgent, date_time, location, address_reference, state)
VALUES
    -- 1. Vialidad - Bache peligroso
    (
        'c0000001-0000-4000-8000-000000000001',
        '22222222-2222-4222-a222-222222222222',
        'Hundimiento de pavimento en carril lateral',
        'Se formó un hundimiento considerable en la carpeta asfáltica debido al tránsito pesado de camiones de carga.',
        'VIALIDAD',
        true,
        NOW() - INTERVAL '3 hours',
        ST_SetSRID(ST_MakePoint(-101.1962, 20.1471), 4326),
        'Blvd. Plan de Ayala, esquina con Río Lerma',
        'ENVIADO'
    ),
    -- 2. Alumbrado - Falla en serie de luminarias
    (
        'c0000001-0000-4000-8000-000000000002',
        '66666666-6666-4666-a666-666666666666',
        'Circuito completo de luminarias apagadas en avenida comercial',
        'Cinco postes continuos se encuentran sin energía desde hace cuatro noches consecutivas, dejando la zona comercial a oscuras.',
        'ALUMBRADO',
        false,
        NOW() - INTERVAL '8 hours',
        ST_SetSRID(ST_MakePoint(-101.1934, 20.1495), 4326),
        'Av. 16 de Septiembre, entre Aldama y Guerrero',
        'ENVIADO'
    ),
    -- 3. Agua y Drenaje - Drenaje colapsado
    (
        'c0000001-0000-4000-8000-000000000003',
        '55555555-5555-4555-a555-555555555555',
        'Brote de aguas negras de registro sanitario',
        'El registro pluvial y sanitario está saturado y expulsa aguas negras hacia la calle, generando malos olores e insalubridad.',
        'AGUA',
        true,
        NOW() - INTERVAL '1 day',
        ST_SetSRID(ST_MakePoint(-101.1882, 20.1389), 4326),
        'Calle Matamoros #112, frente al kinder Benito Juárez',
        'EN_PROCESO'
    ),
    -- 4. Limpieza - Escombros en vía pública
    (
        'c0000001-0000-4000-8000-000000000004',
        '77777777-7777-4777-a777-777777777777',
        'Escombros de demolición abandonados en la banqueta',
        'Camión de volteo descargó bultos de yeso, tabiques y cascajo sobre la banqueta peatonal impidiendo el tránsito de personas.',
        'LIMPIEZA',
        false,
        NOW() - INTERVAL '18 hours',
        ST_SetSRID(ST_MakePoint(-101.2010, 20.1532), 4326),
        'Calle Pinos s/n, casi esquina con Jacarandas',
        'ENVIADO'
    ),
    -- 5. Seguridad - Barandal de puente vehicular dañado
    (
        'c0000001-0000-4000-8000-000000000005',
        '11111111-1111-4111-a111-111111111111',
        'Barandal de protección fracturado tras choque',
        'El muro de contención y barandal metálico del puente fue derribado por un vehículo accidentado. Hay riesgo de caída al arroyo.',
        'SEGURIDAD',
        true,
        NOW() - INTERVAL '2 days',
        ST_SetSRID(ST_MakePoint(-101.1905, 20.1448), 4326),
        'Puente del Río Huacapa, carril norte-sur',
        'EN_PROCESO'
    ),
    -- 6. Áreas Verdes - Rama de eucalipto a punto de caer
    (
        'c0000001-0000-4000-8000-000000000006',
        '33333333-3333-4333-a333-333333333333',
        'Rama gruesa recargada sobre cables de media tensión',
        'Rama seca de más de cuatro metros de largo cuelga peligrosamente sobre el cableado de luz frente a una vivienda.',
        'AREAS_VERDES',
        true,
        NOW() - INTERVAL '2 days',
        ST_SetSRID(ST_MakePoint(-101.1948, 20.1420), 4326),
        'Calle Galeana #56, Barrio de San Miguel',
        'EN_PROCESO'
    ),
    -- 7. Animales - Perros en situación de calle en área escolar
    (
        'c0000001-0000-4000-8000-000000000007',
        '44444444-4444-4444-a444-444444444444',
        'Jauría agresiva afuera de secundaria técnica',
        'Grupo de 6 perros sin dueño deambulan en el perímetro de la escuela y han intentado morder a estudiantes al horario de salida.',
        'ANIMALES',
        false,
        NOW() - INTERVAL '3 days',
        ST_SetSRID(ST_MakePoint(-101.1865, 20.1460), 4326),
        'Calle Educación Técnica s/n, Col. Magisterial',
        'EN_PROCESO'
    ),
    -- 8. Vialidad - Señal de alto derribada
    (
        'c0000001-0000-4000-8000-000000000008',
        '88888888-8888-4888-a888-888888888888',
        'Disco de alto arrancado de su base',
        'La señalética vertical de ALTO en un cruce de cuatro esquinas fue arrancada, generando confusión y frenadas de emergencia.',
        'VIALIDAD',
        false,
        NOW() - INTERVAL '4 days',
        ST_SetSRID(ST_MakePoint(-101.1995, 20.1441), 4326),
        'Intersección de Abasolo con Ocampo',
        'RESUELTO'
    ),
    -- 9. Agua y Drenaje - Fuga de agua en medidor
    (
        'c0000001-0000-4000-8000-000000000009',
        '99999999-9999-4999-a999-999999999999',
        'Fuga en válvula de expulsión de agua potable',
        'Chorro continuo de agua potable desperdiciándose en la banqueta exterior de un inmueble deshabitado.',
        'AGUA',
        false,
        NOW() - INTERVAL '5 days',
        ST_SetSRID(ST_MakePoint(-101.1920, 20.1502), 4326),
        'Calle Zaragoza #89, entre Rayón e Iturbide',
        'RESUELTO'
    ),
    -- 10. Alumbrado - Lámpara que parpadea continuamente
    (
        'c0000001-0000-4000-8000-000000000010',
        '66666666-6666-4666-a666-666666666666',
        'Luminaria suburbana con fotocelda averiada',
        'La lámpara prende y apaga intermitentemente cada cinco segundos durante toda la noche causando molestias vecinales.',
        'ALUMBRADO',
        false,
        NOW() - INTERVAL '6 days',
        ST_SetSRID(ST_MakePoint(-101.1970, 20.1410), 4326),
        'Andador Los Sauces #14, Manzana 3',
        'RESUELTO'
    ),
    -- 11. Limpieza - Contenedor comunal desbordado
    (
        'c0000001-0000-4000-8000-000000000011',
        '77777777-7777-4777-a777-777777777777',
        'Contenedores de basura desbordados en mercado municipal',
        'El camión recolector no ha pasado en 4 días y la basura invade un carril de la vía pública atrayendo moscas y fauna nociva.',
        'LIMPIEZA',
        true,
        NOW() - INTERVAL '1 day',
        ST_SetSRID(ST_MakePoint(-101.1930, 20.1478), 4326),
        'Lateral del Mercado Hidalgo, rampa de carga y descarga',
        'EN_PROCESO'
    ),
    -- 12. Seguridad - Falta de señalización en excavación profunda
    (
        'c0000001-0000-4000-8000-000000000012',
        '11111111-1111-4111-a111-111111111111',
        'Zanja abierta de obra sin conos ni cinta preventiva',
        'Empresa constructora dejó una zanja de 1.80 metros de profundidad sin señalización luminosa ni barricadas.',
        'SEGURIDAD',
        true,
        NOW() - INTERVAL '7 hours',
        ST_SetSRID(ST_MakePoint(-101.1898, 20.1435), 4326),
        'Av. del Trabajo #302, casi esq. Ferrocarril',
        'ENVIADO'
    ),
    -- 13. Áreas Verdes - Pasto crecido e invasión de maleza en campo deportivo
    (
        'c0000001-0000-4000-8000-000000000013',
        '33333333-3333-4333-a333-333333333333',
        'Maleza excesiva en canchas deportivas de colonia',
        'La maleza supera el metro de altura, imposibilitando el uso de la cancha y propiciando presencia de alacranes y víboras.',
        'AREAS_VERDES',
        false,
        NOW() - INTERVAL '7 days',
        ST_SetSRID(ST_MakePoint(-101.2035, 20.1550), 4326),
        'Unidad Deportiva Norte, cancha #3 de fútbol rápido',
        'RESUELTO'
    ),
    -- 14. Vialidad - Registros de teléfono hundidos
    (
        'c0000001-0000-4000-8000-000000000014',
        '22222222-2222-4222-a222-222222222222',
        'Tapa metálica de registro hundida a mitad de crucero',
        'La tapa metálica perdió soporte de concreto y se bambolea peligrosamente cada vez que pasa un vehículo.',
        'VIALIDAD',
        true,
        NOW() - INTERVAL '12 hours',
        ST_SetSRID(ST_MakePoint(-101.1915, 20.1468), 4326),
        'Paseo de la República esq. 5 de Febrero',
        'ENVIADO'
    ),
    -- 15. Otro - Graffiti en monumento histórico
    (
        'c0000001-0000-4000-8000-000000000015',
        '99999999-9999-4999-a999-999999999999',
        'Pinta vandálica con aerosol sobre fachada de cantera',
        'Afectación al patrimonio arquitectónico del quiosco de la plaza con pintura de esmalte.',
        'OTRO',
        false,
        NOW() - INTERVAL '10 days',
        ST_SetSRID(ST_MakePoint(-101.1942, 20.1470), 4326),
        'Plaza Principal, quiosco central frente a Presidencia',
        'RESUELTO'
    ),
    -- 16. Animales - Caballo suelto en carretera
    (
        'c0000001-0000-4000-8000-000000000016',
        '44444444-4444-4444-a444-444444444444',
        'Semoviente sin custodio pastando a orilla de carretera estatal',
        'Caballo de gran tamaño cruzando inesperadamente los carriles vehiculares de alta velocidad.',
        'ANIMALES',
        true,
        NOW() - INTERVAL '4 hours',
        ST_SetSRID(ST_MakePoint(-101.2050, 20.1360), 4326),
        'Carretera salida a Valle, km 2.5',
        'ENVIADO'
    ),
    -- 17. Agua y Drenaje - Falta total de presión de agua en sector
    (
        'c0000001-0000-4000-8000-000000000017',
        '55555555-5555-4555-a555-555555555555',
        'Desabasto de agua potable por falla en bomba del pozo #4',
        'Tres manzanas llevan 48 horas continuas sin suministro de agua en la red pública domiciliaria.',
        'AGUA',
        true,
        NOW() - INTERVAL '2 days',
        ST_SetSRID(ST_MakePoint(-101.1850, 20.1490), 4326),
        'Fraccionamiento Las Arboledas, calles Ébano y Cedro',
        'EN_PROCESO'
    ),
    -- 18. Alumbrado - Poste chocado a punto de colapsar
    (
        'c0000001-0000-4000-8000-000000000018',
        '66666666-6666-4666-a666-666666666666',
        'Poste de concreto fisurado e inclinado hacia la calle',
        'Un camión golpeó el poste en la madrugada; la base está quebrada con las varillas expuestas.',
        'ALUMBRADO',
        true,
        NOW() - INTERVAL '5 hours',
        ST_SetSRID(ST_MakePoint(-101.1925, 20.1418), 4326),
        'Calle Libertad #410, entre Insurgentes y Terán',
        'ENVIADO'
    ),
    -- 19. Vialidad - Semáforo peatonal descompuesto
    (
        'c0000001-0000-4000-8000-000000000019',
        '88888888-8888-4888-a888-888888888888',
        'Botón de demanda peatonal y luz verde peatonal inoperante',
        'Los peatones no pueden cruzar la avenida principal de 4 carriles porque el semáforo nunca les cede el paso.',
        'VIALIDAD',
        false,
        NOW() - INTERVAL '14 days',
        ST_SetSRID(ST_MakePoint(-101.1960, 20.1485), 4326),
        'Cruce peatonal frente a la clínica del IMSS',
        'RESUELTO'
    ),
    -- 20. Limpieza - Llantas abandonadas en lote baldío
    (
        'c0000001-0000-4000-8000-000000000020',
        '77777777-7777-4777-a777-777777777777',
        'Acumulación de decenas de neumáticos con agua estancada',
        'Taller mecánico clandestino arrojó llantas en un predio abierto; representan foco de propagación de dengue y riesgo de incendio.',
        'LIMPIEZA',
        true,
        NOW() - INTERVAL '1 day',
        ST_SetSRID(ST_MakePoint(-101.1870, 20.1525), 4326),
        'Camino a las Tijeras s/n, a espaldas del rastro',
        'ENVIADO'
    )
ON CONFLICT (id_report) DO NOTHING;
