package com.umg.sgau.docente.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.umg.sgau.docente.entity.Docente;

@Repository
public interface DocenteRepository  extends JpaRepository <Docente, Long> {
	
	Optional<Docente> findByEmail(String email);

    Optional<Docente> findByCodigoDocente(String codigoDocente);

    boolean existsByEmail(String email);

    boolean existsByCodigoDocente(String codigoDocente);
	
}
