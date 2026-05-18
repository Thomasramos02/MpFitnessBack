package com.example.MpFitness.Repositories;

import com.example.MpFitness.Model.WishlistItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByClienteIdOrderByCriadoEmDesc(Long clienteId);

    Optional<WishlistItem> findByClienteIdAndProdutoId(Long clienteId, Long produtoId);

    void deleteByClienteIdAndProdutoId(Long clienteId, Long produtoId);
}
