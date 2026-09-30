package com.umg.sgau.academico;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface CicloAcademicoRepository extends JpaRepository<CicloAcademico,Long> { List<CicloAcademico> findByActivoTrueOrderByAnioDescFechaInicioDesc(); }
