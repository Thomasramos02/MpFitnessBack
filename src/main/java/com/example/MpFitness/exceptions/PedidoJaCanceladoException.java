package com.example.MpFitness.exceptions;

public class PedidoJaCanceladoException extends RuntimeException {

    public PedidoJaCanceladoException() {
        super("Pedido já está cancelado");
    }
}