package com.umg.sgau.academico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface MatriculaCarreraRepository extends JpaRepository<MatriculaCarrera,Long> {
 boolean existsByEstudiante_IdAndActivoTrue(Long estudianteId);
 Optional<MatriculaCarrera> findFirstByEstudiante_IdAndActivoTrueOrderByFechaInscripcionDesc(Long estudianteId);
}
