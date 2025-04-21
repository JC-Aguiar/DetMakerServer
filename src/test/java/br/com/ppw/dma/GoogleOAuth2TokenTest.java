package br.com.ppw.dma;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(classes = DetMakerApplication.class)
public class GoogleOAuth2TokenTest {

    @Autowired MockMvc mockMvc;

    public static final String ACCES_TOKKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImMzN2RhNzVjOWZiZTE4YzJjZTkxMjViOWFhMWYzMDBkY2IzMWU4ZDkiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI5Mjg5NDY4MjMyMTYtMjU4dDgxYWNmMGI4NnIybzBodmtnN21uOG9ya3ZxZnUuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI5Mjg5NDY4MjMyMTYtMjU4dDgxYWNmMGI4NnIybzBodmtnN21uOG9ya3ZxZnUuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTE3MjEzMDcwNjc4NTE1NjgyMDkiLCJoZCI6InBwd2FyZS5jb20uYnIiLCJlbWFpbCI6ImpvYW8uYWd1aWFyQHBwd2FyZS5jb20uYnIiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXRfaGFzaCI6Imp0N3FsRktqbm1fWEZnYWtHTHFFaVEiLCJuYW1lIjoiSm_Do28gQWd1aWFyIiwiZ2l2ZW5fbmFtZSI6Ikpvw6NvIiwiZmFtaWx5X25hbWUiOiJBZ3VpYXIiLCJpYXQiOjE3NDUyNzMzODIsImV4cCI6MTc0NTI3Njk4Mn0.uO0jCyT3riTg69Aea_Fkubt0oL88axFlBRkLBBzYCkZ2IgVZ4n_wjI8pD4It1BUTK0IIjwYDr07s0XK7HT2b-Xcd3otQOR9HvhVFJCkHq-OQIdRQ7aP_Epwzbfz0BpRHxzmY9eR2LloVvOT9ekczUO3nLy1fV1u1cv_WBPL5tIsQ7A5mQbczsRUawZJQCpNoyTi6fQimOE_yTfN82X0LdZkA8PIAnYaweN-n-W7vI0h6UnVnGf69yUrE6tEZdQbD8yVUS5FARkg6mTWlaLnfPp_V0dddXsx3syt02ucRnXeSgvXWZDz6k1GAI9SkoppSLFs6A__gni8zMrgKfLlejw";

    @BeforeEach
    public void validandoVariaveisAmbiente() {
        log.info("Validando variáveis de ambiente.");

        var googleClientId = System.getenv("GOOGLE_CLIENT_ID");
        var googleClientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
        assertNotNull(googleClientId);
        assertNotNull(googleClientSecret);

        System.setProperty("GOOGLE_CLIENT_ID", googleClientSecret);
        System.setProperty("GOOGLE_CLIENT_SECRET", googleClientSecret);
    }

    @Test
    public void anyRequest() throws Exception {
        log.info("Validando requisição ao servidor.");
        log.info("Acces Token: '{}'", ACCES_TOKKEN);
        mockMvc.perform(
            get("/cliente")
                .accept(APPLICATION_JSON)
                .header("Authorization", "Bearer " + ACCES_TOKKEN)
        )
        .andExpect(status().isOk());
    }

//    @Autowired
//    private TestRestTemplate restTemplate;
//
//    @Test
//    public void testGetAccessToken() {
//        String clientId = System.getenv("GOOGLE_CLIENT_ID");
//        String clientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
//        String redirectUri = "https://detmaker.toppen.com.br/login";
//        String authorizationCode = "code";
//        String tokenUri = "https://oauth2.googleapis.com/token";
//
//        var uri = UriComponentsBuilder.fromHttpUrl(tokenUri)
//            .queryParam("response_type", authorizationCode)
//            .queryParam("client_id", clientId)
//            .queryParam("client_secret", clientSecret)
//            .queryParam("redirect_uri", redirectUri)
//            .queryParam("grant_type", "authorization_code")
//            .build()
//            .toUri();
//
//        String response = restTemplate.postForObject(tokenUri, uri, String.class);
//
//        assertThat(response).contains("access_token");
//        // Você pode adicionar mais verificações conforme necessário
//    }
//
//    private Object createTokenRequest(
//        String clientId,
//        String clientSecret,
//        String redirectUri,
//        String authorizationCode) {
//        return UriComponentsBuilder.fromHttpUrl(tokenUri)
//            .queryParam("code", authorizationCode)
//            .queryParam("client_id", clientId)
//            .queryParam("client_secret", clientSecret)
//            .queryParam("redirect_uri", redirectUri)
//            .queryParam("grant_type", "authorization_code")
//            .build()
//            .toUri();
//    }
}
