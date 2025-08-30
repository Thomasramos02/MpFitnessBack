package com.example.MpFitness.Controllers;

import com.example.MpFitness.Model.Carrinho;
import com.example.MpFitness.Security.JwtUtils;
import com.example.MpFitness.Services.CarrinhoService;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes/{clienteId}/carrinho")
@RequiredArgsConstructor
public class CarrinhoController {

    private final CarrinhoService carrinhoService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/adicionar/{produtoId}")
    public ResponseEntity<?> adicionarProduto(
            @PathVariable Long produtoId,
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") Integer quantidade) { // Adicionado parâmetro de quantidade

        try {
            String tokenLimpo = token.startsWith("Bearer ") ? token.substring(7) : token;
            Long userId = jwtUtils.extractId(tokenLimpo);

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Token inválido ou expirado");
            }

            // Valida quantidade mínima
            if (quantidade < 1) {
                return ResponseEntity.badRequest()
                        .body("Quantidade deve ser pelo menos 1");
            }

            Carrinho carrinho = carrinhoService.adicionaCarrinho(produtoId, userId, quantidade);
            return ResponseEntity.ok(carrinho);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao adicionar produto: " + e.getMessage());
        }
    }

    @DeleteMapping("/remover/{produtoId}")
    public ResponseEntity<Carrinho> removerProduto(@PathVariable Long clienteId, @PathVariable Long produtoId) {

        Carrinho carrinho = carrinhoService.removerItem(clienteId, produtoId);
        return ResponseEntity.ok(carrinho);
    }

    @GetMapping
    public ResponseEntity<Carrinho> visualizarCarrinho(@PathVariable Long clienteId) {
        Carrinho carrinho = carrinhoService.visualizarCarrinho(clienteId);
        return ResponseEntity.ok(carrinho);
    }

    @PutMapping("/atualizar/{produtoId}")
    public ResponseEntity<?> atualizarQuantidadeProduto(
            @PathVariable Long produtoId,
            @RequestParam Integer quantidade,
            @RequestHeader("Authorization") String token) {

        try {
            String tokenLimpo = token.startsWith("Bearer ") ? token.substring(7) : token;
            Long clienteId = jwtUtils.extractId(tokenLimpo);

            if (clienteId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Token inválido ou não autorizado.");
            }

            if (quantidade == null || quantidade < 1) {
                return ResponseEntity.badRequest()
                        .body("A quantidade deve ser de no mínimo 1.");
            }
            Carrinho carrinhoAtualizado = carrinhoService.atualizarQuantidadeItem(clienteId, produtoId, quantidade);
            return ResponseEntity.ok(carrinhoAtualizado);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atualizar a quantidade do produto: " + e.getMessage());
        }
    }
}
