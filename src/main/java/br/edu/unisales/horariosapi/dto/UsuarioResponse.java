package br.edu.unisales.horariosapi.dto;

import br.edu.unisales.horariosapi.model.Perfil;

public record UsuarioResponse(String username, Perfil perfil) {
}
