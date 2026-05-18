package com.example.MpFitness.Model;

import java.text.Normalizer;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CategoriaEnum {
    GERAL("Geral"),
    CALCADOS_ESPORTIVOS("Calçados esportivos"),
    ROUPAS_ACADEMIA("Roupas Academia"),
    SUPLEMENTOS("Suplementos"),
    ACESSORIOS_ESPORTIVOS("Acessorios esportivos"),
    EQUIPAMENTOS("Equipamentos");

    private final String displayName;

    CategoriaEnum(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static CategoriaEnum fromValue(String value) {
        String normalized = normalize(value);

        if (normalized.isBlank()) {
            return GERAL;
        }

        return switch (normalized) {
            case "calcados-esportivos", "calcados", "calcado", "tenis", "tenis-esportivos" -> CALCADOS_ESPORTIVOS;
            case "roupas-academia", "roupas", "roupa", "camisetas", "legging" -> ROUPAS_ACADEMIA;
            case "suplementos", "suplemento" -> SUPLEMENTOS;
            case "acessorios-esportivos", "acessorios", "acessorio" -> ACESSORIOS_ESPORTIVOS;
            case "equipamentos", "equipamento" -> EQUIPAMENTOS;
            case "geral" -> GERAL;
            default -> {
                for (CategoriaEnum categoria : values()) {
                    if (normalize(categoria.name()).equals(normalized)
                            || normalize(categoria.displayName).equals(normalized)) {
                        yield categoria;
                    }
                }

                throw new IllegalArgumentException("Categoria inválida: " + value);
            }
        };
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }

        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return withoutAccents
                .toLowerCase(Locale.ROOT)
                .replace('_', '-')
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
    }
}
