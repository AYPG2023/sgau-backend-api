package com.umg.sgau.notificacion.repository;
import com.umg.sgau.notificacion.entity.Notificacion; import java.time.LocalDateTime; import java.util.*; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param;
public interface NotificacionRepository extends JpaRepository<Notificacion,Long>{
 Page<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Long uid,Pageable p); long countByUsuarioIdAndLeidaFalse(Long uid); Optional<Notificacion> findByIdAndUsuarioId(Long id,Long uid); boolean existsByUsuarioIdAndEventKey(Long uid,String key); int deleteByUsuarioId(Long uid);
 @Modifying @Query("update Notificacion n set n.leida=true where n.usuarioId=:uid and n.leida=false") int readAll(@Param("uid")Long uid);
 @Modifying @Query("update Notificacion n set n.entregaEstado='PENDIENTE', n.nextAttemptAt=:now where n.usuarioId=:uid and n.entregaEstado='SIN_DISPOSITIVO'") int requeueForUser(@Param("uid")Long uid,@Param("now")LocalDateTime now);
 @Query("select n from Notificacion n where n.entregaEstado in ('PENDIENTE','ERROR') and n.intentos<5 and n.nextAttemptAt<=:now order by n.fechaCreacion") List<Notificacion> due(@Param("now")LocalDateTime now,Pageable p);
}
