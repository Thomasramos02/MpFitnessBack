package com.example.MpFitness.Controllers;

import com.example.MpFitness.DTO.ClienteAdminDTO;
import com.example.MpFitness.DTO.RupturaDTO;
import com.example.MpFitness.Services.AdminMetricasService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/metricas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMetricasController {

    private final AdminMetricasService adminMetricasService;

    @GetMapping("/produtos-mais-vistos")
    public ResponseEntity<?> listarProdutosMaisVistos(@RequestParam(defaultValue = "10") int limite) {
        return ResponseEntity.ok(adminMetricasService.listarProdutosMaisVistos(limite));
    }

    @GetMapping("/abandono-checkout")
    public ResponseEntity<?> calcularAbandonoCheckout(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {

        LocalDateTime fimEfetivo = fim == null ? LocalDateTime.now() : fim;
        LocalDateTime inicioEfetivo = inicio == null ? fimEfetivo.minusDays(30) : inicio;

        return ResponseEntity.ok(adminMetricasService.calcularAbandonoCheckout(inicioEfetivo, fimEfetivo));
    }

    @GetMapping("/recorrencia-detalhada")
    public ResponseEntity<?> recorrenciaDetalhada() {
        return ResponseEntity.ok(adminMetricasService.calcularRecorrenciaDetalhada());
    }

    @GetMapping("/clientes")
    public ResponseEntity<List<ClienteAdminDTO>> listarTodosClientes() {
        return ResponseEntity.ok(adminMetricasService.listarTodosClientes());
    }

    @GetMapping("/rupturas")
    public ResponseEntity<List<RupturaDTO>> listarRupturas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            @RequestParam(defaultValue = "10") int limite) {

        LocalDateTime fimEfetivo = fim == null ? LocalDateTime.now() : fim;
        LocalDateTime inicioEfetivo = inicio == null ? fimEfetivo.minusDays(30) : inicio;

        return ResponseEntity.ok(adminMetricasService.listarRupturas(inicioEfetivo, fimEfetivo, limite));
    }
}
