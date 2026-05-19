package com.example.MpFitness.Config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.MpFitness.Model.Cliente;
import com.example.MpFitness.Model.Endereco;
import com.example.MpFitness.Model.Cliente.Role;
import com.example.MpFitness.Repositories.ClienteRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AdminInitializer implements CommandLineRunner {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminInitialEmail;
    private final String adminInitialPassword;

    public AdminInitializer(ClienteRepository clienteRepository,
            PasswordEncoder passwordEncoder,
            @Value("${ADMIN_INITIAL_EMAIL:}") String adminInitialEmail,
            @Value("${ADMIN_INITIAL_PASSWORD:}") String adminInitialPassword) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminInitialEmail = adminInitialEmail == null ? "" : adminInitialEmail.trim().toLowerCase();
        this.adminInitialPassword = adminInitialPassword == null ? "" : adminInitialPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (adminInitialEmail.isBlank() || adminInitialPassword.isBlank()) {
            log.info(
                    "⏭️ Bootstrap de admin desativado. Configure ADMIN_INITIAL_EMAIL e ADMIN_INITIAL_PASSWORD para habilitar.");
            return;
        }

        final String adminEmail = adminInitialEmail;
        final String encodedPassword = passwordEncoder.encode(adminInitialPassword);

        try {
            var existingAdminOpt = clienteRepository.findByEmail(adminEmail);

            if (existingAdminOpt.isEmpty()) {
                Cliente admin = createAdminUser(adminEmail, encodedPassword);
                clienteRepository.save(admin);

                log.info("✅ Conta admin criada com sucesso!");
                log.info("Email: {}", adminEmail);
                log.info("Senha inicial definida no bootstrap (ADMIN_INITIAL_PASSWORD ou valor padrão). ");
            } else {
                Cliente existingAdmin = existingAdminOpt.get();
                boolean updated = false;

                if (existingAdmin.getSenha() == null || !existingAdmin.getSenha().startsWith("$2")) {
                    existingAdmin.setSenha(encodedPassword);
                    updated = true;
                } else if (!passwordEncoder.matches(adminInitialPassword, existingAdmin.getSenha())) {
                    existingAdmin.setSenha(encodedPassword);
                    updated = true;
                }

                if (existingAdmin.getRole() != Role.ADMIN) {
                    existingAdmin.setRole(Role.ADMIN);
                    updated = true;
                }

                if (updated) {
                    clienteRepository.save(existingAdmin);
                    log.info("🔐 Conta admin existente foi atualizada (senha criptografada/role ADMIN).");
                } else {
                    log.info("⏩ Conta admin já existe, nenhuma ação necessária");
                }
            }
        } catch (Exception e) {
            log.error("❌ Erro ao criar conta admin", e);
        }
    }

    private Cliente createAdminUser(String adminEmail, String encodedPassword) {
        Cliente admin = new Cliente();
        admin.setNome("Administrador Master");
        admin.setEmail(adminEmail);
        admin.setSenha(encodedPassword);
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
