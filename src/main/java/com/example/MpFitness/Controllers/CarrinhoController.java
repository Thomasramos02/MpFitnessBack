package com.example.MpFitness.Controllers;

import com.example.MpFitness.Model.Carrinho;
import com.example.MpFitness.Security.AuthenticatedUser;
import com.example.MpFitness.Services.CarrinhoService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes/{clienteId}/carrinho")
@RequiredArgsConstructor
@Validated
public class CarrinhoController {

    private final CarrinhoService carrinhoService;

    @PostMapping("/adicionar/{produtoId}")
    @PreAuthorize("#clienteId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<?> adicionarProduto(
            @PathVariable Long clienteId,
            @PathVariable Long produtoId,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(defaultValue = "1") @Min(1) Integer quantidade) {
        Long clienteIdEfetivo = isAdmin(currentUser) ? clienteId : currentUser.getId();
        Carrinho carrinho = carrinhoService.adicionarProdutoAoCarrinho(produtoId, clienteIdEfetivo, quantidade);
        return ResponseEntity.ok(carrinho);
    }

    @DeleteMapping("/remover/{produtoId}")
    @PreAuthorize("#clienteId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<Carrinho> removerProduto(
            @PathVariable Long clienteId,
            @PathVariable Long produtoId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        Long clienteIdEfetivo = isAdmin(currentUser) ? clienteId : currentUser.getId();
        Carrinho carrinho = carrinhoService.removerItem(clienteIdEfetivo, produtoId);
        return ResponseEntity.ok(carrinho);
    }

    @GetMapping
    @PreAuthorize("#clienteId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<Carrinho> visualizarCarrinho(
            @PathVariable Long clienteId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        Long clienteIdEfetivo = isAdmin(currentUser) ? clienteId : currentUser.getId();
        Carrinho carrinho = carrinhoService.visualizarCarrinho(clienteIdEfetivo);
        return ResponseEntity.ok(carrinho);
    }

    @PutMapping("/atualizar/{produtoId}")
    @PreAuthorize("#clienteId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<?> atualizarQuantidadeProduto(
            @PathVariable Long clienteId,
            @PathVariable Long produtoId,
            @RequestParam @Min(1) Integer quantidade,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        Long clienteIdEfetivo = isAdmin(currentUser) ? clienteId : currentUser.getId();
        Carrinho carrinhoAtualizado = carrinhoService.atualizarQuantidadeItem(clienteIdEfetivo, produtoId, quantidade);
        return ResponseEntity.ok(carrinhoAtualizado);
    }

    private boolean isAdmin(AuthenticatedUser currentUser) {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }
}
