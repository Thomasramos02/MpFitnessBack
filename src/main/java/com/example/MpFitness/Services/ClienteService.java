package com.example.MpFitness.Services;

import com.example.MpFitness.DTO.AtualizarClienteRequestDto;
import com.example.MpFitness.DTO.RegisterRequestDto;
import com.example.MpFitness.Model.Cliente;
import com.example.MpFitness.Model.Endereco;
import com.example.MpFitness.Model.Cliente.Role;
import com.example.MpFitness.Repositories.ClienteRepository;
import com.example.MpFitness.exceptions.ClienteNaoEncontradoException;
import com.example.MpFitness.exceptions.CredenciaisInvalidasException;
import com.example.MpFitness.exceptions.EmailJaCadastradoException;
import jakarta.transaction.Transactional;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private static final String TELEFONE_PADRAO = "";

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public Cliente findById(Long id) {
        if (id == null) {
            throw new ClienteNaoEncontradoException("Id do cliente inválido");
        }
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNaoEncontradoException(id));
    }

    @Transactional
    public Cliente criar(RegisterRequestDto request) {
        if (clienteRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailJaCadastradoException(request.getEmail());
        }

        Cliente cliente = new Cliente();
        cliente.setEmail(request.getEmail());
        cliente.setNome(request.getNome());
        cliente.setRole(Role.CLIENTE);
        cliente.setTelefone(TELEFONE_PADRAO);

        if (request.getSenha() != null && !request.getSenha().isBlank()) {
            cliente.setSenha(passwordEncoder.encode(request.getSenha()));
        }

        return clienteRepository.save(cliente);
    }

    public Cliente autenticar(String email, String senha) {
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!passwordEncoder.matches(senha, cliente.getSenha())) {
            throw new CredenciaisInvalidasException();
        }

        return cliente;
    }

    public Optional<Cliente> buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email);
    }

    @Transactional
    public Cliente atualizar(Long id, AtualizarClienteRequestDto request) {
        Cliente cliente = findById(id);

        clienteRepository.findByEmail(request.getEmail())
                .filter(clienteExistente -> !clienteExistente.getId().equals(cliente.getId()))
                .ifPresent(clienteExistente -> {
                    throw new EmailJaCadastradoException(clienteExistente.getEmail());
                });

        cliente.setNome(request.getNome());
        cliente.setEmail(request.getEmail());
        cliente.setTelefone(normalizarTelefone(request.getTelefone()));
        cliente.setEndereco(new Endereco(
                request.getEndereco().getRua(),
                request.getEndereco().getNumero(),
                request.getEndereco().getComplemento(),
                request.getEndereco().getBairro(),
                request.getEndereco().getCidade(),
                request.getEndereco().getEstado(),
                request.getEndereco().getCep(),
                cliente.getEndereco() != null ? cliente.getEndereco().getPais() : null));

        return clienteRepository.save(cliente);
    }

    @Transactional
    public void remover(Long id) {
        clienteRepository.delete(findById(id));
    }

    @Transactional
    public Cliente alterarSenha(Long id, String novaSenha) {
        Cliente cliente = findById(id);
        cliente.setSenha(passwordEncoder.encode(novaSenha));
        return clienteRepository.save(cliente);
    }

    private String normalizarTelefone(String telefone) {
        if (telefone == null) {
            return TELEFONE_PADRAO;
        }
        return telefone.trim();
    }
}
