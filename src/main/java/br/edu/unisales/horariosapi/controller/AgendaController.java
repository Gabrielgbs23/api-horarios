package br.edu.unisales.horariosapi.controller;

import br.edu.unisales.horariosapi.dto.AgendaProfessorResponse;
import br.edu.unisales.horariosapi.dto.AgendaTurmaResponse;
import br.edu.unisales.horariosapi.dto.ErroResponse;
import br.edu.unisales.horariosapi.service.AgendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agenda")
@Tag(name = "Agenda", description = "Visualização de horários por professor e por turma")
public class AgendaController {

    private final AgendaService agendaService;

    public AgendaController(AgendaService agendaService) {
        this.agendaService = agendaService;
    }

    @GetMapping("/professores/{professorId}")
    @Operation(summary = "Horários de um professor",
            description = "Retorna as disciplinas ministradas, as turmas associadas e os dias e horários das aulas do professor")
    @ApiResponse(responseCode = "200", description = "Agenda do professor")
    @ApiResponse(responseCode = "404", description = "Professor não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public AgendaProfessorResponse porProfessor(@PathVariable Long professorId) {
        return agendaService.agendaDoProfessor(professorId);
    }

    @GetMapping("/turmas/{turmaId}")
    @Operation(summary = "Horários de uma turma",
            description = "Retorna os professores responsáveis, as disciplinas oferecidas e os dias e horários das aulas da turma")
    @ApiResponse(responseCode = "200", description = "Agenda da turma")
    @ApiResponse(responseCode = "404", description = "Turma não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public AgendaTurmaResponse porTurma(@PathVariable Long turmaId) {
        return agendaService.agendaDaTurma(turmaId);
    }
}
