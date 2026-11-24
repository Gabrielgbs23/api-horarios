package br.edu.unisales.horariosapi.service;

import br.edu.unisales.horariosapi.dto.ProfessorRequest;
import br.edu.unisales.horariosapi.dto.ProfessorResponse;
import br.edu.unisales.horariosapi.exception.ConflitoException;
import br.edu.unisales.horariosapi.exception.RecursoNaoEncontradoException;
import br.edu.unisales.horariosapi.model.Professor;
import br.edu.unisales.horariosapi.repository.HorarioRepository;
import br.edu.unisales.horariosapi.repository.ProfessorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProfessorService {

    private final ProfessorRepository professorRepository;
    private final HorarioRepository horarioRepository;

    public ProfessorService(ProfessorRepository professorRepository, HorarioRepository horarioRepository) {
        this.professorRepository = professorRepository;
        this.horarioRepository = horarioRepository;
    }

    @Transactional(readOnly = true)
    public List<ProfessorResponse> listar() {
        return professorRepository.findAllByOrderByNomeAsc().stream()
                .map(ProfessorResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProfessorResponse buscarPorId(Long id) {
        return ProfessorResponse.de(buscarEntidade(id));
    }

    @Transactional
    public ProfessorResponse criar(ProfessorRequest request) {
        String email = request.email().trim();
        if (professorRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Já existe um professor cadastrado com o e-mail " + email);
        }
        Professor professor = new Professor();
        aplicar(professor, request);
        return ProfessorResponse.de(professorRepository.save(professor));
    }

    @Transactional
    public ProfessorResponse atualizar(Long id, ProfessorRequest request) {
        Professor professor = buscarEntidade(id);
        String email = request.email().trim();
        if (professorRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ConflitoException("Já existe um professor cadastrado com o e-mail " + email);
        }
        aplicar(professor, request);
        return ProfessorResponse.de(professorRepository.save(professor));
    }

    @Transactional
    public void excluir(Long id) {
        Professor professor = buscarEntidade(id);
        if (horarioRepository.existsByProfessorId(id)) {
            throw new ConflitoException("O professor possui horários cadastrados e não pode ser excluído");
        }
        professorRepository.delete(professor);
    }

    @Transactional(readOnly = true)
    public Professor buscarEntidade(Long id) {
        return professorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor", id));
    }

    private void aplicar(Professor professor, ProfessorRequest request) {
        professor.setNome(request.nome().trim());
        professor.setEmail(request.email().trim().toLowerCase());
        professor.setTitulacao(request.titulacao() == null || request.titulacao().isBlank()
                ? null
                : request.titulacao().trim());
    }
}
