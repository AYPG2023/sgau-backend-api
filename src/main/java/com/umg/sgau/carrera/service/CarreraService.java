package com.umg.sgau.carrera.service;

import com.umg.sgau.carrera.entity.Carrera;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Contrato de operaciones del dominio Carrera usando exclusivamente la entidad Carrera.
 */
public interface CarreraService {

    /**
     * Registra una nueva carrera.
     *
     * @param carrera entidad con los datos de la carrera a crear
     * @return carrera guardada
     */
    Carrera crear(Carrera carrera);

    /**
     * Obtiene una carrera por su identificador.
     *
     * @param id identificador de la carrera
     * @return carrera encontrada
     */
    Carrera obtenerPorId(Long id);

    /**
     * Obtiene todas las carreras registradas.
     *
     * @return lista de carreras
     */
    List<Carrera> obtenerTodas();

    /**
     * Lista carreras aplicando filtros opcionales y paginacion.
     *
     * @param texto texto opcional para busqueda
     * @param activo estado opcional para filtrar carreras
     * @param pageable configuracion de paginacion y ordenamiento
     * @return pagina de carreras que cumplen los filtros
     */
    Page<Carrera> listar(String texto, Boolean activo, Pageable pageable);

    /**
     * Actualiza los campos editables de una carrera existente.
     *
     * @param id identificador de la carrera a actualizar
     * @param carrera entidad con los nuevos datos editables
     * @return carrera actualizada
     */
    Carrera actualizar(Long id, Carrera carrera);

    /**
     * Cambia el estado activo o inactivo de una carrera.
     *
     * @param id identificador de la carrera
     * @param activo nuevo estado de la carrera
     * @return carrera con el estado actualizado
     */
    Carrera cambiarEstado(Long id, Boolean activo);

    /**
     * Obtiene las carreras activas.
     *
     * @return lista de carreras activas
     */
    List<Carrera> obtenerCarrerasActivas();

    /**
     * Obtiene los nombres de las carreras activas.
     *
     * @return lista de nombres de carreras activas
     */
    List<String> obtenerNombresDeCarrerasActivas();
}
