package com.example.MpFitness.exceptions;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({ ClienteNaoEncontradoException.class, ProdutoNaoEncontradoException.class,
            PedidoNaoEncontradoException.class, CarrinhoVazioException.class })
    public ResponseEntity<ProblemDetail> handleNotFound(RuntimeException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("Recurso não encontrado");
        problemDetail.setDetail(exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ProblemDetail> handleConflict(RuntimeException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("Conflito de negócio");
        problemDetail.setDetail(exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler({ CredenciaisInvalidasException.class, JwtException.class })
    public ResponseEntity<ProblemDetail> handleUnauthorized(Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problemDetail.setTitle("Autenticação inválida");
        problemDetail.setDetail("Credenciais inválidas ou token expirado");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    @ExceptionHandler({ EstoqueInsuficienteException.class, RegraDeProdutoException.class,
            QuantidadeInvalidaException.class })
    public ResponseEntity<ProblemDetail> handleBadRequest(RuntimeException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Solicitação inválida");
        problemDetail.setDetail(exception.getMessage());
        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validação falhou");
        problemDetail.setDetail("Campos inválidos na requisição");
        problemDetail.setProperty("errors", exception.getFieldErrors().stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", error.getDefaultMessage()))
                .collect(Collectors.toList()));
        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validação falhou");
        problemDetail.setDetail("Campos inválidos na requisição");
        problemDetail.setProperty("errors", exception.getConstraintViolations().stream()
                .map(violation -> Map.of(
                        "field", violation.getPropertyPath().toString(),
                        "message", violation.getMessage()))
                .collect(Collectors.toList()));
        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> handleResponseStatusException(ResponseStatusException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(exception.getStatusCode());
        problemDetail.setTitle("Erro na requisição");
        problemDetail.setDetail(exception.getReason());
        return ResponseEntity.status(exception.getStatusCode()).body(problemDetail);
    }

    @ExceptionHandler(FalhaProcessamentoImagemException.class)
    public ResponseEntity<ProblemDetail> handleFalhaProcessamentoImagem(FalhaProcessamentoImagemException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_GATEWAY);
        problemDetail.setTitle("Falha no upload da imagem");
        problemDetail.setDetail(exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Erro interno");
        problemDetail.setDetail("Ocorreu um erro inesperado");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }
}