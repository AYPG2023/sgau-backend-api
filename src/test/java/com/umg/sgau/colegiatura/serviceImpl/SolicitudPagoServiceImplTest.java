package com.umg.sgau.colegiatura.serviceimpl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.umg.sgau.colegiatura.dto.SolicitudPagoDTOs;
import com.umg.sgau.colegiatura.entity.*;
import com.umg.sgau.colegiatura.repository.*;
import com.umg.sgau.config.AccessScopeService;
import com.umg.sgau.estudiante.entity.Estudiante;
import com.umg.sgau.estudiante.repository.EstudianteRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

class SolicitudPagoServiceImplTest {
 @Mock AccessScopeService scope; @Mock EstudianteRepository estudiantes; @Mock ColegiaturaRepository cargos;
 @Mock SolicitudPagoRepository solicitudes; @Mock Authentication auth; @InjectMocks SolicitudPagoServiceImpl service;
 @BeforeEach void init(){MockitoAnnotations.openMocks(this);}
 private Colegiatura cargo(long owner){return Colegiatura.builder().id(7L).estudiante(Estudiante.builder().id(owner).build()).activo(true).montoTotal(new BigDecimal("500.00")).montoPagado(BigDecimal.ZERO).saldoPendiente(new BigDecimal("500.00")).build();}
 private SolicitudPagoDTOs.Registro request(){return new SolicitudPagoDTOs.Registro(new BigDecimal("100.00"),LocalDate.now(),"REF-1","TRANSFERENCIA",null,"key-1");}
 @Test void rechazaColegiaturaAjena(){when(scope.idEstudiante(auth)).thenReturn(Optional.of(2L));when(cargos.findByIdForUpdate(7L)).thenReturn(Optional.of(cargo(1L)));assertThatThrownBy(()->service.registrar(auth,7L,request())).isInstanceOf(AccessDeniedException.class);}
 @Test void pendienteNoAfectaSaldoOficial(){Colegiatura c=cargo(1L);when(scope.idEstudiante(auth)).thenReturn(Optional.of(1L));when(cargos.findByIdForUpdate(7L)).thenReturn(Optional.of(c));when(solicitudes.findByEstudiante_IdAndIdempotencyKey(1L,"key-1")).thenReturn(Optional.empty());when(solicitudes.save(any())).thenAnswer(i->{SolicitudPago s=i.getArgument(0);s.setId(9L);return s;});service.registrar(auth,7L,request());assertThat(c.getSaldoPendiente()).isEqualByComparingTo("500.00");verify(cargos,never()).save(any());}
 @Test void reintentoDevuelveMismaSolicitud(){SolicitudPago s=SolicitudPago.builder().id(9L).colegiatura(cargo(1L)).estudiante(Estudiante.builder().id(1L).build()).monto(new BigDecimal("100")).fechaPago(LocalDate.now()).referencia("REF-1").estado("PENDIENTE").idempotencyKey("key-1").build();when(scope.idEstudiante(auth)).thenReturn(Optional.of(1L));when(cargos.findByIdForUpdate(7L)).thenReturn(Optional.of(s.getColegiatura()));when(solicitudes.findByEstudiante_IdAndIdempotencyKey(1L,"key-1")).thenReturn(Optional.of(s));assertThat(service.registrar(auth,7L,request()).id()).isEqualTo(9L);verify(solicitudes,never()).save(any());}
 @Test void aprobarDosVecesSoloAplicaUna(){Colegiatura c=cargo(1L);SolicitudPago s=SolicitudPago.builder().id(9L).colegiatura(c).estudiante(c.getEstudiante()).monto(new BigDecimal("100")).fechaPago(LocalDate.now()).referencia("R").estado("PENDIENTE").idempotencyKey("k").build();when(solicitudes.findById(9L)).thenReturn(Optional.of(s));when(cargos.findByIdForUpdate(7L)).thenReturn(Optional.of(c));when(solicitudes.save(any())).thenAnswer(i->i.getArgument(0));service.revisar(9L,new SolicitudPagoDTOs.Revision("APROBADO",null));service.revisar(9L,new SolicitudPagoDTOs.Revision("APROBADO",null));assertThat(c.getMontoPagado()).isEqualByComparingTo("100.00");verify(cargos,times(1)).save(c);}
}
