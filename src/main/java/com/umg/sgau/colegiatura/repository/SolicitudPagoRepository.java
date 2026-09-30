package com.umg.sgau.colegiatura.repository;
import com.umg.sgau.colegiatura.entity.SolicitudPago;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SolicitudPagoRepository extends JpaRepository<SolicitudPago, Long> {
    Optional<SolicitudPago> findByEstudiante_IdAndIdempotencyKey(Long estudianteId, String key);
    List<SolicitudPago> findByEstudiante_IdOrderByFechaCreacionDesc(Long estudianteId);
    List<SolicitudPago> findByEstadoOrderByFechaCreacionAsc(String estado);
}
