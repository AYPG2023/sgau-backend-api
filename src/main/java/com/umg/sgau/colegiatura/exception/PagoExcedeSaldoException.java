package com.umg.sgau.colegiatura.exception;

public class PagoExcedeSaldoException extends RuntimeException {

    public PagoExcedeSaldoException(Long id) {
        super("El pago supera el saldo pendiente de la colegiatura " + id + ".");
    }
}
