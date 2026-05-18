package com.example.MpFitness.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SchemaMigrationInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public SchemaMigrationInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("""
                    ALTER TABLE pedidos
                    ADD COLUMN IF NOT EXISTS valor_frete NUMERIC(10,2) NOT NULL DEFAULT 0
                    """);

            jdbcTemplate.execute("""
                    ALTER TABLE pedidos
                    ADD COLUMN IF NOT EXISTS valor_produtos NUMERIC(10,2)
                    """);

            jdbcTemplate.execute("""
                    UPDATE pedidos
                    SET valor_frete = 0
                    WHERE valor_frete IS NULL
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE produtos ADD COLUMN IF NOT EXISTS
                     visualizacoes NUMERIC""");

            jdbcTemplate.execute("""
                    UPDATE produtos
                    SET categoria_produto = CASE
                        WHEN lower(categoria_produto) IN ('suplementos', 'suplemento') THEN 'SUPLEMENTOS'
                        WHEN lower(categoria_produto) IN ('roupas', 'roupa', 'roupas academia', 'roupas_academia', 'roupas-academia') THEN 'ROUPAS_ACADEMIA'
                        WHEN lower(categoria_produto) IN ('calçados', 'calcados', 'tênis', 'tenis', 'calçados esportivos', 'calcados esportivos', 'calcados_esportivos', 'calcados-esportivos') THEN 'CALCADOS_ESPORTIVOS'
                        WHEN lower(categoria_produto) IN ('acessórios', 'acessorios', 'acessórios esportivos', 'acessorios esportivos', 'acessorios_esportivos', 'acessorios-esportivos') THEN 'ACESSORIOS_ESPORTIVOS'
                        WHEN lower(categoria_produto) IN ('geral') THEN 'GERAL'
                        WHEN lower(categoria_produto) IN ('equipamento', 'equipamentos') THEN 'EQUIPAMENTOS'
                        ELSE categoria_produto
                    END
                    WHERE categoria_produto IS NOT NULL
                    """);

            jdbcTemplate.execute("""
                    UPDATE produtos
                    SET categoria_produto = 'GERAL'
                    WHERE categoria_produto IS NULL
                       OR categoria_produto NOT IN (
                           'CALCADOS_ESPORTIVOS',
                           'ROUPAS_ACADEMIA',
                           'SUPLEMENTOS',
                           'ACESSORIOS_ESPORTIVOS',
                           'EQUIPAMENTOS',
                           'GERAL'
                       )
                    """);

            jdbcTemplate.execute("""
                    UPDATE pedidos
                    SET valor_produtos = COALESCE(valor_total, 0) - COALESCE(valor_frete, 0)
                    WHERE valor_produtos IS NULL
                    """);

            jdbcTemplate.execute("""
                    ALTER TABLE pedidos
                    ALTER COLUMN valor_frete SET DEFAULT 0
                    """);

            jdbcTemplate.execute("""
                    ALTER TABLE pedidos
                    ALTER COLUMN valor_frete SET NOT NULL
                    """);

            jdbcTemplate.execute("""
                    ALTER TABLE pedidos
                    ALTER COLUMN valor_produtos SET DEFAULT 0
                    """);

            jdbcTemplate.execute("""
                    ALTER TABLE pedidos
                    ALTER COLUMN valor_produtos SET NOT NULL
                    """);

            jdbcTemplate.execute("""
                    UPDATE produtos
                    SET visualizacoes = 0
                    WHERE visualizacoes IS NULL
                    """);

            jdbcTemplate.execute("""
                    ALTER TABLE produtos
                    ALTER COLUMN visualizacoes SET DEFAULT 0
                    """);

            jdbcTemplate.execute("""
                    ALTER TABLE produtos
                    ALTER COLUMN visualizacoes SET NOT NULL
                    """);

            log.info("Schema check: colunas de pedidos e produtos validadas/corrigidas com sucesso.");
        } catch (Exception ex) {
            log.error("Falha ao validar/corrigir schema de pedidos/produtos", ex);
        }
    }
}
