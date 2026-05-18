package com.example.MpFitness.exceptions;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException(String email) {
        super("Email já está em uso: " + email);
    }
}