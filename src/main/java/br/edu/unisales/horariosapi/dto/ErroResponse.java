package br.edu.unisales.horariosapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErroResponse(
        OffsetDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        Map<String, String> campos
) {

    public static ErroResponse de(int status, String erro, String mensagem, String caminho) {
        return new ErroResponse(OffsetDateTime.now(), status, erro, mensagem, caminho, Map.of());
    }

    public static ErroResponse de(int status, String erro, String mensagem, String caminho, Map<String, String> campos) {
        return new ErroResponse(OffsetDateTime.now(), status, erro, mensagem, caminho, campos);
    }
}
