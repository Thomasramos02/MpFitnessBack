package com.example.MpFitness.exceptions;

public class CarrinhoVazioException extends RuntimeException {

    public CarrinhoVazioException() {
        super("Carrinho vazio");
    }

    public CarrinhoVazioException(String mensagem) {
        super(mensagem);
    }
}