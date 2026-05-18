package com.example.MpFitness.Repositories;

import com.example.MpFitness.Model.CheckoutEtapaEvento;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckoutEtapaEventoRepository extends JpaRepository<CheckoutEtapaEvento, Long> {

    interface EtapaEventoCountProjection {
        String getEtapa();

        String getEvento();

        Long getTotal();
    }

    @Query("""
            SELECT e.etapa AS etapa,
                   e.evento AS evento,
                   COUNT(e) AS total
            FROM CheckoutEtapaEvento e
            WHERE e.criadoEm BETWEEN :inicio AND :fim
            GROUP BY e.etapa, e.evento
            """)
    List<EtapaEventoCountProjection> countByEtapaAndEvento(LocalDateTime inicio, LocalDateTime fim);
}
