package com.umg.sgau.notificacion.service;
public record EventoNotificacion(Long usuarioId,String eventKey,String tipo,String titulo,String mensaje,String destinoTipo,Long destinoId,boolean administradores) { }
