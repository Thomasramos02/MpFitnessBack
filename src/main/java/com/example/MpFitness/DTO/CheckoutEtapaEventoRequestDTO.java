package com.example.MpFitness.DTO;

import com.example.MpFitness.Model.CheckoutEtapaEvento.EtapaCheckout;
import com.example.MpFitness.Model.CheckoutEtapaEvento.TipoEventoCheckout;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutEtapaEventoRequestDTO {

    @NotBlank
    private String sessionId;

    private Long clienteId;

    @NotNull
    private EtapaCheckout etapa;

    @NotNull
    private TipoEventoCheckout evento;
}
