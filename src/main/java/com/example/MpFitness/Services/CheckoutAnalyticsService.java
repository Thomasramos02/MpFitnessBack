package com.example.MpFitness.Services;

import com.example.MpFitness.DTO.CheckoutAbandonoEtapaDTO;
import com.example.MpFitness.DTO.CheckoutEtapaEventoRequestDTO;
import com.example.MpFitness.Model.CheckoutEtapaEvento;
import com.example.MpFitness.Model.CheckoutEtapaEvento.EtapaCheckout;
import com.example.MpFitness.Model.CheckoutEtapaEvento.TipoEventoCheckout;
import com.example.MpFitness.Repositories.CheckoutEtapaEventoRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckoutAnalyticsService {

    private final CheckoutEtapaEventoRepository checkoutEtapaEventoRepository;

    @Transactional
    public void registrarEvento(CheckoutEtapaEventoRequestDTO request) {
        CheckoutEtapaEvento evento = new CheckoutEtapaEvento();
        evento.setSessionId(request.getSessionId());
        evento.setClienteId(request.getClienteId());
        evento.setEtapa(request.getEtapa());
        evento.setEvento(request.getEvento());
        evento.setCriadoEm(LocalDateTime.now());
        checkoutEtapaEventoRepository.save(evento);
    }

    public List<CheckoutAbandonoEtapaDTO> calcularAbandonoPorEtapa(LocalDateTime inicio, LocalDateTime fim) {
        List<CheckoutEtapaEventoRepository.EtapaEventoCountProjection> dados = buscarContagemEventosPorEtapa(inicio,
                fim);
        EnumMap<EtapaCheckout, EnumMap<TipoEventoCheckout, Long>> mapaAgrupado = inicializarMapaAgrupado();

        preencherMapaComDados(mapaAgrupado, dados);
        return montarResultadoAbandono(mapaAgrupado);
    }

    private List<CheckoutEtapaEventoRepository.EtapaEventoCountProjection> buscarContagemEventosPorEtapa(
            LocalDateTime inicio,
            LocalDateTime fim) {
        return checkoutEtapaEventoRepository.countByEtapaAndEvento(inicio, fim);
    }

    private EnumMap<EtapaCheckout, EnumMap<TipoEventoCheckout, Long>> inicializarMapaAgrupado() {
        EnumMap<EtapaCheckout, EnumMap<TipoEventoCheckout, Long>> agrupado = new EnumMap<>(EtapaCheckout.class);

        for (EtapaCheckout etapa : EtapaCheckout.values()) {
            EnumMap<TipoEventoCheckout, Long> eventosPorTipo = new EnumMap<>(TipoEventoCheckout.class);
            for (TipoEventoCheckout tipo : TipoEventoCheckout.values()) {
                eventosPorTipo.put(tipo, 0L);
            }
            agrupado.put(etapa, eventosPorTipo);
        }

        return agrupado;
    }

    private void preencherMapaComDados(
            EnumMap<EtapaCheckout, EnumMap<TipoEventoCheckout, Long>> mapaAgrupado,
            List<CheckoutEtapaEventoRepository.EtapaEventoCountProjection> dados) {
        for (CheckoutEtapaEventoRepository.EtapaEventoCountProjection linha : dados) {
            EtapaCheckout etapa = EtapaCheckout.valueOf(linha.getEtapa());
            TipoEventoCheckout evento = TipoEventoCheckout.valueOf(linha.getEvento());
            mapaAgrupado.get(etapa).put(evento, linha.getTotal());
        }
    }

    private List<CheckoutAbandonoEtapaDTO> montarResultadoAbandono(
            EnumMap<EtapaCheckout, EnumMap<TipoEventoCheckout, Long>> mapaAgrupado) {
        List<CheckoutAbandonoEtapaDTO> resultado = new ArrayList<>();

        for (EtapaCheckout etapa : EtapaCheckout.values()) {
            long entradas = mapaAgrupado.get(etapa).get(TipoEventoCheckout.ENTRADA);
            long conclusoes = mapaAgrupado.get(etapa).get(TipoEventoCheckout.CONCLUSAO);
            long abandonos = mapaAgrupado.get(etapa).get(TipoEventoCheckout.ABANDONO);

            resultado.add(new CheckoutAbandonoEtapaDTO(
                    etapa.name(),
                    entradas,
                    conclusoes,
                    abandonos,
                    calcularTaxaAbandono(entradas, abandonos)));
        }

        return resultado;
    }

    private double calcularTaxaAbandono(long entradas, long abandonos) {
        if (entradas <= 0) {
            return 0.0;
        }

        double taxaAbandono = (abandonos * 100.0) / entradas;
        return Math.round(taxaAbandono * 100.0) / 100.0;
    }
}
