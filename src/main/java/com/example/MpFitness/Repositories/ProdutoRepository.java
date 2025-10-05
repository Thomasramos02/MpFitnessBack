package com.example.MpFitness.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.MpFitness.Model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByStatus(String status);

    List<Produto> findByCategoriaAndStatus(String categoria, String status);

    /*Como a classe lista por status, status e categoria, seria bom listar apenas por categoria, já que um cliente por exemplo deseja apenas visualizar os produtos da categoria "Musculação" por exemplo. Esse método seria escrito dessa forma: List<Produto> findByCategoria(String categoria) */

    
}