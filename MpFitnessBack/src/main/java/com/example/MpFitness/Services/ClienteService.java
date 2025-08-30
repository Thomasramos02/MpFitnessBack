package com.example.MpFitness.Services;

import com.example.MpFitness.Model.Cliente;
import com.example.MpFitness.Repositories.ClienteRepository;
import jakarta.transaction.Transactional;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Cliente findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id do cliente <UNK> null");
        }
        return clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("id do cliente <UNK> null"));
    }

    @Transactional
    public Cliente criar(Cliente cliente) {
        if (clienteRepository.findByEmail(cliente.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email ja esta em uso");
        }
        if (cliente.getSenha() != null && !cliente.getSenha().isBlank()) {
            cliente.setSenha(passwordEncoder.encode(cliente.getSenha()));
        }

        return clienteRepository.save(cliente);
    }

    public Cliente autenticar(String email, String senha) {
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (!passwordEncoder.matches(senha, cliente.getSenha())) {
            throw new IllegalArgumentException("Senha incorreta");
        }

        return cliente;
    }

    public Optional<Cliente> buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email);
    }

    public void alterarSenha(String email, String novaSenha) {
    Optional<Cliente> optionalCliente = buscarPorEmail(email);
    if (optionalCliente.isEmpty()) {
        throw new IllegalArgumentException("Usuário não encontrado");
    }
    Cliente cliente = optionalCliente.get();
    cliente.setSenha(passwordEncoder.encode(novaSenha));
    clienteRepository.save(cliente);
}


}
