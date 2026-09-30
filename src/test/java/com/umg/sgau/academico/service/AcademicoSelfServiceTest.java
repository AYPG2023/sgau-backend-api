package com.umg.sgau.academico.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.umg.sgau.academico.exception.VinculacionAcademicaNoEncontradaException;
import com.umg.sgau.colegiatura.repository.ColegiaturaRepository;
import com.umg.sgau.carrera.repository.CarreraRepository;
import com.umg.sgau.config.AccessScopeService;
import com.umg.sgau.curso.entity.Curso;
import com.umg.sgau.carrera.entity.Carrera;
import com.umg.sgau.curso.repository.CursoRepository;
import com.umg.sgau.docente.entity.Docente;
import com.umg.sgau.docente.repository.DocenteRepository;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.repository.EstudianteRepository;
import com.umg.sgau.inscripcion.entity.Inscripcion;
import com.umg.sgau.inscripcion.repository.InscripcionRepository;
import com.umg.sgau.nota.repository.NotaRepository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AcademicoSelfServiceTest {
    @Mock AccessScopeService scope; @Mock DocenteRepository docentes; @Mock EstudianteRepository estudiantes;
    @Mock CursoRepository cursos; @Mock InscripcionRepository inscripciones; @Mock NotaRepository notas;
    @Mock CarreraRepository carreras;
    @Mock ColegiaturaRepository colegiaturas; @Mock Authentication auth;
    AcademicoSelfService service;

    @BeforeEach void setUp() { service = new AcademicoSelfService(scope, docentes, estudiantes, cursos, carreras, inscripciones, notas, colegiaturas); }

    @Test void docenteSinCursosRecibeListaVacia() {
        Docente d=Docente.builder().id(5L).email("docente@sgau.test").activo(true).build();
        when(scope.idDocente(auth)).thenReturn(Optional.of(5L)); when(docentes.findById(5L)).thenReturn(Optional.of(d));
        when(cursos.findByDocente_Id(5L)).thenReturn(List.of());
        assertThat(service.cursosDocente(auth)).isEmpty();
    }

    @Test void docenteNoPuedeConsultarAlumnosDeCursoAjeno() {
        Docente d=Docente.builder().id(5L).email("docente@sgau.test").activo(true).build();
        when(scope.idDocente(auth)).thenReturn(Optional.of(5L)); when(docentes.findById(5L)).thenReturn(Optional.of(d));
        when(cursos.findById(99L)).thenReturn(java.util.Optional.of(Curso.builder().id(99L).build()));
        assertThatThrownBy(() -> service.alumnosCurso(auth, 99L, Pageable.unpaged())).isInstanceOf(AccessDeniedException.class);
    }

    @Test void estudianteSoloConsultaInscripcionesDelIdResueltoPorJwt() {
        Estudiante e=Estudiante.builder().id(8L).correo("estudiante@sgau.test").activo(true).build();
        when(scope.idEstudiante(auth)).thenReturn(Optional.of(8L)); when(estudiantes.findById(8L)).thenReturn(Optional.of(e));
        when(inscripciones.findByEstudiante_Id(8L, Pageable.unpaged())).thenReturn(new PageImpl<Inscripcion>(List.of()));
        assertThat(service.inscripcionesEstudiante(auth, Pageable.unpaged())).isEmpty();
    }

    @Test void faltaDeVinculoProduceErrorExplicito() {
        when(scope.idDocente(auth)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.docente(auth)).isInstanceOf(VinculacionAcademicaNoEncontradaException.class)
                .hasMessageContaining("docente");
    }

    @Test void docenteNoPuedeForzarOtroCicloEnCursoAsignado() {
        Docente d=Docente.builder().id(5L).email("docente@sgau.test").activo(true).build();
        Curso c=Curso.builder().id(10L).docente(d).cicloAnio(2026).build();
        when(scope.idDocente(auth)).thenReturn(Optional.of(5L)); when(docentes.findById(5L)).thenReturn(Optional.of(d));
        when(cursos.findById(10L)).thenReturn(Optional.of(c));
        assertThatThrownBy(() -> service.alumnosCurso(auth,10L,2027,Pageable.unpaged()))
                .isInstanceOf(AccessDeniedException.class).hasMessageContaining("ciclo");
    }

    @Test void planDistingueCursosDisponiblesDeInscritosYSumaCreditos() {
        Estudiante e=Estudiante.builder().id(8L).correo("estudiante@sgau.test").activo(true).build();
        Carrera carrera=Carrera.builder().id(3L).codigo("SIS").nombre("Sistemas").duracionAnios(5).activo(true).build();
        Curso inscrito=Curso.builder().id(10L).codigo("C1").nombre("Uno").creditos(5).horasSemanales(4).cicloAnio(2026).carrera(carrera).activo(true).build();
        Curso disponible=Curso.builder().id(11L).codigo("C2").nombre("Dos").creditos(4).horasSemanales(3).cicloAnio(2026).carrera(carrera).activo(true).build();
        Inscripcion i=Inscripcion.builder().id(20L).estudiante(e).carrera(carrera).curso(inscrito).cicloAnio(2026)
                .fechaInscripcion(LocalDate.now()).estado("ACTIVA").activo(true).build();
        when(scope.idEstudiante(auth)).thenReturn(Optional.of(8L)); when(estudiantes.findById(8L)).thenReturn(Optional.of(e));
        when(inscripciones.findByEstudiante_IdAndActivoTrueOrderByCicloAnioDescFechaInscripcionDesc(8L)).thenReturn(List.of(i));
        when(cursos.findByCarrera_IdAndActivoTrueOrderByCicloAnioDescNombreAsc(3L)).thenReturn(List.of(inscrito,disponible));
        var plan=service.planCarrera(auth);
        assertThat(plan.totalCreditosPlan()).isEqualTo(9);
        assertThat(plan.totalCreditosInscritos()).isEqualTo(5);
        assertThat(plan.cursosDisponibles()).extracting(com.umg.sgau.academico.dto.AcademicoDTOs.CursoPlan::inscrito)
                .containsExactly(true,false);
        assertThat(plan.cursosInscritos()).hasSize(1);
    }
}
