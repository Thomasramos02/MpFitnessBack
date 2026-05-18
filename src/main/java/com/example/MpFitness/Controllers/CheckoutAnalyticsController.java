package com.example.MpFitness.Controllers;

import com.example.MpFitness.DTO.CheckoutEtapaEventoRequestDTO;
import com.example.MpFitness.Services.CheckoutAnalyticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout-analytics")
@RequiredArgsConstructor
public class CheckoutAnalyticsController {

    private final CheckoutAnalyticsService checkoutAnalyticsService;

    @PostMapping("/eventos")
    public ResponseEntity<?> registrarEvento(@Valid @RequestBody CheckoutEtapaEventoRequestDTO request) {
        checkoutAnalyticsService.registrarEvento(request);
        return ResponseEntity.ok().build();
    }
}
