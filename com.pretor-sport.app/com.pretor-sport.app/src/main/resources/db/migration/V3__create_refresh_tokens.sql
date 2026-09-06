-- V3: Tabla de refresh tokens revocables y rotables (control total de sesiones)
CREATE TABLE refresh_tokens (
    id bigserial NOT NULL,
    usuario_id bigint NOT NULL,
    token_hash varchar(64) NOT NULL,
    expires_at timestamp(6) NOT NULL,
    revocado boolean NOT NULL,
    fecha_creacion timestamp(6) NOT NULL,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE INDEX idx_refresh_token_usuario ON refresh_tokens (usuario_id);

COMMENT ON TABLE refresh_tokens IS 'Refresh tokens emitidos, con hash, rotación y revocación (logout/single-use)';