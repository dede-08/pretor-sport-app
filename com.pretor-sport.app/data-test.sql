-- Datos de prueba para la base de datos pretor_sport_db

-- Insertar categorías
INSERT INTO categorias (nombre, descripcion, tipo, icono_url, activo, fecha_creacion, fecha_actualizacion) VALUES
('Fútbol', 'Artículos para fútbol', 'DEPORTE', 'https://via.placeholder.com/50x50?text=F', true, NOW(), NOW()),
('Básquetbol', 'Artículos para básquetbol', 'DEPORTE', 'https://via.placeholder.com/50x50?text=B', true, NOW(), NOW()),
('Tenis', 'Artículos para tenis', 'DEPORTE', 'https://via.placeholder.com/50x50?text=T', true, NOW(), NOW()),
('Running', 'Artículos para correr', 'DEPORTE', 'https://via.placeholder.com/50x50?text=R', true, NOW(), NOW()),
('Fitness', 'Artículos para fitness', 'DEPORTE', 'https://via.placeholder.com/50x50?text=F', true, NOW(), NOW());

-- Insertar proveedores
INSERT INTO proveedores (nombre, email, telefono, direccion, activo, fecha_creacion, fecha_actualizacion) VALUES
('Nike México', 'contacto@nike.com.mx', '555-0101', 'Ciudad de México', true, NOW(), NOW()),
('Adidas México', 'contacto@adidas.com.mx', '555-0102', 'Guadalajara', true, NOW(), NOW()),
('Puma México', 'contacto@puma.com.mx', '555-0103', 'Monterrey', true, NOW(), NOW()),
('Under Armour México', 'contacto@underarmour.com.mx', '555-0104', 'Puebla', true, NOW(), NOW());

-- Insertar productos
INSERT INTO productos (nombre, descripcion, precio, stock, imagen_url, marca, modelo, talla, color, genero, material, peso, caracteristicas, activo, categoria_id, proveedor_id, fecha_creacion, fecha_actualizacion) VALUES
('Nike Air Max 270', 'Zapatillas deportivas Nike Air Max 270 con tecnología de amortiguación', 2499.00, 15, 'https://via.placeholder.com/300x300?text=Nike+Air+Max+270', 'Nike', 'Air Max 270', '28', 'Blanco', 'UNISEX', 'Mesh y sintético', 0.8, '["Amortiguación Air Max", "Suela de goma", "Respirable"]', true, 1, 1, NOW(), NOW()),
('Adidas Ultraboost 22', 'Zapatillas de running Adidas Ultraboost 22 con tecnología Boost', 3299.00, 12, 'https://via.placeholder.com/300x300?text=Adidas+Ultraboost+22', 'Adidas', 'Ultraboost 22', '27', 'Negro', 'UNISEX', 'Primeknit', 0.7, '["Tecnología Boost", "Primeknit", "Suela Continental"]', true, 4, 2, NOW(), NOW()),
('Balón de Fútbol Nike Strike', 'Balón oficial de fútbol Nike Strike para entrenamiento', 899.00, 25, 'https://via.placeholder.com/300x300?text=Balón+Nike+Strike', 'Nike', 'Strike', '5', 'Blanco/Negro', 'UNISEX', 'Cuero sintético', 0.4, '["Cámaras de aire", "Superficie texturizada", "Resistente al agua"]', true, 1, 1, NOW(), NOW()),
('Balón de Básquetbol Spalding', 'Balón oficial de básquetbol Spalding NBA', 1299.00, 18, 'https://via.placeholder.com/300x300?text=Balón+Spalding+NBA', 'Spalding', 'NBA Official', '7', 'Naranja', 'UNISEX', 'Cuero sintético', 0.6, '["Cámaras de aire", "Grip mejorado", "Durabilidad"]', true, 2, 3, NOW(), NOW()),
('Raqueta de Tenis Wilson', 'Raqueta de tenis Wilson Pro Staff para jugadores avanzados', 4599.00, 8, 'https://via.placeholder.com/300x300?text=Raqueta+Wilson+Pro+Staff', 'Wilson', 'Pro Staff', 'L', 'Negro', 'UNISEX', 'Grafito', 0.3, '["Tecnología Countervail", "Cuerdas incluidas", "Grip ergonómico"]', true, 3, 4, NOW(), NOW()),
('Camiseta Nike Dri-FIT', 'Camiseta deportiva Nike Dri-FIT para entrenamiento', 599.00, 30, 'https://via.placeholder.com/300x300?text=Camiseta+Nike+Dri-FIT', 'Nike', 'Dri-FIT', 'M', 'Azul', 'HOMBRE', 'Poliester', 0.2, '["Tecnología Dri-FIT", "Respirable", "Secado rápido"]', true, 5, 1, NOW(), NOW()),
('Shorts Adidas Climalite', 'Shorts deportivos Adidas Climalite para running', 799.00, 22, 'https://via.placeholder.com/300x300?text=Shorts+Adidas+Climalite', 'Adidas', 'Climalite', 'L', 'Negro', 'HOMBRE', 'Poliester', 0.15, '["Tecnología Climalite", "Bolsa interna", "Elástico ajustable"]', true, 4, 2, NOW(), NOW()),
('Zapatillas Puma RS-X', 'Zapatillas deportivas Puma RS-X con diseño retro', 1899.00, 14, 'https://via.placeholder.com/300x300?text=Puma+RS-X', 'Puma', 'RS-X', '29', 'Blanco/Rosa', 'MUJER', 'Sintético', 0.75, '["Diseño retro", "Suela de goma", "Comfortable"]', true, 5, 3, NOW(), NOW()),
('Mancuernas Ajustables', 'Par de mancuernas ajustables de 20kg cada una', 2999.00, 6, 'https://via.placeholder.com/300x300?text=Mancuernas+Ajustables', 'Generic', 'Ajustables 20kg', 'N/A', 'Negro', 'UNISEX', 'Hierro fundido', 20.0, '["Pesos ajustables", "Agarre ergonómico", "Seguro"]', true, 5, 4, NOW(), NOW()),
('Cinta de Correr ProForm', 'Cinta de correr ProForm con inclinación automática', 15999.00, 3, 'https://via.placeholder.com/300x300?text=Cinta+ProForm', 'ProForm', 'Smart 5.0', 'N/A', 'Negro', 'UNISEX', 'Acero', 85.0, '["Inclinación automática", "Pantalla LCD", "Programas preestablecidos"]', true, 5, 4, NOW(), NOW());
