# Login web cifrado con JWE

## Contrato

La web solicita `GET /api/auth/encryption`. La respuesta, con `Cache-Control: no-store`, contiene `kid`, un JWK **público** RSA, un desafío aleatorio y `expiresAt`. Después envía a `POST /api/auth/login/encrypted` solamente `{ "encryptedCredentials": "<JWE compacto>" }`.

El contenido cifrado es `{ "username": "...", "password": "...", "challenge": "..." }`. Se aceptan exclusivamente `alg=RSA-OAEP-256`, `enc=A256GCM` y el `kid` activo. El desafío dura como máximo cinco minutos, se guarda sólo como SHA-256 en PostgreSQL y se consume mediante una actualización condicional atómica. Por ello, el uso único funciona entre varias instancias de Cloud Run.

`POST /api/auth/login` sigue disponible para Android y clientes antiguos durante su migración. Esos clientes conservan el contrato anterior y **no** debe afirmarse que ya usan JWE.

## Secretos y despliegue

La clave JWE es independiente de `JWT_SECRET`. Crear un RSA de al menos 2048 bits y almacenar su JWK privado completo en Google Secret Manager; no guardarlo en Git, respuestas, logs ni variables `VITE_*`. Inyectar `LOGIN_ENCRYPTION_ENABLED=true`, `LOGIN_ENCRYPTION_PRIVATE_JWK` desde Secret Manager, `LOGIN_ENCRYPTION_KID` como identificador estable de la versión y, opcionalmente, `LOGIN_CHALLENGE_TTL_SECONDS=120` (30--300).

Desplegar primero el backend, comprobar por HTTPS la publicación del JWK público y un login cifrado, y sólo entonces desplegar el frontend. El endpoint histórico permanece hasta migrar Android.

## Rotación

Crear una clave privada nueva y un `kid` nuevo. Para rotación sin interrupciones, desplegar temporalmente soporte de descifrado para ambos `kid`, publicar sólo el nuevo y conservar el anterior durante al menos el TTL más el tiempo máximo de despliegue antes de retirarlo.

Los logs de acceso deben registrar ruta, estado y correlación, nunca cuerpos. La auditoría omite ambos endpoints de login; las respuestas de error son genéricas y no incluyen credenciales, hashes, JWE ni JWT.
