package br.edu.unisales.horariosapi.dto;

import br.edu.unisales.horariosapi.model.Professor;

public record ProfessorResponse(Long id, String nome, String email, String titulacao) {

    public static ProfessorResponse de(Professor professor) {
        return new ProfessorResponse(
                professor.getId(),
                professor.getNome(),
                professor.getEmail(),
                professor.getTitulacao());
    }
}
