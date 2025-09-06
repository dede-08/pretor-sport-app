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

SELECT * FROM Clientes;