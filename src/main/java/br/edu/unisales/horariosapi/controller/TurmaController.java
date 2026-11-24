package br.edu.unisales.horariosapi.controller;

import br.edu.unisales.horariosapi.dto.ErroResponse;
import br.edu.unisales.horariosapi.dto.TurmaRequest;
import br.edu.unisales.horariosapi.dto.TurmaResponse;
import br.edu.unisales.horariosapi.service.TurmaService;
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
@RequestMapping("/api/turmas")
@Tag(name = "Turmas", description = "Cadastro e gestão de turmas")
public class TurmaController {

    private final TurmaService turmaService;

    public TurmaController(TurmaService turmaService) {
        this.turmaService = turmaService;
    }

    @GetMapping
    @Operation(summary = "Lista todas as turmas")
    public List<TurmaResponse> listar() {
        return turmaService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma turma pelo id")
    @ApiResponse(responseCode = "200", description = "Turma encontrada")
    @ApiResponse(responseCode = "404", description = "Turma não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public TurmaResponse buscar(@PathVariable Long id) {
        return turmaService.buscarPorId(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra uma turma")
    @ApiResponse(responseCode = "201", description = "Turma cadastrada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Nome de turma já cadastrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<TurmaResponse> criar(@Valid @RequestBody TurmaRequest request) {
        TurmaResponse criada = turmaService.criar(request);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(local).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma turma")
    @ApiResponse(responseCode = "200", description = "Turma atualizada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "Turma não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Nome de turma já cadastrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public TurmaResponse atualizar(@PathVariable Long id, @Valid @RequestBody TurmaRequest request) {
        return turmaService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma turma")
    @ApiResponse(responseCode = "204", description = "Turma excluída")
    @ApiResponse(responseCode = "404", description = "Turma não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Turma possui horários vinculados",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        turmaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
