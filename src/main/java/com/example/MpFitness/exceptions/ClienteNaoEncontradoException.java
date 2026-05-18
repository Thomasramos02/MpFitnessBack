package com.example.MpFitness.exceptions;

public class ClienteNaoEncontradoException extends RuntimeException {

    public ClienteNaoEncontradoException(Long clienteId) {
        super("Cliente não encontrado: " + clienteId);
    }

    public ClienteNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}