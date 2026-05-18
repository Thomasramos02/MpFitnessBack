package com.example.MpFitness.Controllers;

import com.example.MpFitness.DTO.AtualizarClienteRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;

import com.example.MpFitness.DTO.ClienteTelefoneCart;
import com.example.MpFitness.DTO.EnderecoDTO;
import com.example.MpFitness.DTO.NovaSenhaRequest;
import com.example.MpFitness.Model.Cliente;
import com.example.MpFitness.Security.JwtUtils;
import com.example.MpFitness.Services.ClienteService;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final JwtUtils jwtUtils;

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("Authorization") String token) {
        Long userId = extractUserIdFromAuthorization(token);
        Cliente cliente = clienteService.findById(userId);
        cliente.setSenha(null);
        return ResponseEntity.ok(cliente);
    }

    @GetMapping("/telefone")
    public ResponseEntity<?> getTelefone(@RequestHeader("Authorization") String token) {
        Long userId = extractUserIdFromAuthorization(token);
        Cliente cliente = clienteService.findById(userId);

        if (cliente.getTelefone() == null || cliente.getTelefone().isBlank()) {
            return ResponseEntity.notFound().build();
        }
        ClienteTelefoneCart telefoneDTO = new ClienteTelefoneCart();
        telefoneDTO.setTelefone(cliente.getTelefone());
        return ResponseEntity.ok(telefoneDTO);
    }

    @GetMapping("/endereco")
    public ResponseEntity<?> getEndereco(@RequestHeader("Authorization") String token) {
        Long userId = extractUserIdFromAuthorization(token);
        Cliente cliente = clienteService.findById(userId);

        if (cliente.getEndereco() == null) {
            return ResponseEntity.notFound().build();
        }

        EnderecoDTO enderecoDTO = new EnderecoDTO();
        enderecoDTO.setCep(cliente.getEndereco().getCep());
        enderecoDTO.setRua(cliente.getEndereco().getRua());
        enderecoDTO.setNumero(cliente.getEndereco().getNumero());
        enderecoDTO.setComplemento(cliente.getEndereco().getComplemento());
        enderecoDTO.setBairro(cliente.getEndereco().getBairro());
        enderecoDTO.setCidade(cliente.getEndereco().getCidade());
        enderecoDTO.setEstado(cliente.getEndereco().getEstado());

        return ResponseEntity.ok(enderecoDTO);
    }

    @PutMapping("/atualizar")
    public ResponseEntity<?> atualizar(@RequestHeader("Authorization") String token,
            @Valid @RequestBody AtualizarClienteRequestDto clienteAtualizado) {
        Long userId = extractUserIdFromAuthorization(token);
        Cliente clienteExistente = clienteService.atualizar(userId, clienteAtualizado);
        return ResponseEntity.ok(clienteExistente);
    }

    @DeleteMapping("/remover")
    public ResponseEntity<?> remover(@RequestHeader("Authorization") String token) {
        Long userId = extractUserIdFromAuthorization(token);
        clienteService.remover(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/atualizar-senha")
    public ResponseEntity<?> atualizarSenha(@RequestHeader("Authorization") String token,
            @Valid @RequestBody NovaSenhaRequest request) {
        Long userId = extractUserIdFromAuthorization(token);
        clienteService.alterarSenha(userId, request.getNovaSenha());

        return ResponseEntity.ok("Senha atualizada com sucesso!");
    }

    private Long extractUserIdFromAuthorization(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token não informado");
        }

        String token = authorizationHeader.startsWith("Bearer ")
                ? authorizationHeader.substring(7).trim()
                : authorizationHeader.trim();

        if (token.isBlank() || "null".equalsIgnoreCase(token) || "undefined".equalsIgnoreCase(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }

        try {
            return jwtUtils.extractId(token);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }
    }

}