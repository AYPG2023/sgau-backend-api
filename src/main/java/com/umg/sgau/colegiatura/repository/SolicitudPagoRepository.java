package com.umg.sgau.colegiatura.repository;
import com.umg.sgau.colegiatura.entity.SolicitudPago;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface SolicitudPagoRepository extends JpaRepository<SolicitudPago, Long> {
    Optional<SolicitudPago> findByEstudiante_IdAndIdempotencyKey(Long estudianteId, String key);
    boolean existsByReferenciaUnica(String referenciaUnica);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from SolicitudPago p where p.id = :id")
    Optional<SolicitudPago> findByIdForUpdate(@Param("id") Long id);
    @Query("select coalesce(sum(p.monto), 0) from SolicitudPago p where p.colegiatura.id = :colegiaturaId and p.estado = 'PENDIENTE'")
    java.math.BigDecimal sumarPendientes(@Param("colegiaturaId") Long colegiaturaId);
    @Query(value = "select p from SolicitudPago p join p.estudiante e join p.colegiatura c left join c.inscripcionCarrera m left join m.carrera ca where (:estado is null or p.estado = :estado) and (:texto is null or :texto = '' or lower(p.referencia) like lower(concat('%',:texto,'%')) or lower(e.nombres) like lower(concat('%',:texto,'%')) or lower(e.apellidos) like lower(concat('%',:texto,'%')) or lower(e.codigoEstudiantil) like lower(concat('%',:texto,'%')) or lower(c.concepto) like lower(concat('%',:texto,'%')) or lower(ca.nombre) like lower(concat('%',:texto,'%')))", countQuery = "select count(p) from SolicitudPago p join p.estudiante e join p.colegiatura c left join c.inscripcionCarrera m left join m.carrera ca where (:estado is null or p.estado = :estado) and (:texto is null or :texto = '' or lower(p.referencia) like lower(concat('%',:texto,'%')) or lower(e.nombres) like lower(concat('%',:texto,'%')) or lower(e.apellidos) like lower(concat('%',:texto,'%')) or lower(e.codigoEstudiantil) like lower(concat('%',:texto,'%')) or lower(c.concepto) like lower(concat('%',:texto,'%')) or lower(ca.nombre) like lower(concat('%',:texto,'%')))" )
    Page<SolicitudPago> buscarRevision(@Param("estado") String estado, @Param("texto") String texto, Pageable pageable);
    List<SolicitudPago> findByEstudiante_IdOrderByFechaCreacionDesc(Long estudianteId);
    List<SolicitudPago> findByEstadoOrderByFechaCreacionAsc(String estado);
}
