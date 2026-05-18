package com.example.MpFitness.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.MpFitness.Model.Produto;
import com.example.MpFitness.Model.CategoriaEnum;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByStatus(String status);

    List<Produto> findByCategoriaAndStatus(CategoriaEnum categoria, String status);

    List<Produto> findTop10ByStatusOrderByVisualizacoesDesc(String status);

    List<Produto> findTop20ByStatusOrderByVisualizacoesDesc(String status);
}