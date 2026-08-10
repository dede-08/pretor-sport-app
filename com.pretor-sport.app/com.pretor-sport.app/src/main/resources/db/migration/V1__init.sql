-- =============================================
-- V1: Esquema inicial de Pretor Sport App
-- Espeja las entidades JPA (Hibernate 6.x / PostgreSQL)
-- =============================================

CREATE TABLE categorias (
    id                  bigserial       NOT NULL,
    nombre              varchar(50)     NOT NULL,
    descripcion         varchar(255),
    icono_url           varchar(255),
    tipo                varchar(20),
    orden               integer         NOT NULL,
    activa              boolean         NOT NULL,
    fecha_creacion      timestamp(6),
    fecha_actualizacion timestamp(6),
    CONSTRAINT pk_categorias PRIMARY KEY (id),
    CONSTRAINT uk_categorias_nombre UNIQUE (nombre)
);

CREATE INDEX idx_categoria_tipo ON categorias (tipo);
CREATE INDEX idx_categoria_activa ON categorias (activa);
CREATE INDEX idx_categoria_orden ON categorias (orden);

CREATE TABLE proveedores (
    id        bigserial   NOT NULL,
    nombre    varchar(100) NOT NULL,
    ruc       varchar(20),
    direccion varchar(255),
    telefono  varchar(20),
    email     varchar(100),
    CONSTRAINT pk_proveedores PRIMARY KEY (id)
);

CREATE TABLE usuarios (
    id                 bigserial    NOT NULL,
    nombre             varchar(100) NOT NULL,
    apellidos          varchar(100) NOT NULL,
    email              varchar(100) NOT NULL,
    password           varchar(255) NOT NULL,
    direccion          varchar(255),
    telefono           varchar(20),
    rol                varchar(255) NOT NULL,
    activo             boolean      NOT NULL,
    email_verificado   boolean      NOT NULL,
    token_verificacion varchar(255),
    ultimo_acceso      timestamp(6),
    fecha_registro     timestamp(6),
    fecha_actualizacion timestamp(6),
    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_email UNIQUE (email)
);

CREATE INDEX idx_usuario_email ON usuarios (email);
CREATE INDEX idx_usuario_rol ON usuarios (rol);
CREATE INDEX idx_usuario_activo ON usuarios (activo);

CREATE TABLE productos (
    id                  bigserial       NOT NULL,
    nombre              varchar(100)    NOT NULL,
    descripcion         varchar(1000),
    precio              numeric(10, 2)  NOT NULL,
    stock               integer         NOT NULL,
    imagen_url          varchar(255),
    marca               varchar(50),
    modelo              varchar(50),
    talla               varchar(20),
    color               varchar(30),
    genero              varchar(10),
    material            varchar(50),
    peso                numeric(6, 3),
    activo              boolean         NOT NULL,
    fecha_creacion      timestamp(6),
    fecha_actualizacion timestamp(6),
    categoria_id        bigint          NOT NULL,
    proveedor_id        bigint,
    CONSTRAINT pk_productos PRIMARY KEY (id),
    CONSTRAINT fk_productos_categoria FOREIGN KEY (categoria_id) REFERENCES categorias (id),
    CONSTRAINT fk_productos_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedores (id)
);

CREATE INDEX idx_producto_categoria ON productos (categoria_id);
CREATE INDEX idx_producto_precio ON productos (precio);
CREATE INDEX idx_producto_marca ON productos (marca);
CREATE INDEX idx_producto_genero ON productos (genero);

CREATE TABLE producto_caracteristicas (
    producto_id    bigint       NOT NULL,
    caracteristica varchar(255),
    CONSTRAINT fk_producto_caracteristicas_producto FOREIGN KEY (producto_id) REFERENCES productos (id)
);

CREATE TABLE pedidos (
    id             bigserial      NOT NULL,
    usuario_id     bigint         NOT NULL,
    fecha_creacion timestamp(6)   NOT NULL,
    estado         varchar(20)    NOT NULL,
    CONSTRAINT pk_pedidos PRIMARY KEY (id),
    CONSTRAINT fk_pedidos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE detalles_pedido (
    id              bigint        NOT NULL,
    pedido_id       bigint        NOT NULL,
    producto_id     bigint        NOT NULL,
    cantidad        integer       NOT NULL,
    precio_unitario numeric(10, 2) NOT NULL,
    CONSTRAINT pk_detalles_pedido PRIMARY KEY (id),
    CONSTRAINT fk_detalles_pedido_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos (id),
    CONSTRAINT fk_detalles_pedido_producto FOREIGN KEY (producto_id) REFERENCES productos (id)
);

CREATE TABLE pagos (
    id             bigint         NOT NULL,
    monto          numeric(10, 2) NOT NULL,
    fecha_pago     timestamp(6)   NOT NULL,
    metodo_pago    varchar(50)    NOT NULL,
    estado_pago    varchar(20)    NOT NULL,
    id_transaccion varchar(100),
    CONSTRAINT pk_pagos PRIMARY KEY (id)
);

CREATE TABLE ventas (
    id          bigint         NOT NULL,
    pedido_id   bigint         NOT NULL,
    pago_id     bigint,
    fecha_venta timestamp(6)   NOT NULL,
    total_venta numeric(10, 2) NOT NULL,
    CONSTRAINT pk_ventas PRIMARY KEY (id),
    CONSTRAINT fk_ventas_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos (id),
    CONSTRAINT uk_ventas_pedido UNIQUE (pedido_id),
    CONSTRAINT fk_ventas_pago FOREIGN KEY (pago_id) REFERENCES pagos (id)
);
