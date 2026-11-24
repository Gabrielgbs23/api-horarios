package br.edu.unisales.horariosapi.service;

import br.edu.unisales.horariosapi.dto.AgendaProfessorResponse;
import br.edu.unisales.horariosapi.dto.AgendaTurmaResponse;
import br.edu.unisales.horariosapi.dto.AulaResponse;
import br.edu.unisales.horariosapi.dto.DisciplinaResponse;
import br.edu.unisales.horariosapi.dto.ProfessorResponse;
import br.edu.unisales.horariosapi.dto.TurmaResponse;
import br.edu.unisales.horariosapi.model.Disciplina;
import br.edu.unisales.horariosapi.model.Horario;
import br.edu.unisales.horariosapi.model.Professor;
import br.edu.unisales.horariosapi.model.Turma;
import br.edu.unisales.horariosapi.repository.HorarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@Service
public class AgendaService {

    private final HorarioRepository horarioRepository;
    private final ProfessorService professorService;
    private final TurmaService turmaService;

    public AgendaService(HorarioRepository horarioRepository,
                         ProfessorService professorService,
                         TurmaService turmaService) {
        this.horarioRepository = horarioRepository;
        this.professorService = professorService;
        this.turmaService = turmaService;
    }

    @Transactional(readOnly = true)
    public AgendaProfessorResponse agendaDoProfessor(Long professorId) {
        Professor professor = professorService.buscarEntidade(professorId);
        List<Horario> horarios = ordenar(horarioRepository.findByProfessorId(professorId));
        return new AgendaProfessorResponse(
                ProfessorResponse.de(professor),
                distintos(horarios, Horario::getDisciplina, Disciplina::getNome, DisciplinaResponse::de),
                distintos(horarios, Horario::getTurma, Turma::getNome, TurmaResponse::de),
                horarios.stream().map(AulaResponse::de).toList());
    }

    @Transactional(readOnly = true)
    public AgendaTurmaResponse agendaDaTurma(Long turmaId) {
        Turma turma = turmaService.buscarEntidade(turmaId);
        List<Horario> horarios = ordenar(horarioRepository.findByTurmaId(turmaId));
        return new AgendaTurmaResponse(
                TurmaResponse.de(turma),
                distintos(horarios, Horario::getProfessor, Professor::getNome, ProfessorResponse::de),
                distintos(horarios, Horario::getDisciplina, Disciplina::getNome, DisciplinaResponse::de),
                horarios.stream().map(AulaResponse::de).toList());
    }

    private List<Horario> ordenar(List<Horario> horarios) {
        return horarios.stream().sorted(HorarioService.ORDEM_CRONOLOGICA).toList();
    }

    private <E, R> List<R> distintos(List<Horario> horarios,
                                     Function<Horario, E> extrator,
                                     Function<E, String> ordenacao,
                                     Function<E, R> conversor) {
        return horarios.stream()
                .map(extrator)
                .distinct()
                .sorted(Comparator.comparing(ordenacao, String.CASE_INSENSITIVE_ORDER))
                .map(conversor)
                .toList();
    }
}
