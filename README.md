# SGAU Backend API

API REST para un Sistema de Gestion Educativa desarrollada con Java, Spring Boot, Spring Data JPA y PostgreSQL.

El sistema permite administrar usuarios, estudiantes, docentes, carreras, cursos, inscripciones, colegiaturas, notas, roles y permisos. Fue desarrollado como parte del curso Desarrollo Web I y aplica arquitectura modular Package by Feature, separacion de responsabilidades, DTO, Mapper, validaciones, relaciones JPA, Soft Delete, BCrypt, JWT y control de acceso basado en roles y permisos.

## Tecnologias utilizadas

- Java 17
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA
- Spring Security
- JWT con `io.jsonwebtoken`
- PostgreSQL
- Maven Wrapper
- Lombok
- Jakarta Bean Validation
- BCrypt
- Springdoc OpenAPI / Swagger UI
- H2 para pruebas
- Git y GitHub
- Postman
- Neon PostgreSQL
- Google Cloud Run

## Arquitectura del proyecto

El proyecto utiliza **Package by Feature**: cada dominio concentra sus controladores, DTO, entidades, repositorios, servicios, mappers y excepciones. Esto reduce acoplamiento, facilita el mantenimiento y permite que cada modulo evolucione de forma independiente.

```text
src/main/java/com/umg/sgau/
|-- auth/
|-- config/
|-- usuario/
|-- estudiante/
|-- docente/
|-- carrera/
|-- curso/
|-- inscripcion/
|-- colegiatura/
|-- nota/
|-- rol/
|-- permiso/
|-- historialacademico/
|-- notificacion/
`-- estadogeneral/
```

Estructura usada dentro de los dominios principales:

```text
carrera/
|-- controller/
|-- dto/
|-- entity/
|-- exception/
|-- mapper/
|-- repository/
|-- service/
`-- serviceimpl/
```

| Componente | Responsabilidad |
|------------|-----------------|
| Controller | Recibe peticiones HTTP y devuelve respuestas REST |
| DTO | Define datos de entrada y salida sin exponer entidades |
| Mapper | Convierte DTO a Entity y Entity a DTO |
| Entity | Representa tablas, columnas y relaciones JPA |
| Repository | Accede a PostgreSQL mediante Spring Data JPA |
| Service | Define contratos de negocio |
| ServiceImpl | Implementa reglas de negocio, validaciones y persistencia |
| Exception | Representa errores especificos del dominio |
| ExceptionHandler | Convierte excepciones en respuestas HTTP |

## Flujo de una peticion

```text
Cliente / Postman
  |
  v
Controller
  |
  v
RequestDTO
  |
  v
Mapper
  |
  v
Entity
  |
  v
Service / ServiceImpl
  |
  v
Repository
  |
  v
PostgreSQL
  |
  v
Entity
  |
  v
Mapper
  |
  v
ResponseDTO
  |
  v
ResponseEntity / JSON
```

El Controller no accede directamente al Repository. La validacion de reglas de negocio, entidades relacionadas, duplicados y Soft Delete se mantiene en la capa ServiceImpl.

## Notificaciones y push (FCM)

Las notificaciones de buzón se escriben en `notificaciones` dentro de la misma transacción que confirma el evento académico/financiero (outbox transaccional). Un proceso programado intenta FCM en segundo plano hasta cinco veces con espera incremental; el error de Firebase no revierte la operación original. `event_key` es único por usuario. Borrar/vaciar elimina también cualquier entrega aún pendiente. Un mensaje FCM contiene solamente identificadores de destino; el Android consulta el buzón con la sesión vigente antes de navegar. El texto de la notificación visible es genérico para no divulgar notas ni pagos.

Configura `FIREBASE_PROJECT_ID` y `GOOGLE_APPLICATION_CREDENTIALS` en el entorno del servidor (el segundo apunta a una credencial de cuenta de servicio montada como secreto; nunca se debe guardar en el repositorio). Sin configuración/credencial de Admin, el buzón sigue funcionando, pero no se envía push. En Android se requiere `app/google-services.json` correspondiente al proyecto Firebase y permiso de notificaciones en Android 13 o posterior.

Todos los endpoints siguientes requieren `Authorization: Bearer <JWT>`. El usuario se resuelve desde el JWT y no se acepta un `usuarioId` del cliente:

| Método y ruta | Uso |
|---|---|
| `GET /api/notificaciones/me?page=0&size=20` | Buzón paginado (máximo 100 por página). |
| `GET /api/notificaciones/me/count` | `{ "noLeidas": 2 }`. |
| `GET /api/notificaciones/me/{id}` | Consultar detalle propio; `404` si no existe/no pertenece. |
| `PATCH /api/notificaciones/me/{id}/leida` | Marcar una propia como leída. |
| `PATCH /api/notificaciones/me/leidas` | Marcar todas las propias; `{ "actualizadas": 2 }`. |
| `DELETE /api/notificaciones/me/{id}` | Eliminar una propia; `204` al éxito. |
| `DELETE /api/notificaciones/me` | Vaciar solo el buzón propio; `{ "eliminadas": 2 }`. |
| `POST /api/notificaciones/me/dispositivos` | Registrar/rotar token: `{ "token": "<FCM>" }`; respuesta `204`. |
| `DELETE /api/notificaciones/me/dispositivos?token=<FCM>` | Desvincular el dispositivo de la cuenta autenticada. |

Ejemplo de elemento de buzón: `{ "id": 45, "tipo": "PAGO_PENDIENTE", "titulo": "Pago registrado", "mensaje": "Tu pago quedó pendiente de revisión.", "destinoTipo": "COLEGIATURA", "destinoId": 12, "fechaCreacion": "2026-09-29T10:30:00", "leida": false }`. Los errores de validación de token usan el manejador estándar de Bean Validation (`400`); una notificación ajena o no disponible da `404`, sin revelar su existencia.

Los eventos conectados son inscripción confirmada, nota creada/modificada, asignación docente, colegiatura generada y pago declarado pendiente / aprobado / rechazado. Los pagos pendientes se distinguen de pagos confirmados; solo administradores activos con permiso de registro de pagos reciben avisos de revisión. Los resultados de envío se registran como `PENDIENTE`, `SIN_DISPOSITIVO`, `ERROR` o `ENVIADO`; los tokens que FCM identifica como no registrados se eliminan.

## Relaciones entre entidades

Relaciones JPA implementadas actualmente:

```text
Carrera     1 ----- N Curso
Docente     1 ----- N Curso

Estudiante  1 ----- N Inscripcion
Carrera     1 ----- N Inscripcion
Curso       1 ----- N Inscripcion

Estudiante  1 ----- N Colegiatura

Estudiante  1 ----- N Nota
Curso       1 ----- N Nota

Usuario     N ----- N Rol
Rol         N ----- N Permiso
```

Las relaciones uno a muchos se implementan con `@ManyToOne`, `@OneToMany` y `@JoinColumn`. Las relaciones muchos a muchos usan `@ManyToMany` y tablas intermedias.

Tablas intermedias:

- `usuarios_roles`: asigna uno o varios roles a un usuario.
- `roles_permisos`: asigna varios permisos a cada rol.

Ejemplo real de la relacion Carrera-Curso en `Curso`:

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "carrera_id", nullable = false)
private Carrera carrera;
```

Cada Curso pertenece a una Carrera, mientras que una Carrera puede contener varios Cursos.

## Relaciones y DTO

Aunque las entidades usan relaciones JPA, la API recibe IDs en los DTO. No se reciben objetos `Carrera`, `Docente`, `Estudiante` o `Curso` completos desde JSON.

Ejemplo para crear curso:

```json
{
  "codigo": "PROG-01",
  "nombre": "Programacion I",
  "descripcion": "Curso introductorio de programacion",
  "creditos": 5,
  "horasSemanales": 6,
  "carreraId": 1,
  "cicloAnio": 2026
}
```

Flujo interno:

```text
carreraId = 1
  |
  v
CarreraRepository / CarreraService.findById(1)
  |
  v
Carrera Entity
  |
  v
Curso.setCarrera(carrera)
```

## Integridad referencial

Los ServiceImpl validan entidades relacionadas antes de guardar. Si un ID no existe o la entidad relacionada esta inactiva, se lanza una excepcion del dominio.

Ejemplos:

- No se puede crear una Nota con `estudianteId` inexistente.
- No se puede crear una Nota con `cursoId` inexistente.
- No se puede crear un Curso con `carreraId` inexistente.
- No se puede crear una Colegiatura con `estudianteId` inexistente.
- No se puede asignar un Rol o Permiso inactivo.

Patron utilizado:

```java
findById(id).orElseThrow(...)
```

## Sistema de roles y permisos

Modelo de autorizacion:

```text
Usuario
  |
  v
Rol
  |
  v
Permiso
```

Los roles se almacenan dinamicamente en la tabla `roles`; no estan limitados a un enum fijo. El codigo permite roles como `ADMIN`, `DOCENTE` o `ESTUDIANTE` si existen en base de datos.

Cada permiso representa una accion especifica, por ejemplo:

- `USUARIO_CREAR`
- `USUARIO_CONSULTAR`
- `CURSO_CREAR`
- `CURSO_CONSULTAR`
- `NOTA_CREAR`
- `NOTA_EDITAR`

Un rol puede tener varios permisos y un mismo permiso puede pertenecer a varios roles.

## Autoridades en Spring Security

`CustomUserDetailsService` transforma roles y permisos activos en authorities:

```text
ROLE_ADMIN
USUARIO_CREAR
USUARIO_EDITAR
NOTA_CONSULTAR
```

`SecurityConfig` habilita seguridad stateless, JWT, CORS, `@EnableMethodSecurity`, BCrypt y handlers personalizados para 401 y 403. Actualmente `/api/auth/login`, Swagger y OpenAPI son publicos; el resto requiere autenticacion. Las mutaciones de `/api/usuarios/**` requieren rol `ADMIN`.

## Soft Delete

El sistema no elimina fisicamente la mayoria de registros. En su lugar utiliza el campo:

```text
activo
```

- `activo = true`: registro disponible.
- `activo = false`: registro inhabilitado.

Esto conserva historial, evita perdida de informacion y protege la integridad referencial entre dominios.

## Uso de Streams

El proyecto usa Streams para filtrar, transformar y resumir colecciones:

- `filter()`: filtra activos, pendientes o resultados especificos.
- `map()`: convierte entidades a DTO o extrae campos.
- `collect()`: agrupa resultados en listas, sets o mapas.
- `reduce()`: calcula totales, por ejemplo saldos de colegiaturas.

Ejemplo:

```java
repository.findAll()
        .stream()
        .filter(item -> Boolean.TRUE.equals(item.getActivo()))
        .collect(Collectors.toList());
```

## Seguridad de contrasenas

Las contrasenas de Usuario se almacenan cifradas con BCrypt:

```java
passwordEncoder.encode(password)
```

Y se verifican mediante Spring Security:

```java
passwordEncoder.matches(passwordPlano, passwordCifrado)
```

La contrasena no se expone en `UsuarioResponseDTO`, respuestas JSON, logs ni documentacion. El modulo Usuario cifra la contrasena en `UsuarioServiceImpl` y devuelve DTO sin el campo `password`.

## Autenticacion JWT

JWT esta implementado en el proyecto actual.

Endpoints:

```http
POST /api/auth/register
POST /api/auth/login
GET  /api/auth/me
```

Flujo:

1. El cliente puede crear una cuenta inicial en `/api/auth/register`.
2. El usuario queda activo, sin roles y sin permisos.
3. Un administrador asigna roles mediante `/api/usuarios/{id}/roles`.
4. El cliente envia `username` y `password` a `/api/auth/login`.
5. Spring Security autentica contra `CustomUserDetailsService`.
6. `AuthService` genera un token con `JwtService`.
7. El token incluye `userId`, `email` y `authorities`.
8. Las siguientes peticiones usan el header:

```http
Authorization: Bearer <token>
```

Ejemplo de registro publico:

```json
{
  "username": "nuevo.usuario",
  "password": "Usuario123*",
  "email": "nuevo.usuario@sgau.local",
  "nombre": "Nuevo",
  "apellido": "Usuario"
}
```

Ejemplo de login:

```json
{
  "username": "admin",
  "password": "password-plano"
}
```

Respuesta:

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "usuarioId": 1,
  "username": "admin",
  "nombre": "Administrador",
  "apellido": "Sistema",
  "roles": ["ADMIN"]
}
```

## Principales endpoints

### Consultas academicas propias

Estas rutas resuelven el registro academico comparando el correo del usuario autenticado con
`docentes.email` o `estudiantes.correo`. Nunca reciben un ID de docente o estudiante elegido por
el cliente. Si no existe el vinculo responden `404` con
`code: VINCULACION_ACADEMICA_NO_ENCONTRADA`; una coleccion sin datos responde `200` vacia.

| Rol | Ruta | Permiso |
|---|---|---|
| DOCENTE | `GET /api/academico/docente/me` | `CURSOS_LEER` |
| DOCENTE | `GET /api/academico/docente/me/cursos` | `CURSOS_LEER` |
| DOCENTE | `GET /api/academico/docente/me/cursos/{cursoId}/estudiantes` | `INSCRIPCIONES_LEER` |
| DOCENTE | `GET /api/academico/docente/me/cursos/{cursoId}/notas` | `NOTAS_LEER` |
| ESTUDIANTE | `GET /api/academico/estudiante/me` | `ESTUDIANTES_LEER` |
| ESTUDIANTE | `GET /api/academico/estudiante/me/inscripciones` | `INSCRIPCIONES_LEER` |
| ESTUDIANTE | `GET /api/academico/estudiante/me/cursos` | `CURSOS_LEER` |
| ESTUDIANTE | `GET /api/academico/estudiante/me/notas` | `NOTAS_LEER` |
| ESTUDIANTE | `GET /api/academico/estudiante/me/promedio` | `NOTAS_LEER` |
| ESTUDIANTE | `GET /api/academico/estudiante/me/colegiaturas` | `COLEGIATURAS_LEER` |
| ESTUDIANTE | `GET /api/academico/estudiante/me/estado-cuenta` | `COLEGIATURAS_LEER` |

Los cursos incluyen `carreraId`, `carreraCodigo` y `carreraNombre`. Las inscripciones y notas
incluyen nombres legibles de curso y estudiante. El modelo persistente no contiene horario, por
lo que la API no publica un campo ficticio.

Ejemplo de curso propio:

```json
{
  "id": 12,
  "codigo": "PROG-01",
  "nombre": "Programacion I",
  "creditos": 5,
  "horasSemanales": 6,
  "cicloAnio": 2026,
  "activo": true,
  "carreraId": 2,
  "carreraCodigo": "ISC",
  "carreraNombre": "Ingenieria en Sistemas"
}
```

### Auditoria

`GET /api/auditoria` es paginado y admite `desde`, `hasta`, `usuarioId`, `username`, `modulo`,
`accion`, `tipoEntidad` y `entidadId`. Requiere `AUDITORIA_LEER`, permiso agregado al catalogo y
asignado de forma aditiva solamente a `ADMIN`. La API no expone operaciones de escritura sobre
eventos.

Las operaciones HTTP `POST`, `PUT`, `PATCH` y `DELETE` confirmadas por los controladores se
registran una sola vez. Login queda excluido porque no cambia datos. Passwords, tokens,
Authorization, secretos y credenciales se eliminan recursivamente de los cambios serializados.
Para registros existentes, `cambiosAntes` y `cambiosDespues` contienen snapshots escalares y los
IDs de sus relaciones; para creaciones, `cambiosAntes` contiene la solicitud sanitizada.
El registro ocurre al regresar exitosamente la operacion de negocio, cuando su transaccion ya
termino, y usa `REQUIRES_NEW`. Si la escritura de auditoria falla, se registra el error tecnico sin
revertir ni dejar a medias el cambio de negocio ya confirmado. Las operaciones fallidas no crean
un evento `EXITOSO`.

El esquema vigente se administra con `spring.jpa.hibernate.ddl-auto=update`; la entidad
`AuditoriaEvento` crea la tabla e indices. Para ambientes que aplican DDL manual existe el script
de referencia `src/main/resources/db/migration-manual/V1__create_auditoria_eventos.sql`, ubicado
fuera del directorio automatico de Flyway porque este proyecto no usa Flyway.

### Autenticacion

| Metodo | Ruta | Uso |
|--------|------|-----|
| POST | `/api/auth/register` | Registrar usuario sin roles |
| POST | `/api/auth/login` | Iniciar sesion y obtener JWT |
| GET | `/api/auth/me` | Obtener perfil autenticado |

### Usuarios, roles y permisos

| Dominio | Metodo | Ruta |
|---------|--------|------|
| Usuario | POST | `/api/usuarios` |
| Usuario | GET | `/api/usuarios` |
| Usuario | GET | `/api/usuarios/{id}` |
| Usuario | PUT | `/api/usuarios/{id}` |
| Usuario | DELETE | `/api/usuarios/{id}` |
| Usuario | PUT | `/api/usuarios/{id}/roles` |
| Usuario | GET | `/api/usuarios/{id}/roles` |
| Rol | POST | `/api/roles` |
| Rol | GET | `/api/roles` |
| Rol | GET | `/api/roles/{id}` |
| Rol | PUT | `/api/roles/{id}` |
| Rol | PATCH | `/api/roles/{id}/estado` |
| Rol | PUT | `/api/roles/{id}/permisos` |
| Rol | GET | `/api/roles/{id}/permisos` |
| Permiso | POST | `/api/permisos` |
| Permiso | GET | `/api/permisos` |
| Permiso | GET | `/api/permisos/{id}` |
| Permiso | PUT | `/api/permisos/{id}` |
| Permiso | PATCH | `/api/permisos/{id}/estado` |
| Permiso | GET | `/api/permisos/activos` |

### Dominios academicos

| Dominio | Rutas principales |
|---------|-------------------|
| Estudiantes | `POST /api/estudiantes`, `GET /api/estudiantes`, `GET /api/estudiantes/{id}`, `PUT /api/estudiantes/{id}`, `PATCH /api/estudiantes/{id}/estado` |
| Docentes | `POST /api/docentes`, `GET /api/docentes`, `GET /api/docentes/{id}`, `PUT /api/docentes/{id}`, `PATCH /api/docentes/{id}/estado`, `DELETE /api/docentes/{id}` |
| Carreras | `POST /api/carreras`, `GET /api/carreras`, `GET /api/carreras/{id}`, `PUT /api/carreras/{id}`, `PATCH /api/carreras/{id}/estado` |
| Cursos | `POST /api/cursos`, `GET /api/cursos`, `GET /api/cursos/{id}`, `PUT /api/cursos/{id}`, `PATCH /api/cursos/{id}/estado`, `PATCH /api/cursos/{id}/docente` |
| Inscripciones | `POST /api/inscripciones`, `GET /api/inscripciones`, `GET /api/inscripciones/{id}`, `PUT /api/inscripciones/{id}`, `PATCH /api/inscripciones/{id}/estado`, `PATCH /api/inscripciones/{id}/reactivar` |
| Colegiaturas | `POST /api/colegiaturas`, `GET /api/colegiaturas`, `GET /api/colegiaturas/{id}`, `PUT /api/colegiaturas/{id}`, `PATCH /api/colegiaturas/{id}/pago`, `PATCH /api/colegiaturas/{id}/estado` |
| Notas | `POST /api/notas`, `GET /api/notas`, `GET /api/notas/{id}`, `PUT /api/notas/{id}`, `PATCH /api/notas/{id}/estado` |
| Reportes de estudiante | `GET /api/estudiantes/{id}/resumen`, `GET /api/estudiantes/{id}/historial-academico`, `GET /api/estudiantes/{id}/estado-general` |

## Ejemplos para Postman

Crear carrera:

```json
{
  "codigo": "ISC",
  "nombre": "Ingenieria en Sistemas",
  "descripcion": "Carrera de tecnologia",
  "duracionAnios": 5
}
```

Crear curso:

```json
{
  "codigo": "PROG-01",
  "nombre": "Programacion I",
  "descripcion": "Fundamentos de programacion",
  "creditos": 5,
  "horasSemanales": 6,
  "carreraId": 1,
  "cicloAnio": 2026
}
```

Crear nota:

```json
{
  "estudianteId": 1,
  "cursoId": 1,
  "cicloAnio": 2026,
  "tipoEvaluacion": "PARCIAL_1",
  "calificacion": 88.50,
  "observaciones": "Evaluacion aprobada"
}
```

Crear colegiatura:

```json
{
  "estudianteId": 1,
  "cicloAnio": 2026,
  "concepto": "Mensualidad enero",
  "montoTotal": 450.00,
  "fechaEmision": "2026-01-01",
  "fechaVencimiento": "2026-01-31"
}
```

Asignar permisos a rol:

```json
{
  "permisoIds": [1, 2, 3]
}
```

Asignar roles a usuario:

```json
{
  "rolIds": [1]
}
```

## Base de datos

El proyecto utiliza PostgreSQL. Para despliegue se usa PostgreSQL administrado mediante Neon. No se deben versionar host real, usuario real, password ni connection string productivo.

Tablas principales segun entidades actuales:

- `usuario`
- `estudiantes`
- `docentes`
- `carreras`
- `cursos`
- `inscripciones`
- `colegiaturas`
- `notas`
- `roles`
- `permisos`
- `usuarios_roles`
- `roles_permisos`

Diagrama textual:

```text
usuario
  |
  `-- usuarios_roles -- roles
                         |
                         `-- roles_permisos -- permisos

carreras ---< cursos >--- docentes
   |           |
   |           `---< notas >--- estudiantes
   |                              |
   `---< inscripciones >----------`
                                  |
                                  `---< colegiaturas
```

Configuracion de PostgreSQL:

```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
```

## Variables de entorno

| Variable | Descripcion |
|----------|-------------|
| `DATABASE_URL` | URL JDBC de PostgreSQL |
| `DATABASE_USERNAME` | Usuario PostgreSQL |
| `DATABASE_PASSWORD` | Contrasena PostgreSQL |
| `PORT` | Puerto usado por la aplicacion; por defecto `8080` |
| `JWT_SECRET` | Secreto para firmar JWT; debe tener al menos 32 bytes |
| `JWT_EXPIRATION` | Duracion del JWT en milisegundos; por defecto `3600000` |
| `JWT_ISSUER` | Emisor del token; por defecto `sgau-backend-api` |
| `CORS_ALLOWED_ORIGINS` | Origenes permitidos separados por coma |

## Ejecucion local

Requisitos:

- JDK 17.
- PostgreSQL disponible.
- Variables de entorno configuradas.
- Maven Wrapper incluido en el repositorio.

Windows:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw clean test
./mvnw spring-boot:run
```

La aplicacion escucha en:

```properties
server.port=${PORT:8080}
server.address=0.0.0.0
```

Por defecto usa el puerto `8080` si `PORT` no esta definido.

## Pruebas

El repositorio incluye pruebas automatizadas en `src/test/java` para servicios y controladores de dominios principales, ademas de pruebas para JWT y autenticacion.

Se prueban o se deben cubrir en Postman y tests automatizados:

- CRUD.
- Validaciones.
- Duplicados.
- Soft Delete.
- Relaciones JPA.
- IDs inexistentes.
- Paginacion y filtros.
- Roles y permisos.
- BCrypt.
- JWT.
- Excepciones HTTP.

## Codigos HTTP

| Codigo | Uso |
|--------|-----|
| 200 | Consulta o actualizacion exitosa |
| 201 | Registro creado |
| 400 | Datos invalidos o credenciales invalidas |
| 401 | No autenticado o JWT ausente/invalido |
| 403 | Usuario autenticado sin permisos suficientes |
| 404 | Registro inexistente |
| 409 | Conflicto, duplicidad o regla de negocio incumplida |

## Flujo de trabajo con Git

El proyecto se trabaja con Git Flow:

```text
main
  ^
release/*
  ^
develop
  ^
feature/*
```

Las funcionalidades se desarrollan en ramas independientes y se integran mediante Pull Request hacia `develop`.

Comandos basicos:

```bash
git checkout develop
git pull origin develop
git checkout -b feature/nombre-tarea
```

## Despliegue

Estado documentado del proyecto:

- Backend: Google Cloud Run.
- Base de datos: Neon PostgreSQL.

Cloud Run inyecta configuracion mediante variables de entorno. Por eso el proyecto respeta:

```properties
server.port=${PORT:8080}
```

No se deben colocar credenciales, secrets ni cadenas de conexion productivas dentro del repositorio.

## Consideraciones tecnicas

- No exponer Entities directamente en las respuestas.
- Utilizar DTO para entrada y salida.
- Mantener la conversion en Mapper.
- Gestionar relaciones mediante JPA.
- Validar entidades relacionadas antes de guardar.
- Usar Soft Delete mediante `activo`.
- Evitar `CascadeType.REMOVE` indiscriminado.
- No exponer password.
- Mantener roles y permisos dinamicos en base de datos.
- Transformar roles a authorities con prefijo `ROLE_`.
- Evitar ciclos de serializacion con relaciones bidireccionales.
- Mantener secretos fuera del repositorio.

## Estado actual

| Funcionalidad | Estado |
|---------------|--------|
| API REST por dominios | Implementado |
| Package by Feature | Implementado |
| PostgreSQL | Implementado |
| DTO y Mapper | Implementado |
| Bean Validation | Implementado |
| Lombok | Implementado |
| Soft Delete | Implementado |
| Streams | Implementado |
| BCrypt | Implementado |
| Relaciones JPA | Implementado |
| Roles y permisos | Implementado |

## Autorizacion por permisos

Ademas de requerir un JWT valido, cada operacion de negocio comprueba una authority obtenida de los permisos activos de los roles activos del usuario. La comprobacion se realiza en el servidor en cada solicitud; `ROLE_ADMIN` no omite estas reglas. Al iniciar la aplicacion se crea o actualiza el catalogo siguiente y, si existe el rol `ADMIN`, se le agregan todos los permisos para conservar su acceso.

| Modulo | Operacion HTTP/ruta | Permiso |
|---|---|---|
| Usuarios | `GET /api/usuarios/**` (incluye roles) | `USUARIOS_LEER` |
| Usuarios | `POST /api/usuarios` | `USUARIOS_CREAR` |
| Usuarios | `PUT /api/usuarios/{id}` | `USUARIOS_EDITAR` |
| Usuarios | `DELETE /api/usuarios/{id}` | `USUARIOS_ELIMINAR` |
| Usuarios | `PUT /api/usuarios/{id}/roles` | `USUARIOS_ASIGNAR_ROLES` |
| Roles | `GET /api/roles/**` | `ROLES_LEER` |
| Roles | `POST /api/roles` | `ROLES_CREAR` |
| Roles | `PUT /api/roles/{id}` | `ROLES_EDITAR` |
| Roles | `PATCH /api/roles/{id}/estado` | `ROLES_CAMBIAR_ESTADO` |
| Roles | `PUT /api/roles/{id}/permisos` | `ROLES_ASIGNAR_PERMISOS` |
| Permisos | `GET /api/permisos/**` (incluye activos) | `PERMISOS_LEER` |
| Permisos | `POST /api/permisos` | `PERMISOS_CREAR` |
| Permisos | `PUT /api/permisos/{id}` | `PERMISOS_EDITAR` |
| Permisos | `PATCH /api/permisos/{id}/estado` | `PERMISOS_CAMBIAR_ESTADO` |
| Carreras | `GET`, `POST`, `PUT`, `PATCH .../estado` | `CARRERAS_LEER`, `CARRERAS_CREAR`, `CARRERAS_EDITAR`, `CARRERAS_CAMBIAR_ESTADO` |
| Cursos | `GET`, `POST`, `PUT`, `PATCH .../estado` | `CURSOS_LEER`, `CURSOS_CREAR`, `CURSOS_EDITAR`, `CURSOS_CAMBIAR_ESTADO` |
| Cursos | `PATCH` o `DELETE /api/cursos/{id}/docente` | `CURSOS_ASIGNAR_DOCENTE` |
| Docentes | `GET`, `POST`, `PUT`, `PATCH .../estado`, `DELETE` | `DOCENTES_LEER`, `DOCENTES_CREAR`, `DOCENTES_EDITAR`, `DOCENTES_CAMBIAR_ESTADO`, `DOCENTES_ELIMINAR` |
| Estudiantes | `GET`, `POST`, `PUT`, `PATCH .../estado` | `ESTUDIANTES_LEER`, `ESTUDIANTES_CREAR`, `ESTUDIANTES_EDITAR`, `ESTUDIANTES_CAMBIAR_ESTADO` |
| Inscripciones | `GET`, `POST`, `PUT`, `PATCH .../estado` o `.../reactivar` | `INSCRIPCIONES_LEER`, `INSCRIPCIONES_CREAR`, `INSCRIPCIONES_EDITAR`, `INSCRIPCIONES_CAMBIAR_ESTADO` |
| Notas | `GET`, `POST`, `PUT`, `PATCH .../estado` | `NOTAS_LEER`, `NOTAS_CREAR`, `NOTAS_EDITAR`, `NOTAS_CAMBIAR_ESTADO` |
| Colegiaturas | `GET`, `POST`, `PUT`, `PATCH .../estado` | `COLEGIATURAS_LEER`, `COLEGIATURAS_CREAR`, `COLEGIATURAS_EDITAR`, `COLEGIATURAS_CAMBIAR_ESTADO` |
| Colegiaturas | `PATCH /api/colegiaturas/{id}/pago` | `COLEGIATURAS_REGISTRAR_PAGO` |

Las rutas auxiliares de lectura (listas activas, nombres/correos, consultas por docente, carrera, curso o estudiante, historiales, resumen, promedio y estado de cuenta) usan el permiso `MODULO_LEER` correspondiente. `POST /api/auth/login`, `POST /api/auth/register` y la documentacion OpenAPI conservan su acceso publico; `GET /api/auth/me` requiere autenticacion.

Ejemplo de campos de sesion agregados sin cambiar los existentes:

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "usuarioId": 7,
  "username": "operador",
  "nombre": "Ana",
  "apellido": "Lopez",
  "roles": ["OPERADOR"],
  "permisos": ["USUARIOS_LEER", "CARRERAS_LEER"]
}
```

`GET /api/auth/me` devuelve igualmente `permisos` junto con `id`, `username`, `email`, `nombre`, `apellido`, `roles` y `activo`. El arreglo es la union sin duplicados de permisos activos pertenecientes a roles activos.

### Autoservicio de perfil

`GET /api/auth/me` requiere JWT y devuelve `usuarioId`, `username`, `email`, `nombre`, `apellido`, `roles` y `permisos` (se conserva tambien el campo historico `id`). `PUT /api/auth/me` permite actualizar solo username, email, nombre y apellido; identifica al usuario por el token y no necesita `USUARIOS_EDITAR`. Si el email vincula su usuario con un registro de estudiante o docente, tambien se actualiza ese correo asociado dentro de la misma transaccion para conservar el acceso a sus propios datos. La respuesta mantiene el perfil e incluye `requiereNuevoLogin`: vale `true` cuando se cambia el username, porque los JWT anteriores usan el username como sujeto y dejan de validar.

```json
{
  "username": "nuevo_usuario",
  "email": "nuevo@correo.com",
  "nombre": "Nuevo nombre",
  "apellido": "Nuevo apellido"
}
```

`PUT /api/auth/me/password` acepta `currentPassword` y `newPassword`, exige la contrasena vigente y aplica la politica actual del registro (entre 8 y 72 caracteres). Tras cinco contrasenas actuales incorrectas se bloquean nuevos intentos durante 15 minutos. Un cambio exitoso responde `204 No Content` y registra un evento de seguridad sin datos de contrasena. Los JWT existentes siguen validos hasta su expiracion; el usuario debe iniciar sesion otra vez para usar la nueva contrasena. Las respuestas nunca incluyen hashes ni valores de contrasena.

### Matriz base por rol y alcance de datos

Los roles son dinamicos. El inicializador reconoce `ADMIN`, `ESTUDIANTE` y `DOCENTE`; cualquier otro rol existente conserva sus asignaciones manuales. Para evitar sobrescribir administracion realizada desde la aplicacion, la matriz base de `ESTUDIANTE` y `DOCENTE` solo se aplica cuando el rol aun no tiene permisos. Las siguientes ejecuciones no reemplazan ni eliminan sus asignaciones. `ADMIN` recibe de forma aditiva el catalogo completo.

| Rol | Permisos base | Alcance efectivo |
|---|---|---|
| `ADMIN` | Todo el catalogo | Acceso global administrativo y academico |
| `ESTUDIANTE` | `ESTUDIANTES_LEER`, `CURSOS_LEER`, `INSCRIPCIONES_LEER`, `NOTAS_LEER`, `COLEGIATURAS_LEER` | Solo su perfil, resumen, historial, estado general, inscripciones, cursos inscritos, notas y colegiaturas |
| `DOCENTE` | `CURSOS_LEER`, `INSCRIPCIONES_LEER`, `NOTAS_LEER`, `NOTAS_CREAR`, `NOTAS_EDITAR`, `NOTAS_CAMBIAR_ESTADO` | Solo sus cursos asignados, alumnos inscritos en esos cursos y notas de esos cursos |
| Otros roles | Sin asignacion automatica | Determinado por sus permisos manuales y por las restricciones de alcance aplicables |

La pertenencia se resuelve en el servidor comparando, sin distinguir mayusculas, `usuario.email` con `estudiante.correo` o `docente.email`. Si no existe esa asociacion, el acceso de alcance se deniega. Para notas, colegiaturas e inscripciones, las rutas por ID consultan la relacion persistida antes de responder; cambiar el ID de la URL no permite acceder a otro estudiante. Los listados globales de estudiantes, cursos, inscripciones, notas y colegiaturas quedan reservados a ADMIN. Un docente debe ser el docente asignado al curso para consultar o modificar sus notas.

### Pagos declarados por estudiantes

Aplicar manualmente `src/main/resources/db/migration/V20260929_02__solicitudes_pago.sql` antes de desplegar. La migracion crea `solicitudes_pago` sin modificar los cargos existentes. Estados: `PENDIENTE` (declarado, sin efecto contable), `APROBADO` (aplicado una vez al saldo) y `RECHAZADO` (sin efecto contable).

* `GET /api/academico/estudiante/me/pagos`: historial propio.
* `POST /api/academico/estudiante/me/colegiaturas/{id}/pagos`: declara un pago propio. El servidor obtiene el estudiante del JWT y rechaza cargos ajenos.
* `GET /api/colegiaturas/pagos/pendientes`: bandeja administrativa.
* `PATCH /api/colegiaturas/pagos/{id}/revision`: aprobacion o rechazo administrativo.

Ejemplo de registro (la clave debe conservarse al reintentar):

```json
{"monto":250.00,"fechaPago":"2026-09-29","referencia":"TRX-9841","metodoPago":"TRANSFERENCIA","comprobanteUrl":null,"idempotencyKey":"1d6d7bdb-9126-46d9-a18c-d4e9876c75da"}
```

Respuesta: `{"id":18,"colegiaturaId":4,"monto":250.00,"fechaPago":"2026-09-29","referencia":"TRX-9841","estado":"PENDIENTE"}`. Errores habituales: `400` por monto/fecha/referencia invalidos o monto superior al saldo, `403` si el cargo no es propio y `404` si no existe. Para revisar: `{"estado":"APROBADO"}` o `{"estado":"RECHAZADO","motivo":"Referencia no localizada"}`.

### Matrícula universitaria, oferta y colegiaturas

Un `CicloAcademico` es el período calendario de oferta con nombre, año y fechas concretas (por ejemplo, “Primer semestre 2027”); no representa el semestre/nivel del plan curricular. `Curso.cicloAnio` se conserva como compatibilidad histórica y año de oferta. Los cursos nuevos pueden vincularse al período concreto mediante `PATCH /api/cursos/{id}/ciclo` con `{"cicloId": 1}`. `GradoAcademico` identifica el nivel o año de avance universitario y `SeccionAcademica` es un grupo asociado a ese grado. La matrícula a carrera se guarda en `matriculas_carrera`; las filas de `inscripciones` conservan su significado de asignación a curso y la enlazan con `matricula_carrera_id`. `grado` y `seccion` de texto se conservan para lectura histórica y las relaciones nuevas se guardan en `grado_id`, `seccion_id` y `ciclo_id`.

La mensualidad de carrera es decimal. `cantidadCuotas` determina cuántos cargos mensuales se emiten desde la fecha de inicio del ciclo; `diaVencimiento` establece el día de vencimiento de cada mes (se ajusta al último día de ese mes cuando es menor). Carreras antiguas conservan estos tres campos nulos hasta su configuración. El flujo no emite cargos históricos. Cada cuota guarda `montoTotal`, ciclo, matrícula y número de cuota; un índice único evita cuotas repetidas y cambios posteriores de precio no recalculan los cargos emitidos.

Para el estudiante, `GET /api/academico/estudiante/me/carreras-disponibles` entrega carreras activas configuradas; `GET .../ciclos-disponibles`, `GET .../grados-disponibles` y `GET .../grados/{gradoId}/secciones` entregan los selectores válidos. `POST /api/academico/estudiante/me/inscripciones` recibe `carreraId`, `cicloId`, `gradoId` y `seccionId`; nunca recibe `estudianteId`. `POST /api/academico/estudiante/me/cursos/{cursoId}/asignacion` asigna un curso de esa carrera y período. Ambas operaciones resuelven al estudiante del JWT. Los permisos son `CARRERAS_LEER`, `INSCRIPCIONES_LEER`, `INSCRIPCIONES_CREAR` y `CURSOS_LEER` para las lecturas académicas; la acción de matrícula requiere `INSCRIPCIONES_CREAR` y rol `ESTUDIANTE`. El registro de pago propio requiere `COLEGIATURAS_LEER`, `COLEGIATURAS_REGISTRAR_PAGO` y rol `ESTUDIANTE` en el endpoint de solicitudes de pago.

Aplicar manualmente `src/main/resources/db/migration/V20261001_01__ciclos_inscripcion_y_cuotas.sql` después de respaldar la base. No hay Flyway/Liquibase configurado: `spring.jpa.hibernate.ddl-auto=update` no ejecuta el SQL de migración. Los textos históricos permanecen y se listan en `migracion_inscripciones_sin_relacion`; como los datos anteriores solo tienen año y texto libre, la migración no inventa nombres/fechas ni asocia registros automáticamente. Después cree ciclos, grados y secciones con `/api/academico/catalogos/{ciclos,grados,secciones}` (POST/PUT; lectura protegida por `CARRERAS_LEER` o `INSCRIPCIONES_LEER`) y asocie el ciclo de oferta a los cursos. Estudiantes con inscripciones activas preexistentes conservan su acceso y no reciben cuotas retroactivas; tampoco pueden iniciar una matrícula diferente hasta que un administrador resuelva su estado vigente.

El inicializador `PermissionCatalogInitializer` agrega permisos de lectura y acciones propias al rol cuyo código es `ESTUDIANTE` al arrancar. Para provisionar específicamente el rol existente `id=3`, ejecute `collection/habilitar_permisos_estudiante_rol_3.sql`; el script comprueba `id=3` y `codigo=ESTUDIANTE`, inserta por código natural e informa el resultado. La migración de estructura no asigna permisos. La app móvil guarda los permisos del inicio de sesión en sesión local; cierre sesión e inicie nuevamente después de cambiar permisos para actualizar las acciones visibles.

### Revisión de requisitos del proyecto

El documento `Proyecto Desarrollo Web.docx` no estaba disponible en el workspace ni en la carpeta Documentos revisada. La siguiente revisión usa evidencia del código y de los requisitos compartidos en la solicitud; la correspondencia exacta del texto de RF-027 a RF-043 necesita el documento fuente.

| Requisito | Estado | Evidencia revisada |
|---|---|---|
| Configuración del centro educativo | Pendiente | No se localizaron entidades/endpoints de configuración institucional en `src/main/java`. |
| Inicio de sesión por correo | Implementado | `AuthService` busca por username o email; `UsuarioRepository.findWithRolesAndPermisosByUsernameIgnoreCaseOrEmailIgnoreCase`. |
| Refresh Token | Pendiente | JWT de acceso con expiración; no existe ruta ni modelo/token de refresco. |
| Cambio de contraseña | Implementado | `AuthController PUT /api/auth/me/password`, verificación de contraseña actual, política y limitador de intentos. |
| MapStruct | Pendiente | Los mappers son clases manuales (`CarreraMapper`, `CursoMapper`, etc.); MapStruct no está en `pom.xml`. |
| Bean Validation | Implementado | `spring-boot-starter-validation`, DTO con restricciones y `@Valid` en controladores. |
| Package by Feature | Implementado | Paquetes por dominio (`carrera`, `curso`, `inscripcion`, `colegiatura`, `academico`, etc.). |
| Swagger/OpenAPI | Implementado | Springdoc en `pom.xml`, configuración `OpenApiSecurityConfig`, Swagger UI habilitada. |
| Postman | Implementado | `collection/SGAU Backend API.postman_collection.json`. |
| README | Implementado | Este archivo describe arquitectura, contratos, seguridad y migración. |
| Dockerfile / Docker Compose | Pendiente | No se encontraron esos archivos en ambos proyectos. |
| Spring Boot 3.x | Diferencia | `pom.xml` fija Spring Boot `4.1.0`; no se cambió automáticamente. |

Trazabilidad de esta entrega: RF-027 ciclo académico; RF-028 catálogo de grados; RF-029 secciones ligadas al grado; RF-030 matrícula a carrera con estudiante/carrera/grado/sección/ciclo; RF-031 selección de período y validación de fechas/estado; RF-032 asignación de curso a carrera/período; RF-033 prevención de duplicados; RF-034 generación transaccional de cuotas; RF-035 snapshot monetario y vencimientos; RF-036 lectura de colegiaturas y pagos propios; RF-037 declaración/revisión de pagos; RF-038 permisos y propiedad desde JWT; RF-039 Android con selectores y acciones; RF-040 migración conservadora de históricos; RF-041 notificaciones; RF-042 documentación de API/actualización; RF-043 pruebas y compilación. Esta numeración es una correspondencia de trabajo derivada de los requisitos suministrados y no una afirmación del texto fuente ausente.
| Spring Security stateless | Implementado |
| JWT | Implementado |
| Swagger / OpenAPI | Implementado |
| Integracion con Neon PostgreSQL | Preparado por variables de entorno |
| Despliegue en Google Cloud Run | Preparado por `PORT` y variables de entorno |

### Revisión de pagos (actualización)

Aplicar manualmente y en orden cronológico `src/main/resources/db/migration/V20260929_04__solicitudes_pago.sql`, `src/main/resources/db/migration/V20261001_01__ciclos_inscripcion_y_cuotas.sql` y `src/main/resources/db/migration/V20261001_02__payment_review_and_receipt_uniqueness.sql`. No hay Flyway/Liquibase configurado. La última conserva solicitudes y boletas repetidas históricas, registra ambigüedades en `migracion_boletas_duplicadas`, y solo protege con unicidad los comprobantes nuevos/no ambiguos normalizados por medio + número. También agrega administrador responsable de revisión.

Contrato actualizado: `POST /api/academico/estudiante/me/colegiaturas/{id}/pagos` recibe `monto`, `fechaPago`, `numeroBoleta` (String; admite el alias legado `referencia`), `metodoPago` obligatorio, `comprobanteUrl` opcional y `idempotencyKey`. La clave se conserva al reintentar; el estudiante se obtiene del JWT. Los importes pendientes se reservan contra saldo disponible sin alterar el saldo oficial hasta aprobación. Boleta única: `LOWER(TRIM(metodoPago)) + ':' + LOWER(TRIM(numeroBoleta))`, con índice único en `referencia_unica`.

Revisión: `GET /api/colegiaturas/pagos?estado=PENDIENTE&q=texto&page=0&size=10`, `GET /api/colegiaturas/pagos/{id}`, y `PATCH /api/colegiaturas/pagos/{id}/revision` con `estado=APROBADO|RECHAZADO` y motivo obligatorio para rechazo. Todos verifican en servidor cuenta activa, rol activo `ADMIN` y permiso vigente `COLEGIATURAS_CAMBIAR_ESTADO`; la revisión bloquea la fila del pago y luego la cuota para serializar administradores. `fechaRevision` y `revisadoPorUsuarioId` conservan la auditoría. Android ofrece “Revisión de pagos” desde Colegiaturas y muestra boleta, fecha, importe, carrera, cuota y comprobante enlazado. No se configuró carga binaria de comprobantes, solo URL opcional.

Los scripts `collection/habilitar_permisos_estudiante_rol_3.sql` y `collection/habilitar_revision_pagos_rol_admin.sql` asignan permisos por código natural en transacciones idempotentes; el segundo resuelve el rol por código `ADMIN`. Las migraciones de esquema no otorgan permisos. El `PermissionCatalogInitializer` agrega el catálogo al rol `ADMIN` activo al arrancar, pero el cliente Android requiere cerrar sesión e iniciar de nuevo para refrescar permisos del JWT/sesión.
