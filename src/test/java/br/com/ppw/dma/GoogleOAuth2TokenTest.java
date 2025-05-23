package br.com.ppw.dma;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Slf4j
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(classes = DetMakerApplication.class)
public class GoogleOAuth2TokenTest {

    @Autowired MockMvc mockMvc;


    @BeforeEach
    public void validandoVariaveisAmbiente() {
        log.info("Validando variáveis de ambiente.");
        Set.of(
            "GOOGLE_CLIENT_ID",
            "GOOGLE_CLIENT_SECRET"
        ).forEach(env -> {
            var valor = System.getenv(env);
            assertNotNull(valor, () -> "Variável de ambiente '%s' indisponível".formatted(env));
            System.setProperty(env, valor);
        });
    }

    @Test
    public void anyRequest() throws Exception {
        log.info("Validando requisição ao servidor.");
        mockMvc.perform(get("/cliente")
            .accept(APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

}
