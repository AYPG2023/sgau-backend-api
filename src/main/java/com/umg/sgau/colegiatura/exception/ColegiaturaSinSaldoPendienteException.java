package com.umg.sgau.colegiatura.exception;

public class ColegiaturaSinSaldoPendienteException extends RuntimeException {

    public ColegiaturaSinSaldoPendienteException(Long id) {
        super("La colegiatura con ID " + id + " no tiene saldo pendiente.");
    }
}
