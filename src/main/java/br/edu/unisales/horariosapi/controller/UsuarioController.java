package br.edu.unisales.horariosapi.controller;

import br.edu.unisales.horariosapi.dto.UsuarioResponse;
import br.edu.unisales.horariosapi.model.Perfil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Dados do usuário autenticado")
public class UsuarioController {

    private static final String PREFIXO_PAPEL = "ROLE_";

    @GetMapping("/me")
    @Operation(summary = "Retorna o usuário autenticado e seu perfil",
            description = "Usado pela interface web para validar o login e exibir as ações permitidas ao perfil")
    @ApiResponse(responseCode = "200", description = "Usuário autenticado")
    @ApiResponse(responseCode = "401", description = "Credenciais ausentes ou inválidas")
    public UsuarioResponse usuarioAtual(Authentication autenticacao) {
        Perfil perfil = autenticacao.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(papel -> papel.startsWith(PREFIXO_PAPEL))
                .map(papel -> Perfil.valueOf(papel.substring(PREFIXO_PAPEL.length())))
                .findFirst()
                .orElse(Perfil.CONSULTA);
        return new UsuarioResponse(autenticacao.getName(), perfil);
    }
}
