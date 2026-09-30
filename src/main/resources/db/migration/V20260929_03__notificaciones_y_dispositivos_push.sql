CREATE TABLE IF NOT EXISTS notificaciones (
 id BIGSERIAL PRIMARY KEY,
 usuario_id BIGINT NOT NULL REFERENCES usuario(id),
 event_key VARCHAR(160) NOT NULL,
 tipo VARCHAR(40) NOT NULL,
 titulo VARCHAR(120) NOT NULL,
 mensaje VARCHAR(500) NOT NULL,
 destino_tipo VARCHAR(30),
 destino_id BIGINT,
 fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 leida BOOLEAN NOT NULL DEFAULT FALSE,
 entrega_estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
 intentos INTEGER NOT NULL DEFAULT 0,
 next_attempt_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 entrega_error VARCHAR(300),
 CONSTRAINT uk_notificacion_evento_usuario UNIQUE(usuario_id,event_key)
);
CREATE INDEX IF NOT EXISTS idx_notificacion_usuario_fecha ON notificaciones(usuario_id,fecha_creacion DESC);
CREATE INDEX IF NOT EXISTS idx_notificacion_entrega ON notificaciones(entrega_estado,next_attempt_at);
CREATE TABLE IF NOT EXISTS dispositivos_push (
 id BIGSERIAL PRIMARY KEY,
 usuario_id BIGINT NOT NULL REFERENCES usuario(id),
 token VARCHAR(500) NOT NULL UNIQUE,
 actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_dispositivo_usuario ON dispositivos_push(usuario_id);
