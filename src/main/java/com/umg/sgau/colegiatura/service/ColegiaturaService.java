package com.umg.sgau.colegiatura.service;

import com.umg.sgau.colegiatura.entity.Colegiatura;
import com.umg.sgau.colegiatura.model.EstadoCuentaEstudiante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ColegiaturaService {

    Colegiatura crear(
            Colegiatura colegiatura
    );

    Colegiatura obtenerPorId(
            Long id
    );

    Page<Colegiatura> listar(
            Long estudianteId,
            Integer cicloAnio,
            String estado,
            Boolean activo,
            String concepto,
            Pageable pageable
    );

    Colegiatura actualizar(
            Long id,
            Colegiatura colegiatura
    );

    Colegiatura registrarPago(
            Long id,
            BigDecimal montoPago
    );

    Colegiatura cambiarEstado(
            Long id,
            Boolean activo
    );

    List<Colegiatura> obtenerPendientes();

    List<BigDecimal> obtenerSaldosPendientes();

    List<Colegiatura> obtenerHistorialPorEstudiante(
            Long estudianteId
    );

    List<Colegiatura> obtenerActivasPorEstudiante(
            Long estudianteId
    );

    Page<Colegiatura> obtenerActivasPorEstudiante(
            Long estudianteId,
            Pageable pageable
    );

    List<Colegiatura> obtenerPendientesPorEstudiante(
            Long estudianteId
    );

    Page<Colegiatura> obtenerPendientesPorEstudiante(
            Long estudianteId,
            Pageable pageable
    );

    BigDecimal calcularSaldoPendiente(
            Long estudianteId
    );

    EstadoCuentaEstudiante generarEstadoCuenta(
            Long estudianteId
    );

}
