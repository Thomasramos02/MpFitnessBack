package com.example.MpFitness;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MpFitnessApplication {

    public static void main(String[] args) {
        // Carrega o arquivo .env na raiz do projeto
        Dotenv dotenv = Dotenv.load();

        // Define as variáveis de ambiente para o Spring Boot ler
        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        System.setProperty("UPLOAD_DIR", dotenv.get("UPLOAD_DIR"));
        System.setProperty("MERCADO_PAGO_ACCESS_TOKEN", dotenv.get("MERCADO_PAGO_ACCESS_TOKEN"));
        System.setProperty("CLOUDINARY_CLOUD_NAME", dotenv.get("CLOUDINARY_CLOUD_NAME"));
        System.setProperty("CLOUDINARY_API_KEY", dotenv.get("CLOUDINARY_API_KEY"));
        System.setProperty("CLOUDINARY_API_SECRET", dotenv.get("CLOUDINARY_API_SECRET"));
        System.setProperty("GOOGLE_CLIENT_ID", dotenv.get("GOOGLE_CLIENT_ID"));
        System.setProperty("GOOGLE_CLIENT_SECRET", dotenv.get("GOOGLE_CLIENT_SECRET"));
        System.setProperty("FRONTEND_URL", dotenv.get("FRONTEND_URL"));

        SpringApplication.run(MpFitnessApplication.class, args);
    }
}
