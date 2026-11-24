package br.edu.unisales.horariosapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiHorariosApplicationTests {

    private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveExigirAutenticacao() throws Exception {
        mockMvc.perform(get("/api/professores"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRecusarCredenciaisInvalidas() throws Exception {
        mockMvc.perform(get("/api/professores").with(httpBasic("admin", "errada")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void perfilConsultaNaoPodeCadastrar() throws Exception {
        mockMvc.perform(post("/api/professores").with(httpBasic("consulta", "consulta123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Teste\",\"email\":\"teste@unisales.br\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void documentacaoSwaggerDeveSerPublica() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API de Gestão de Horários de Aula"));
    }

    @Test
    void deveValidarDadosDoProfessor() throws Exception {
        mockMvc.perform(post("/api/professores").with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"email\":\"invalido\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.email").exists());
    }

    @Test
    void fluxoCompletoDeHorariosEAgendas() throws Exception {
        long professorId = criar("/api/professores",
                "{\"nome\":\"Maria Silva\",\"email\":\"maria.silva@unisales.br\",\"titulacao\":\"Mestre\"}");
        long disciplinaId = criar("/api/disciplinas",
                "{\"codigo\":\"tst101\",\"nome\":\"Algoritmos\",\"cargaHoraria\":80}");
        long turmaId = criar("/api/turmas",
                "{\"nome\":\"TST-1N\",\"curso\":\"Engenharia de Software\",\"anoLetivo\":\"2026/2\",\"turno\":\"NOTURNO\"}");

        String horario = """
                {"professorId":%d,"disciplinaId":%d,"turmaId":%d,
                 "diaSemana":"SEGUNDA","horaInicio":"19:00","horaFim":"20:40","sala":"Sala 1"}
                """.formatted(professorId, disciplinaId, turmaId);
        long horarioId = criar("/api/horarios", horario);

        mockMvc.perform(post("/api/horarios").with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(horario.replace("19:00", "20:00").replace("20:40", "21:00")))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/horarios").with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(horario.replace("SEGUNDA", "TERCA").replace("20:40", "18:00")))
                .andExpect(status().isUnprocessableContent());

        mockMvc.perform(get("/api/agenda/professores/" + professorId).with(httpBasic("consulta", "consulta123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professor.nome").value("Maria Silva"))
                .andExpect(jsonPath("$.disciplinas[0].codigo").value("TST101"))
                .andExpect(jsonPath("$.turmas[0].nome").value("TST-1N"))
                .andExpect(jsonPath("$.aulas[0].diaSemana").value("SEGUNDA"))
                .andExpect(jsonPath("$.aulas[0].horaInicio").value("19:00"));

        mockMvc.perform(get("/api/agenda/turmas/" + turmaId).with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professores[0].nome").value("Maria Silva"))
                .andExpect(jsonPath("$.disciplinas[0].nome").value("Algoritmos"))
                .andExpect(jsonPath("$.aulas.length()").value(1));

        mockMvc.perform(delete("/api/professores/" + professorId).with(admin()))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/horarios/" + horarioId).with(admin()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/professores/" + professorId).with(admin()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/professores/" + professorId).with(admin()))
                .andExpect(status().isNotFound());
    }

    private long criar(String url, String json) throws Exception {
        MvcResult resultado = mockMvc.perform(post(url).with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();
        Matcher matcher = ID.matcher(resultado.getResponse().getContentAsString());
        if (!matcher.find()) {
            throw new IllegalStateException("Resposta sem id");
        }
        return Long.parseLong(matcher.group(1));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor admin() {
        return httpBasic("admin", "admin123");
    }
}
