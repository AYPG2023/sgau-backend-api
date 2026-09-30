package com.umg.sgau.academico;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface SeccionAcademicaRepository extends JpaRepository<SeccionAcademica,Long> { List<SeccionAcademica> findByGrado_IdAndActivoTrueOrderByNombreAsc(Long gradoId); }
