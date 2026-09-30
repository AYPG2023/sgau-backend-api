package com.umg.sgau.academico;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface GradoAcademicoRepository extends JpaRepository<GradoAcademico,Long> { List<GradoAcademico> findByActivoTrueOrderByNombreAsc(); }
