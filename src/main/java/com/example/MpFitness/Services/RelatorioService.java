package com.example.MpFitness.Services;

import com.example.MpFitness.Model.Pedido;
import com.example.MpFitness.Repositories.PedidoRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final PedidoRepository pedidoRepository;

    public List<Pedido> gerarRelatorioMensal(int mes, int ano) {
        return pedidoRepository.findByMesEAno(mes, ano);
    }
}
