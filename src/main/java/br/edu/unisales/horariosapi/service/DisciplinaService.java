package br.edu.unisales.horariosapi.service;

import br.edu.unisales.horariosapi.dto.DisciplinaRequest;
import br.edu.unisales.horariosapi.dto.DisciplinaResponse;
import br.edu.unisales.horariosapi.exception.ConflitoException;
import br.edu.unisales.horariosapi.exception.RecursoNaoEncontradoException;
import br.edu.unisales.horariosapi.model.Disciplina;
import br.edu.unisales.horariosapi.repository.DisciplinaRepository;
import br.edu.unisales.horariosapi.repository.HorarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;
    private final HorarioRepository horarioRepository;

    public DisciplinaService(DisciplinaRepository disciplinaRepository, HorarioRepository horarioRepository) {
        this.disciplinaRepository = disciplinaRepository;
        this.horarioRepository = horarioRepository;
    }

    @Transactional(readOnly = true)
    public List<DisciplinaResponse> listar() {
        return disciplinaRepository.findAllByOrderByNomeAsc().stream()
                .map(DisciplinaResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public DisciplinaResponse buscarPorId(Long id) {
        return DisciplinaResponse.de(buscarEntidade(id));
    }

    @Transactional
    public DisciplinaResponse criar(DisciplinaRequest request) {
        String codigo = normalizarCodigo(request.codigo());
        if (disciplinaRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ConflitoException("Já existe uma disciplina cadastrada com o código " + codigo);
        }
        Disciplina disciplina = new Disciplina();
        aplicar(disciplina, request);
        return DisciplinaResponse.de(disciplinaRepository.save(disciplina));
    }

    @Transactional
    public DisciplinaResponse atualizar(Long id, DisciplinaRequest request) {
        Disciplina disciplina = buscarEntidade(id);
        String codigo = normalizarCodigo(request.codigo());
        if (disciplinaRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new ConflitoException("Já existe uma disciplina cadastrada com o código " + codigo);
        }
        aplicar(disciplina, request);
        return DisciplinaResponse.de(disciplinaRepository.save(disciplina));
    }

    @Transactional
    public void excluir(Long id) {
        Disciplina disciplina = buscarEntidade(id);
        if (horarioRepository.existsByDisciplinaId(id)) {
            throw new ConflitoException("A disciplina possui horários cadastrados e não pode ser excluída");
        }
        disciplinaRepository.delete(disciplina);
    }

    @Transactional(readOnly = true)
    public Disciplina buscarEntidade(Long id) {
        return disciplinaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Disciplina", id));
    }

    private void aplicar(Disciplina disciplina, DisciplinaRequest request) {
        disciplina.setCodigo(normalizarCodigo(request.codigo()));
        disciplina.setNome(request.nome().trim());
        disciplina.setCargaHoraria(request.cargaHoraria());
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase();
    }
}
