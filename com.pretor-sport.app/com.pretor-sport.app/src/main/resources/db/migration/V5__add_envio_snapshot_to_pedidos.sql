-- =============================================
-- V5: Snapshot de envío en pedidos
-- Guarda los datos de entrega tal como estaban al cerrar la compra.
-- =============================================

ALTER TABLE pedidos
    ADD COLUMN nombre_envio        varchar(100),
    ADD COLUMN apellidos_envio     varchar(100),
    ADD COLUMN direccion_envio     varchar(255),
    ADD COLUMN ciudad_envio        varchar(100),
    ADD COLUMN codigo_postal_envio varchar(10),
    ADD COLUMN telefono_envio      varchar(20),
    ADD COLUMN email_envio         varchar(100),
    ADD COLUMN metodo_envio        varchar(20),
    ADD COLUMN costo_envio         numeric(10, 2),
    ADD COLUMN notas               varchar(500);
