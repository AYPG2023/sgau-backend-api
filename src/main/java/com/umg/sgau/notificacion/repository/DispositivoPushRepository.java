package com.umg.sgau.notificacion.repository;
import com.umg.sgau.notificacion.entity.DispositivoPush; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface DispositivoPushRepository extends JpaRepository<DispositivoPush,Long>{ Optional<DispositivoPush> findByToken(String token); List<DispositivoPush> findByUsuarioId(Long usuarioId); void deleteByUsuarioIdAndToken(Long usuarioId,String token); void deleteByToken(String token); }
