# SGAU Backend API

## Descripción

SGAU Backend API es el backend del Sistema de Gestión Académica Universitaria. Su propósito es servir como base para administrar procesos académicos como usuarios, estudiantes, docentes, carreras, cursos, inscripciones, colegiaturas y notas.

El sistema expone servicios REST que permitirán centralizar la lógica de negocio, la validación de datos, la seguridad y el acceso a la base de datos para los módulos académicos previstos.

## Objetivo general

Desarrollar una API backend robusta, mantenible y segura para gestionar la información académica universitaria, aplicando buenas prácticas de arquitectura, separación de responsabilidades y control de versiones.

## Tecnologías utilizadas

- Java 17
- Spring Boot 4
- Maven
- Spring Web
- Spring Data JPA
- Spring Security
- PostgreSQL
- Jakarta Validation
- Lombok
- Spring Boot DevTools
- Git y GitHub

## Arquitectura

El proyecto seguirá una arquitectura **Package by Feature**, organizando el código por dominio funcional en lugar de agruparlo solamente por tipo técnico. Esta estructura facilita que cada módulo concentre sus responsabilidades y pueda evolucionar de forma independiente.

La estructura general esperada del proyecto es la siguiente:

```text
src/main/java/com/umg/sgau/
├── common/
│   ├── config/
│   ├── exception/
│   ├── response/
│   └── security/
├── usuario/
├── estudiante/
├── docente/
├── carrera/
├── curso/
├── inscripcion/
├── colegiatura/
└── nota/
```

Los componentes transversales y compartidos deben colocarse dentro de `common`, mientras que la lógica específica de cada dominio debe mantenerse dentro de su respectivo módulo.

### Paquetes comunes

- `common/config`: configuraciones generales y compartidas de la aplicación.
- `common/exception`: manejo centralizado de excepciones y errores.
- `common/response`: estructuras estandarizadas para las respuestas de la API.
- `common/security`: configuración de autenticación, autorización y Spring Security.

## Estructura esperada por dominio

Cada dominio deberá mantener, cuando aplique, la siguiente estructura:

```text
dominio/
├── controller/
├── dto/
├── entity/
├── mapper/
├── repository/
├── service/
└── serviceimpl/
```

### Responsabilidad de cada paquete

- `controller`: expone los endpoints REST del dominio.
- `dto`: define los objetos de transferencia de datos para entrada y salida.
- `entity`: contiene las entidades persistentes administradas por JPA.
- `mapper`: transforma entidades a DTO y DTO a entidades.
- `repository`: define el acceso a datos mediante Spring Data JPA.
- `service`: declara los contratos de la lógica de negocio.
- `serviceimpl`: implementa los servicios del dominio.

## Dominios previstos

- `usuario`
- `estudiante`
- `docente`
- `carrera`
- `curso`
- `inscripcion`
- `colegiatura`
- `nota`

## Flujo de ramas

- `main`: rama destinada a versiones estables del proyecto.
- `develop`: rama de integración donde se consolidan los avances antes de pasar a una versión estable.
- `feature/tsk-###-dominio`: ramas de trabajo para desarrollar cada módulo o tarea específica. Ejemplo: `feature/tsk-001-usuario`.

## Requisitos para ejecutar el proyecto

- JDK 17 instalado y configurado.
- PostgreSQL instalado y disponible.
- Maven Wrapper incluido en el proyecto.
- Variables de entorno o archivo de configuración local con las credenciales necesarias.
- Git instalado para control de versiones.

## Ejecución del proyecto

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux/macOS

```bash
./mvnw spring-boot:run
```

## Compilación y pruebas

### Windows

```powershell
.\mvnw.cmd clean test
```

### Linux/macOS

```bash
./mvnw clean test
```

## Configuración sensible

Las credenciales, cadenas de conexión, claves secretas y configuraciones sensibles no deben publicarse en GitHub ni compartirse dentro del repositorio.

Archivos como `application.properties`, `application.yml`, `application-dev.properties`, `application-prod.properties`, `.env` y `.env.*` deben mantenerse como configuración local o gestionarse mediante variables de entorno.

