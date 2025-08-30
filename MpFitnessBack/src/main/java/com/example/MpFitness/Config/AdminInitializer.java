package com.example.MpFitness.Config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.MpFitness.Model.Cliente;
import com.example.MpFitness.Model.Endereco;
import com.example.MpFitness.Model.Cliente.Role;
import com.example.MpFitness.Repositories.ClienteRepository;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Component
@Profile("prod") // 🔒 garante que só roda no profile de PRODUÇÃO
public class AdminInitializer implements CommandLineRunner {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(ClienteRepository clienteRepository,
                            PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // lista de admins a serem criados
        List<Cliente> admins = List.of(
                createAdminUser("Administrador Master", "admin1@mpfitness.com", "SenhaForte@123"),
                createAdminUser("Administrador Secundário", "admin2@mpfitness.com", "SenhaForte@123"),
                createAdminUser("Administrador Suporte", "admin3@mpfitness.com", "SenhaForte@123")
        );

        admins.forEach(admin -> {
            try {
                if (clienteRepository.findByEmail(admin.getEmail()).isEmpty()) {
                    clienteRepository.save(admin);
                    log.info("✅ Conta admin criada: {}", admin.getEmail());
                } else {
                    log.info("⏩ Conta admin já existe: {}", admin.getEmail());
                }
            } catch (Exception e) {
                log.error("❌ Erro ao criar conta admin {}", admin.getEmail(), e);
            }
        });
    }

    private Cliente createAdminUser(String nome, String email, String senha) {
        Cliente admin = new Cliente();
        admin.setNome(nome);
        admin.setEmail(email);
        admin.setSenha(passwordEncoder.encode(senha)); // senha codificada
        admin.setTelefone("(11) 91234-5678");
        admin.setRole(Role.ADMIN);

        Endereco endereco = new Endereco();
        endereco.setCep("00000-000");
        endereco.setNumero("0");
        endereco.setComplemento("Admin System");
        endereco.setBairro("Central");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");

        admin.setEndereco(endereco);

        return admin;
    }
}
