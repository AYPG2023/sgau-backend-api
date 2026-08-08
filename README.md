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
| Spring Security stateless | Implementado |
| JWT | Implementado |
| Swagger / OpenAPI | Implementado |
| Integracion con Neon PostgreSQL | Preparado por variables de entorno |
| Despliegue en Google Cloud Run | Preparado por `PORT` y variables de entorno |
