package br.edu.unisales.horariosapi.controller;

import br.edu.unisales.horariosapi.dto.ErroResponse;
import br.edu.unisales.horariosapi.dto.HorarioRequest;
import br.edu.unisales.horariosapi.dto.HorarioResponse;
import br.edu.unisales.horariosapi.service.HorarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/horarios")
@Tag(name = "Horários", description = "Cadastro e gestão de horários de aula")
public class HorarioController {

    private final HorarioService horarioService;

    public HorarioController(HorarioService horarioService) {
        this.horarioService = horarioService;
    }

    @GetMapping
    @Operation(summary = "Lista todos os horários em ordem cronológica")
    public List<HorarioResponse> listar() {
        return horarioService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um horário pelo id")
    @ApiResponse(responseCode = "200", description = "Horário encontrado")
    @ApiResponse(responseCode = "404", description = "Horário não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public HorarioResponse buscar(@PathVariable Long id) {
        return horarioService.buscarPorId(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra um horário de aula",
            description = "Valida se o término é posterior ao início e se professor e turma estão livres no intervalo")
    @ApiResponse(responseCode = "201", description = "Horário cadastrado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "Professor, disciplina ou turma não encontrados",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Conflito de horário do professor ou da turma",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "422", description = "Intervalo de horário inválido",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<HorarioResponse> criar(@Valid @RequestBody HorarioRequest request) {
        HorarioResponse criado = horarioService.criar(request);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(local).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um horário de aula")
    @ApiResponse(responseCode = "200", description = "Horário atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "Horário, professor, disciplina ou turma não encontrados",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Conflito de horário do professor ou da turma",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "422", description = "Intervalo de horário inválido",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public HorarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody HorarioRequest request) {
        return horarioService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um horário de aula")
    @ApiResponse(responseCode = "204", description = "Horário excluído")
    @ApiResponse(responseCode = "404", description = "Horário não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        horarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
