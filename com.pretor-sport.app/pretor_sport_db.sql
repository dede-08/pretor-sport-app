-- Database: pretor_sport_db
-- DROP DATABASE IF EXISTS pretor_sport_db;

CREATE DATABASE pretor_sport_db
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Spanish_Peru.1252'
    LC_CTYPE = 'Spanish_Peru.1252'
    LOCALE_PROVIDER = 'libc'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = False;

SELECT * FROM productos;
SELECT * FROM Usuarios;
SELECT * FROM categorias;
SELECT * FROM proveedores;

INSERT INTO proveedores (direccion, email, nombre, ruc, telefono)
VALUES ('Av. Los Maestros 23423', 'adidas@sport.com', 'Adidas', '5352-AD', '982647182');

INSERT INTO categorias (activa, descripcion, fecha_actualizacion, fecha_creacion, icono_url, nombre, orden, tipo)
VALUES (TRUE, 'Zapatillas deportivas', NOW(), NOW(), 'Zapatillas.png', 'Zapatillas', 2, 'EQUIPAMIENTO');