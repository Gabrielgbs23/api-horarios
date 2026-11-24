package br.edu.unisales.horariosapi.config;

import br.edu.unisales.horariosapi.model.DiaSemana;
import br.edu.unisales.horariosapi.model.Disciplina;
import br.edu.unisales.horariosapi.model.Horario;
import br.edu.unisales.horariosapi.model.Perfil;
import br.edu.unisales.horariosapi.model.Professor;
import br.edu.unisales.horariosapi.model.Turma;
import br.edu.unisales.horariosapi.model.Turno;
import br.edu.unisales.horariosapi.model.Usuario;
import br.edu.unisales.horariosapi.repository.DisciplinaRepository;
import br.edu.unisales.horariosapi.repository.HorarioRepository;
import br.edu.unisales.horariosapi.repository.ProfessorRepository;
import br.edu.unisales.horariosapi.repository.TurmaRepository;
import br.edu.unisales.horariosapi.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Component
public class DataInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final ProfessorRepository professorRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final TurmaRepository turmaRepository;
    private final HorarioRepository horarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seguranca.admin.usuario}")
    private String adminUsuario;

    @Value("${app.seguranca.admin.senha}")
    private String adminSenha;

    @Value("${app.seguranca.consulta.usuario}")
    private String consultaUsuario;

    @Value("${app.seguranca.consulta.senha}")
    private String consultaSenha;

    @Value("${app.dados-exemplo:false}")
    private boolean dadosExemplo;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           ProfessorRepository professorRepository,
                           DisciplinaRepository disciplinaRepository,
                           TurmaRepository turmaRepository,
                           HorarioRepository horarioRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.professorRepository = professorRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.turmaRepository = turmaRepository;
        this.horarioRepository = horarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        criarUsuario(adminUsuario, adminSenha, Perfil.ADMIN);
        criarUsuario(consultaUsuario, consultaSenha, Perfil.CONSULTA);
        if (dadosExemplo && professorRepository.count() == 0) {
            carregarDadosExemplo();
        }
    }

    private void criarUsuario(String username, String senha, Perfil perfil) {
        if (!usuarioRepository.existsByUsername(username)) {
            usuarioRepository.save(new Usuario(username, passwordEncoder.encode(senha), perfil));
        }
    }

    private void carregarDadosExemplo() {
        Professor ana = professorRepository.save(new Professor("Ana Paula Ribeiro", "ana.ribeiro@unisales.br", "Mestre"));
        Professor carlos = professorRepository.save(new Professor("Carlos Eduardo Souza", "carlos.souza@unisales.br", "Doutor"));
        Professor juliana = professorRepository.save(new Professor("Juliana Martins", "juliana.martins@unisales.br", "Especialista"));

        Disciplina poo = disciplinaRepository.save(new Disciplina("ADS201", "Programação Orientada a Objetos", 80));
        Disciplina bd = disciplinaRepository.save(new Disciplina("ADS202", "Banco de Dados", 80));
        Disciplina web = disciplinaRepository.save(new Disciplina("ADS301", "Desenvolvimento Web com Spring Boot", 80));
        Disciplina ed = disciplinaRepository.save(new Disciplina("ADS203", "Estruturas de Dados", 60));

        Turma ads2 = turmaRepository.save(new Turma("ADS-2N", "Análise e Desenvolvimento de Sistemas", "2026/2", Turno.NOTURNO));
        Turma ads3 = turmaRepository.save(new Turma("ADS-3N", "Análise e Desenvolvimento de Sistemas", "2026/2", Turno.NOTURNO));
        Turma si2 = turmaRepository.save(new Turma("SI-2M", "Sistemas de Informação", "2026/2", Turno.MATUTINO));

        salvarHorario(ana, poo, ads2, DiaSemana.SEGUNDA, "19:00", "20:40", "Sala 204");
        salvarHorario(ana, ed, ads2, DiaSemana.QUARTA, "19:00", "20:40", "Lab 03");
        salvarHorario(ana, poo, si2, DiaSemana.TERCA, "08:00", "09:40", "Sala 110");
        salvarHorario(carlos, bd, ads2, DiaSemana.SEGUNDA, "20:50", "22:30", "Lab 01");
        salvarHorario(carlos, bd, ads3, DiaSemana.TERCA, "19:00", "20:40", "Lab 01");
        salvarHorario(juliana, web, ads3, DiaSemana.QUINTA, "19:00", "22:30", "Lab 02");
        salvarHorario(juliana, ed, si2, DiaSemana.SEXTA, "10:00", "11:40", "Sala 112");
    }

    private void salvarHorario(Professor professor, Disciplina disciplina, Turma turma,
                               DiaSemana dia, String inicio, String fim, String sala) {
        Horario horario = new Horario();
        horario.setProfessor(professor);
        horario.setDisciplina(disciplina);
        horario.setTurma(turma);
        horario.setDiaSemana(dia);
        horario.setHoraInicio(LocalTime.parse(inicio));
        horario.setHoraFim(LocalTime.parse(fim));
        horario.setSala(sala);
        horarioRepository.save(horario);
    }
}
