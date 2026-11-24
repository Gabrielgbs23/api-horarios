package br.edu.unisales.horariosapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_SEGURANCA = "basicAuth";

    @Bean
    public OpenAPI apiHorariosOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestão de Horários de Aula")
                        .version("1.0.0")
                        .description("""
                                API REST para organização e gerenciamento de horários de aula de professores.
                                Permite o cadastro de professores, disciplinas, turmas e horários, além da
                                consulta da agenda por professor e por turma.

                                Autenticação: HTTP Basic. O perfil ADMIN possui acesso completo e o perfil
                                CONSULTA possui acesso somente às operações de leitura (GET).
                                """)
                        .contact(new Contact().name("Unisales")))
                .components(new Components()
                        .addSecuritySchemes(ESQUEMA_SEGURANCA, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("Informe usuário e senha")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_SEGURANCA));
    }
}
