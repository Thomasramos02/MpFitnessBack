package com.example.MpFitness.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.MpFitness.Model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByStatus(String status);

    List<Produto> findByCategoriaAndStatus(String categoria, String status);
}