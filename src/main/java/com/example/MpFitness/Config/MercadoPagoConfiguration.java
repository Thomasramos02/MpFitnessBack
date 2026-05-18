package com.example.MpFitness.Config;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Configuration
public class MercadoPagoConfiguration {

    private final String accessToken;

    public MercadoPagoConfiguration(@Value("${mercado_pago.access_token:}") String accessToken) {
        this.accessToken = accessToken;
    }

    @PostConstruct
    public void init() {
        if (accessToken == null || accessToken.isBlank()) {
            log.warn("MERCADO_PAGO_ACCESS_TOKEN not configured. Mercado Pago integration will stay disabled.");
            return;
        }

        MercadoPagoConfig.setAccessToken(accessToken);
    }
}
