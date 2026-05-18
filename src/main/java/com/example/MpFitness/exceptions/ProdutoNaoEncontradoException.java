package com.example.MpFitness.exceptions;

public class ProdutoNaoEncontradoException extends RuntimeException {

    public ProdutoNaoEncontradoException(Long produtoId) {
        super("Produto não encontrado: " + produtoId);
    }

    public ProdutoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}