package br.edu.unisales.horariosapi.service;

import br.edu.unisales.horariosapi.dto.HorarioRequest;
import br.edu.unisales.horariosapi.dto.HorarioResponse;
import br.edu.unisales.horariosapi.exception.ConflitoException;
import br.edu.unisales.horariosapi.exception.RecursoNaoEncontradoException;
import br.edu.unisales.horariosapi.exception.RegraNegocioException;
import br.edu.unisales.horariosapi.model.Horario;
import br.edu.unisales.horariosapi.repository.HorarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class HorarioService {

    public static final Comparator<Horario> ORDEM_CRONOLOGICA = Comparator
            .comparing(Horario::getDiaSemana)
            .thenComparing(Horario::getHoraInicio);

    private static final long SEM_ID = -1L;

    private final HorarioRepository horarioRepository;
    private final ProfessorService professorService;
    private final DisciplinaService disciplinaService;
    private final TurmaService turmaService;

    public HorarioService(HorarioRepository horarioRepository,
                          ProfessorService professorService,
                          DisciplinaService disciplinaService,
                          TurmaService turmaService) {
        this.horarioRepository = horarioRepository;
        this.professorService = professorService;
        this.disciplinaService = disciplinaService;
        this.turmaService = turmaService;
    }

    @Transactional(readOnly = true)
    public List<HorarioResponse> listar() {
        return horarioRepository.findAll().stream()
                .sorted(ORDEM_CRONOLOGICA)
                .map(HorarioResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public HorarioResponse buscarPorId(Long id) {
        return HorarioResponse.de(buscarEntidade(id));
    }

    @Transactional
    public HorarioResponse criar(HorarioRequest request) {
        validar(request, SEM_ID);
        Horario horario = new Horario();
        aplicar(horario, request);
        return HorarioResponse.de(horarioRepository.save(horario));
    }

    @Transactional
    public HorarioResponse atualizar(Long id, HorarioRequest request) {
        Horario horario = buscarEntidade(id);
        validar(request, id);
        aplicar(horario, request);
        return HorarioResponse.de(horarioRepository.save(horario));
    }

    @Transactional
    public void excluir(Long id) {
        horarioRepository.delete(buscarEntidade(id));
    }

    private Horario buscarEntidade(Long id) {
        return horarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Horário", id));
    }

    private void validar(HorarioRequest request, Long ignorarId) {
        if (!request.horaFim().isAfter(request.horaInicio())) {
            throw new RegraNegocioException("A hora de término deve ser posterior à hora de início");
        }
        if (horarioRepository.existeConflitoProfessor(request.professorId(), request.diaSemana(),
                request.horaInicio(), request.horaFim(), ignorarId)) {
            throw new ConflitoException("O professor já possui aula neste dia e intervalo de horário");
        }
        if (horarioRepository.existeConflitoTurma(request.turmaId(), request.diaSemana(),
                request.horaInicio(), request.horaFim(), ignorarId)) {
            throw new ConflitoException("A turma já possui aula neste dia e intervalo de horário");
        }
    }

    private void aplicar(Horario horario, HorarioRequest request) {
        horario.setProfessor(professorService.buscarEntidade(request.professorId()));
        horario.setDisciplina(disciplinaService.buscarEntidade(request.disciplinaId()));
        horario.setTurma(turmaService.buscarEntidade(request.turmaId()));
        horario.setDiaSemana(request.diaSemana());
        horario.setHoraInicio(request.horaInicio());
        horario.setHoraFim(request.horaFim());
        horario.setSala(request.sala() == null || request.sala().isBlank() ? null : request.sala().trim());
    }
}
