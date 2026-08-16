package quizmaster.quiz.auth.Entidade.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum contaPrevilegio {
    
    GRATIS("G", "Gratis"),
    PREMIO("P", "Premio"),
    MEGA("M", "Mega");

    private String codigo;
    private String descricao;

    private contaPrevilegio(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    @JsonValue
    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    @JsonCreator
    public static contaPrevilegio doValor(String codigo) {
        switch (codigo) {
            case "G":
                return GRATIS;
            case "P":
                return PREMIO;
            case "M":
                return MEGA;
            default:
                return null;
        }
    }
}

