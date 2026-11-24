package br.edu.unisales.horariosapi.service;

import br.edu.unisales.horariosapi.dto.TurmaRequest;
import br.edu.unisales.horariosapi.dto.TurmaResponse;
import br.edu.unisales.horariosapi.exception.ConflitoException;
import br.edu.unisales.horariosapi.exception.RecursoNaoEncontradoException;
import br.edu.unisales.horariosapi.model.Turma;
import br.edu.unisales.horariosapi.repository.HorarioRepository;
import br.edu.unisales.horariosapi.repository.TurmaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final HorarioRepository horarioRepository;

    public TurmaService(TurmaRepository turmaRepository, HorarioRepository horarioRepository) {
        this.turmaRepository = turmaRepository;
        this.horarioRepository = horarioRepository;
    }

    @Transactional(readOnly = true)
    public List<TurmaResponse> listar() {
        return turmaRepository.findAllByOrderByNomeAsc().stream()
                .map(TurmaResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public TurmaResponse buscarPorId(Long id) {
        return TurmaResponse.de(buscarEntidade(id));
    }

    @Transactional
    public TurmaResponse criar(TurmaRequest request) {
        String nome = request.nome().trim();
        if (turmaRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException("Já existe uma turma cadastrada com o nome " + nome);
        }
        Turma turma = new Turma();
        aplicar(turma, request);
        return TurmaResponse.de(turmaRepository.save(turma));
    }

    @Transactional
    public TurmaResponse atualizar(Long id, TurmaRequest request) {
        Turma turma = buscarEntidade(id);
        String nome = request.nome().trim();
        if (turmaRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflitoException("Já existe uma turma cadastrada com o nome " + nome);
        }
        aplicar(turma, request);
        return TurmaResponse.de(turmaRepository.save(turma));
    }

    @Transactional
    public void excluir(Long id) {
        Turma turma = buscarEntidade(id);
        if (horarioRepository.existsByTurmaId(id)) {
            throw new ConflitoException("A turma possui horários cadastrados e não pode ser excluída");
        }
        turmaRepository.delete(turma);
    }

    @Transactional(readOnly = true)
    public Turma buscarEntidade(Long id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Turma", id));
    }

    private void aplicar(Turma turma, TurmaRequest request) {
        turma.setNome(request.nome().trim());
        turma.setCurso(request.curso().trim());
        turma.setAnoLetivo(request.anoLetivo().trim());
        turma.setTurno(request.turno());
    }
}
