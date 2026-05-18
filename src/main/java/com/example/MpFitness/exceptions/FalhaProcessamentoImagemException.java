package com.example.MpFitness.exceptions;

public class FalhaProcessamentoImagemException extends RuntimeException {

    public FalhaProcessamentoImagemException() {
        super("Falha ao processar a imagem do produto");
    }

    public FalhaProcessamentoImagemException(String message) {
        super(message);
    }
}