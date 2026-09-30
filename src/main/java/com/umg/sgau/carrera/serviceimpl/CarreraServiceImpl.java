package com.umg.sgau.carrera.serviceimpl;

import com.umg.sgau.carrera.entity.Carrera;
import com.umg.sgau.carrera.exception.CarreraNoEncontradaException;
import com.umg.sgau.carrera.exception.CodigoCarreraDuplicadoException;
import com.umg.sgau.carrera.exception.NombreCarreraDuplicadoException;
import com.umg.sgau.carrera.repository.CarreraRepository;
import com.umg.sgau.carrera.service.CarreraService;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CarreraServiceImpl implements CarreraService {

    private final CarreraRepository carreraRepository;

    public CarreraServiceImpl(CarreraRepository carreraRepository) {
        this.carreraRepository = carreraRepository;
    }

    @Override
    public Carrera crear(Carrera carrera) {
        String codigoNormalizado = normalizarCodigo(carrera.getCodigo());
        String nombreNormalizado = normalizarTextoObligatorio(carrera.getNombre());
        String descripcionNormalizada = normalizarTextoOpcional(carrera.getDescripcion());

        carrera.setCodigo(codigoNormalizado);
        carrera.setNombre(nombreNormalizado);
        carrera.setDescripcion(descripcionNormalizada);

        if (carrera.getActivo() == null) {
            carrera.setActivo(true);
        }

        if (carreraRepository.existsByCodigo(codigoNormalizado)) {
            throw new CodigoCarreraDuplicadoException(codigoNormalizado);
        }

        if (carreraRepository.existsByNombreIgnoreCase(nombreNormalizado)) {
            throw new NombreCarreraDuplicadoException(nombreNormalizado);
        }

        return carreraRepository.save(carrera);
    }

    @Override
    public Carrera obtenerPorId(Long id) {
        return carreraRepository.findById(id)
                .orElseThrow(() -> new CarreraNoEncontradaException(id));
    }

    @Override
    public List<Carrera> obtenerTodas() {
        return carreraRepository.findAll();
    }

    @Override
    public Page<Carrera> listar(String texto, Boolean activo, Pageable pageable) {
        String textoNormalizado = texto == null ? null : texto.trim();
        return carreraRepository.buscarConFiltros(textoNormalizado, activo, pageable);
    }

    @Override
    public Carrera actualizar(Long id, Carrera carrera) {
        Carrera existente = obtenerPorId(id);
        String codigoNormalizado = normalizarCodigo(carrera.getCodigo());
        String nombreNormalizado = normalizarTextoObligatorio(carrera.getNombre());
        String descripcionNormalizada = normalizarTextoOpcional(carrera.getDescripcion());

        if (carreraRepository.existsByCodigoAndIdNot(codigoNormalizado, id)) {
            throw new CodigoCarreraDuplicadoException(codigoNormalizado);
        }

        if (carreraRepository.existsByNombreIgnoreCaseAndIdNot(nombreNormalizado, id)) {
            throw new NombreCarreraDuplicadoException(nombreNormalizado);
        }

        existente.setCodigo(codigoNormalizado);
        existente.setNombre(nombreNormalizado);
        existente.setDescripcion(descripcionNormalizada);
        existente.setDuracionAnios(carrera.getDuracionAnios());
        existente.setMensualidad(carrera.getMensualidad());
        existente.setCantidadCuotas(carrera.getCantidadCuotas());
        existente.setDiaVencimiento(carrera.getDiaVencimiento());

        return carreraRepository.save(existente);
    }

    @Override
    public Carrera cambiarEstado(Long id, Boolean activo) {
        Carrera carrera = obtenerPorId(id);
        carrera.setActivo(activo);
        return carreraRepository.save(carrera);
    }

    @Override
    public List<Carrera> obtenerCarrerasActivas() {
        return carreraRepository.findAll()
                .stream()
                .filter(carrera -> Boolean.TRUE.equals(carrera.getActivo()))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> obtenerNombresDeCarrerasActivas() {
        return carreraRepository.findAll()
                .stream()
                .filter(carrera -> Boolean.TRUE.equals(carrera.getActivo()))
                .map(Carrera::getNombre)
                .collect(Collectors.toList());
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizarTextoObligatorio(String texto) {
        return texto.trim();
    }

    private String normalizarTextoOpcional(String texto) {
        if (texto == null) {
            return "";
        }

        return texto.trim();
    }
}
