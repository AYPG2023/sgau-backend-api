package com.umg.sgau.academico.controller;

import com.umg.sgau.academico.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/academico/catalogos")
public class CatalogosAcademicosController {
 private final CicloAcademicoRepository ciclos; private final GradoAcademicoRepository grados; private final SeccionAcademicaRepository secciones;
 public CatalogosAcademicosController(CicloAcademicoRepository c,GradoAcademicoRepository g,SeccionAcademicaRepository s){ciclos=c;grados=g;secciones=s;}
 public record CicloInput(@NotBlank String nombre,@NotNull @Min(2020) @Max(2100) Integer anio,@NotNull LocalDate fechaInicio,@NotNull LocalDate fechaFin,Boolean activo){}
 public record GradoInput(@NotBlank String codigo,@NotBlank String nombre,Boolean activo){}
 public record SeccionInput(@NotBlank String codigo,@NotBlank String nombre,@NotNull Long gradoId,Boolean activo){}
 @GetMapping("/ciclos") @PreAuthorize("@accessScope.esAdmin(authentication)") public List<CicloAcademico> ciclos(){return ciclos.findAll();}
 @PostMapping("/ciclos") @PreAuthorize("hasAuthority('CARRERAS_CREAR')") public CicloAcademico crearCiclo(@Valid @RequestBody CicloInput x){if(x.fechaFin().isBefore(x.fechaInicio()))throw new IllegalArgumentException("La fecha final debe ser posterior a la inicial.");return ciclos.save(CicloAcademico.builder().nombre(x.nombre().trim()).anio(x.anio()).fechaInicio(x.fechaInicio()).fechaFin(x.fechaFin()).activo(x.activo()==null||x.activo()).build());}
 @PutMapping("/ciclos/{id}") @PreAuthorize("hasAuthority('CARRERAS_EDITAR')") public CicloAcademico actualizarCiclo(@PathVariable Long id,@Valid @RequestBody CicloInput x){var e=ciclos.findById(id).orElseThrow();if(x.fechaFin().isBefore(x.fechaInicio()))throw new IllegalArgumentException("La fecha final debe ser posterior a la inicial.");e.setNombre(x.nombre().trim());e.setAnio(x.anio());e.setFechaInicio(x.fechaInicio());e.setFechaFin(x.fechaFin());e.setActivo(x.activo());return ciclos.save(e);}
 @GetMapping("/grados") @PreAuthorize("@accessScope.esAdmin(authentication)") public List<GradoAcademico> grados(){return grados.findAll();}
 @PostMapping("/grados") @PreAuthorize("hasAuthority('CARRERAS_CREAR')") public GradoAcademico crearGrado(@Valid @RequestBody GradoInput x){return grados.save(GradoAcademico.builder().codigo(x.codigo().trim()).nombre(x.nombre().trim()).activo(x.activo()==null||x.activo()).build());}
 @PutMapping("/grados/{id}") @PreAuthorize("hasAuthority('CARRERAS_EDITAR')") public GradoAcademico actualizarGrado(@PathVariable Long id,@Valid @RequestBody GradoInput x){var e=grados.findById(id).orElseThrow();e.setCodigo(x.codigo().trim());e.setNombre(x.nombre().trim());e.setActivo(x.activo());return grados.save(e);}
 @GetMapping("/secciones") @PreAuthorize("@accessScope.esAdmin(authentication)") public List<SeccionAcademica> secciones(){return secciones.findAll();}
 @PostMapping("/secciones") @PreAuthorize("hasAuthority('CARRERAS_CREAR')") public SeccionAcademica crearSeccion(@Valid @RequestBody SeccionInput x){var g=grados.findById(x.gradoId()).orElseThrow();return secciones.save(SeccionAcademica.builder().codigo(x.codigo().trim()).nombre(x.nombre().trim()).grado(g).activo(x.activo()==null||x.activo()).build());}
 @PutMapping("/secciones/{id}") @PreAuthorize("hasAuthority('CARRERAS_EDITAR')") public SeccionAcademica actualizarSeccion(@PathVariable Long id,@Valid @RequestBody SeccionInput x){var e=secciones.findById(id).orElseThrow();e.setCodigo(x.codigo().trim());e.setNombre(x.nombre().trim());e.setGrado(grados.findById(x.gradoId()).orElseThrow());e.setActivo(x.activo());return secciones.save(e);}
}
