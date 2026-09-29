-- Vinculacion conservadora para instalaciones nuevas.
-- El correo identifica candidatos; nombre y apellido deben ser compatibles.
ALTER TABLE docentes ADD COLUMN IF NOT EXISTS usuario_id BIGINT;
ALTER TABLE estudiantes ADD COLUMN IF NOT EXISTS usuario_id BIGINT;
CREATE UNIQUE INDEX IF NOT EXISTS uq_docentes_usuario_id ON docentes(usuario_id) WHERE usuario_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_estudiantes_usuario_id ON estudiantes(usuario_id) WHERE usuario_id IS NOT NULL;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_docentes_usuario') THEN
    ALTER TABLE docentes ADD CONSTRAINT fk_docentes_usuario FOREIGN KEY(usuario_id) REFERENCES usuario(id);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_estudiantes_usuario') THEN
    ALTER TABLE estudiantes ADD CONSTRAINT fk_estudiantes_usuario FOREIGN KEY(usuario_id) REFERENCES usuario(id);
  END IF;
END $$;

CREATE TABLE IF NOT EXISTS vinculacion_identidad_revision (
  id BIGSERIAL PRIMARY KEY,
  tipo_perfil VARCHAR(20) NOT NULL,
  perfil_id BIGINT NOT NULL,
  usuario_id BIGINT NOT NULL,
  email_usuario VARCHAR(150), email_perfil VARCHAR(150),
  nombre_usuario VARCHAR(120), nombre_perfil VARCHAR(120),
  apellido_usuario VARCHAR(120), apellido_perfil VARCHAR(120),
  correo_coincide BOOLEAN NOT NULL,
  nombre_coincide BOOLEAN NOT NULL,
  apellido_coincide BOOLEAN NOT NULL,
  motivo VARCHAR(250) NOT NULL,
  resuelto BOOLEAN NOT NULL DEFAULT FALSE,
  detectado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(tipo_perfil,perfil_id,usuario_id)
);

WITH candidatos AS (
 SELECT d.id perfil_id,MIN(u.id) usuario_id
 FROM docentes d JOIN usuario u ON LOWER(TRIM(u.email))=LOWER(TRIM(d.email))
 JOIN usuarios_roles ur ON ur.usuario_id=u.id JOIN roles r ON r.id=ur.rol_id
 WHERE d.usuario_id IS NULL AND r.activo=true AND UPPER(r.codigo)='DOCENTE'
   AND LOWER(REGEXP_REPLACE(TRIM(u.nombre),'\s+',' ','g'))=LOWER(REGEXP_REPLACE(TRIM(d.nombre),'\s+',' ','g'))
   AND LOWER(REGEXP_REPLACE(TRIM(u.apellido),'\s+',' ','g'))=LOWER(REGEXP_REPLACE(TRIM(d.apellido),'\s+',' ','g'))
   AND NOT EXISTS(SELECT 1 FROM docentes x WHERE x.usuario_id=u.id)
 GROUP BY d.id,d.email,d.nombre,d.apellido HAVING COUNT(DISTINCT u.id)=1
   AND (SELECT COUNT(*) FROM docentes x WHERE LOWER(TRIM(x.email))=LOWER(TRIM(d.email)))=1
)
UPDATE docentes d SET usuario_id=c.usuario_id FROM candidatos c WHERE d.id=c.perfil_id;

WITH candidatos AS (
 SELECT e.id perfil_id,MIN(u.id) usuario_id
 FROM estudiantes e JOIN usuario u ON LOWER(TRIM(u.email))=LOWER(TRIM(e.correo))
 JOIN usuarios_roles ur ON ur.usuario_id=u.id JOIN roles r ON r.id=ur.rol_id
 WHERE e.usuario_id IS NULL AND r.activo=true AND UPPER(r.codigo)='ESTUDIANTE'
   AND LOWER(REGEXP_REPLACE(TRIM(u.nombre),'\s+',' ','g'))=LOWER(REGEXP_REPLACE(TRIM(e.nombres),'\s+',' ','g'))
   AND LOWER(REGEXP_REPLACE(TRIM(u.apellido),'\s+',' ','g'))=LOWER(REGEXP_REPLACE(TRIM(e.apellidos),'\s+',' ','g'))
   AND NOT EXISTS(SELECT 1 FROM estudiantes x WHERE x.usuario_id=u.id)
 GROUP BY e.id,e.correo,e.nombres,e.apellidos HAVING COUNT(DISTINCT u.id)=1
   AND (SELECT COUNT(*) FROM estudiantes x WHERE LOWER(TRIM(x.correo))=LOWER(TRIM(e.correo)))=1
)
UPDATE estudiantes e SET usuario_id=c.usuario_id FROM candidatos c WHERE e.id=c.perfil_id;

-- El mismo correo con nombres contradictorios se reporta, nunca se vincula.
INSERT INTO vinculacion_identidad_revision(tipo_perfil,perfil_id,usuario_id,email_usuario,email_perfil,
 nombre_usuario,nombre_perfil,apellido_usuario,apellido_perfil,correo_coincide,nombre_coincide,apellido_coincide,motivo)
SELECT 'DOCENTE',d.id,u.id,u.email,d.email,u.nombre,d.nombre,u.apellido,d.apellido,true,
 LOWER(REGEXP_REPLACE(TRIM(u.nombre),'\s+',' ','g'))=LOWER(REGEXP_REPLACE(TRIM(d.nombre),'\s+',' ','g')),
 LOWER(REGEXP_REPLACE(TRIM(u.apellido),'\s+',' ','g'))=LOWER(REGEXP_REPLACE(TRIM(d.apellido),'\s+',' ','g')),
 'CORREO_COINCIDE_PERO_IDENTIDAD_CONTRADICTORIA'
FROM docentes d JOIN usuario u ON LOWER(TRIM(u.email))=LOWER(TRIM(d.email))
WHERE d.usuario_id IS NULL AND (LOWER(REGEXP_REPLACE(TRIM(u.nombre),'\s+',' ','g'))<>LOWER(REGEXP_REPLACE(TRIM(d.nombre),'\s+',' ','g')) OR LOWER(REGEXP_REPLACE(TRIM(u.apellido),'\s+',' ','g'))<>LOWER(REGEXP_REPLACE(TRIM(d.apellido),'\s+',' ','g')))
ON CONFLICT(tipo_perfil,perfil_id,usuario_id) DO NOTHING;

INSERT INTO vinculacion_identidad_revision(tipo_perfil,perfil_id,usuario_id,email_usuario,email_perfil,
 nombre_usuario,nombre_perfil,apellido_usuario,apellido_perfil,correo_coincide,nombre_coincide,apellido_coincide,motivo)
SELECT 'ESTUDIANTE',e.id,u.id,u.email,e.correo,u.nombre,e.nombres,u.apellido,e.apellidos,true,
 LOWER(REGEXP_REPLACE(TRIM(u.nombre),'\s+',' ','g'))=LOWER(REGEXP_REPLACE(TRIM(e.nombres),'\s+',' ','g')),
 LOWER(REGEXP_REPLACE(TRIM(u.apellido),'\s+',' ','g'))=LOWER(REGEXP_REPLACE(TRIM(e.apellidos),'\s+',' ','g')),
 'CORREO_COINCIDE_PERO_IDENTIDAD_CONTRADICTORIA'
FROM estudiantes e JOIN usuario u ON LOWER(TRIM(u.email))=LOWER(TRIM(e.correo))
WHERE e.usuario_id IS NULL AND (LOWER(REGEXP_REPLACE(TRIM(u.nombre),'\s+',' ','g'))<>LOWER(REGEXP_REPLACE(TRIM(e.nombres),'\s+',' ','g')) OR LOWER(REGEXP_REPLACE(TRIM(u.apellido),'\s+',' ','g'))<>LOWER(REGEXP_REPLACE(TRIM(e.apellidos),'\s+',' ','g')))
ON CONFLICT(tipo_perfil,perfil_id,usuario_id) DO NOTHING;
