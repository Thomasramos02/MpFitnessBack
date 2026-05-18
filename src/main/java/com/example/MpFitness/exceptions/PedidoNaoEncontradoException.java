package com.example.MpFitness.exceptions;

public class PedidoNaoEncontradoException extends RuntimeException {

    public PedidoNaoEncontradoException(Long pedidoId) {
        super("Pedido não encontrado: " + pedidoId);
    }

    public PedidoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}