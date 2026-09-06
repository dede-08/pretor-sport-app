-- V2: Columna de version optimista para Producto (control de concurrencia de stock)
ALTER TABLE productos ADD COLUMN version bigint;

UPDATE productos SET version = 0;

ALTER TABLE productos ALTER COLUMN version SET NOT NULL;
