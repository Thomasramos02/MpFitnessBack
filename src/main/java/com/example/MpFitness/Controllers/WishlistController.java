package com.example.MpFitness.Controllers;

import com.example.MpFitness.Services.WishlistService;
import com.example.MpFitness.Security.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final JwtUtils jwtUtils;

    public WishlistController(WishlistService wishlistService, JwtUtils jwtUtils) {
        this.wishlistService = wishlistService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/me")
    public ResponseEntity<?> listar(@RequestHeader("Authorization") String token) {
        Long clienteId = extrairClienteId(token);
        return ResponseEntity.ok(wishlistService.listar(clienteId));
    }

    @PostMapping("/{produtoId}")
    public ResponseEntity<?> adicionar(@RequestHeader("Authorization") String token, @PathVariable Long produtoId) {
        Long clienteId = extrairClienteId(token);
        return ResponseEntity.ok(wishlistService.adicionar(clienteId, produtoId));
    }

    @DeleteMapping("/{produtoId}")
    public ResponseEntity<?> remover(@RequestHeader("Authorization") String token, @PathVariable Long produtoId) {
        Long clienteId = extrairClienteId(token);
        return ResponseEntity.ok(wishlistService.remover(clienteId, produtoId));
    }

    private Long extrairClienteId(String token) {
        String tokenLimpo = token.startsWith("Bearer ") ? token.substring(7) : token;
        return jwtUtils.extractId(tokenLimpo);
    }
}
