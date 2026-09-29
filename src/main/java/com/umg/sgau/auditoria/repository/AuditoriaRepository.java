package com.umg.sgau.auditoria.repository;

import com.umg.sgau.auditoria.entity.AuditoriaEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditoriaRepository extends JpaRepository<AuditoriaEvento, Long>, JpaSpecificationExecutor<AuditoriaEvento> {}
