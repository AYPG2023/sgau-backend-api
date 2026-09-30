package com.umg.sgau.curso.controller;
import com.umg.sgau.academico.CicloAcademicoRepository; import com.umg.sgau.curso.repository.CursoRepository;
import jakarta.validation.constraints.NotNull; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/cursos")
public class CursoCicloController {
 private final CursoRepository cursos; private final CicloAcademicoRepository ciclos;
 public CursoCicloController(CursoRepository cursos,CicloAcademicoRepository ciclos){this.cursos=cursos;this.ciclos=ciclos;}
 public record CicloRequest(@NotNull Long cicloId){}
 @PatchMapping("/{id}/ciclo") @PreAuthorize("hasAnyAuthority('CURSOS_CREAR','CURSOS_EDITAR')")
 public Object vincularCiclo(@PathVariable Long id,@RequestBody CicloRequest req){var curso=cursos.findById(id).orElseThrow();var ciclo=ciclos.findById(req.cicloId()).orElseThrow();if(!Boolean.TRUE.equals(ciclo.getActivo()))throw new IllegalArgumentException("El ciclo académico debe estar activo.");curso.setCiclo(ciclo);curso.setCicloAnio(ciclo.getAnio());return cursos.save(curso);}
}
