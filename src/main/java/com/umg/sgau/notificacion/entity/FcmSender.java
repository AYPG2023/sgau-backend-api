package com.umg.sgau.notificacion.service;
import java.util.Map;
public interface FcmSender { void send(String token,Map<String,String> data) throws Exception; }
