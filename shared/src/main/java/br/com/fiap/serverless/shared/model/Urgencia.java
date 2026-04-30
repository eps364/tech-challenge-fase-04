package br.com.fiap.serverless.shared.model;

import java.math.BigDecimal;

public enum Urgencia {
    BAIXA,
    MEDIA,
    ALTA,
    CRITICA;

    public static Urgencia fromNota(BigDecimal nota) {
        if (nota.compareTo(BigDecimal.valueOf(5)) < 0) {
            return CRITICA;
        }
        if (nota.compareTo(BigDecimal.valueOf(7)) < 0) {
            return ALTA;
        }
        if (nota.compareTo(BigDecimal.valueOf(9)) < 0) {
            return MEDIA;
        }
        return BAIXA;
    }

    public boolean isCritica() {
        return this == CRITICA;
    }
}
