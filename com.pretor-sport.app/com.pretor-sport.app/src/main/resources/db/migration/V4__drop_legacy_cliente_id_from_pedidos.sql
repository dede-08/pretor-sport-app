-- V4: elimina la columna heredada cliente_id de pedidos.
-- El modelo actual relaciona Pedido con Usuario mediante usuario_id. cliente_id
-- procedía de un esquema antiguo (tabla clientes) que ya no se usa, era NOT NULL y
-- rompía el INSERT de pedidos (DataIntegrityViolation) en bases creadas por el
-- esquema antiguo de Hibernate.
ALTER TABLE pedidos DROP COLUMN IF EXISTS cliente_id CASCADE;
