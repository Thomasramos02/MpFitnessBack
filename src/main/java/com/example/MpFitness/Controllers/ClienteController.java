package com.example.MpFitness.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.MpFitness.DTO.ClienteTelefoneCart;
import com.example.MpFitness.DTO.EnderecoDTO;
import com.example.MpFitness.DTO.NovaSenhaRequest;
import com.example.MpFitness.Model.Cliente;
import com.example.MpFitness.Repositories.ClienteRepository;
import com.example.MpFitness.Security.JwtUtils;
import com.example.MpFitness.Services.ClienteService;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("Authorization") String token) {
        String tokenLimpo = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long userId = jwtUtils.extractId(tokenLimpo);
        Cliente cliente = clienteService.findById(userId);

        if (cliente == null) {
            return ResponseEntity.notFound().build();
        }

        cliente.setSenha(null);
        return ResponseEntity.ok(cliente);
    }

    @GetMapping("/telefone")
    public ResponseEntity<?> getTelefone(@RequestHeader("Authorization") String token) {
        String tokenLimpo = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long userId = jwtUtils.extractId(tokenLimpo);
        Cliente cliente = clienteService.findById(userId);

        if (cliente == null || cliente.getTelefone() == null) {
            return ResponseEntity.notFound().build();
        }
        ClienteTelefoneCart telefoneDTO = new ClienteTelefoneCart();
        telefoneDTO.setTelefone(cliente.getTelefone());
        return ResponseEntity.ok(telefoneDTO);
    }

    @GetMapping("/endereco")
    public ResponseEntity<?> getEndereco(@RequestHeader("Authorization") String token) {
        String tokenLimpo = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long userId = jwtUtils.extractId(tokenLimpo);
        Cliente cliente = clienteService.findById(userId);

        if (cliente == null || cliente.getEndereco() == null) {
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
            @RequestBody Cliente clienteAtualizado) {
        String tokenLimpo = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long userId = jwtUtils.extractId(tokenLimpo);
        Cliente clienteExistente = clienteService.findById(userId);

        if (clienteExistente == null) {
            return ResponseEntity.notFound().build();
        }

        clienteExistente.setNome(clienteAtualizado.getNome());
        clienteExistente.setEmail(clienteAtualizado.getEmail());
        clienteExistente.setEndereco(clienteAtualizado.getEndereco());
        clienteExistente.setTelefone(clienteAtualizado.getTelefone());

        clienteRepository.save(clienteExistente);

        return ResponseEntity.ok(clienteExistente);
    }

    @DeleteMapping("/remover")
    public ResponseEntity<?> remover(@RequestHeader("Authorization") String token) {
        String tokenLimpo = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long userId = jwtUtils.extractId(tokenLimpo);
        Cliente clienteExistente = clienteService.findById(userId);

        if (clienteExistente == null) {
            return ResponseEntity.notFound().build();
        }

        clienteRepository.delete(clienteExistente);
        return ResponseEntity.noContent().build();
    }

   @PutMapping("/atualizar-senha")
    public ResponseEntity<?> atualizarSenha(@RequestHeader("Authorization") String token,
                                            @RequestBody NovaSenhaRequest request) {
        String tokenLimpo = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long userId = jwtUtils.extractId(tokenLimpo);
        Cliente clienteExistente = clienteService.findById(userId);

        if (clienteExistente == null) {
            return ResponseEntity.notFound().build();
        }

        clienteExistente.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        clienteRepository.save(clienteExistente);

        return ResponseEntity.ok("Senha atualizada com sucesso!");
    }


}