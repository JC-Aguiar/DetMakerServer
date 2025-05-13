package br.com.ppw.dma.security;


import com.nimbusds.jwt.JWTParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain configure(
        HttpSecurity http,
        AuthenticationFilter jwtAuthFilter,
        JwtDecoder jwtDecoder,
        JwtMultiConverter jwtConverter)
    throws Exception {
        http.authorizeHttpRequests(registry -> registry.anyRequest().authenticated())
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .sessionManagement(sessionManager ->  sessionManager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterAfter(jwtAuthFilter, BearerTokenAuthenticationFilter.class)
            .oauth2ResourceServer(config -> config.jwt(
                jwt -> jwt.decoder(jwtDecoder)
                    .jwtAuthenticationConverter(jwtConverter)
            ));
        //    .exceptionHandling().accessDeniedPage("/error/denied")
        //    .and()
        //    .passwordManagement(manager -> manager.changePasswordPage("/password"));
        return http.build();
    }

    /**
     * Registrando quais issuers (emissores), obtido pelo campo 'iss' dentro do JWT, possuem quais
     * serviços para sua respectiva validação.<br/>
     * Com exceção do issuer desta própria aplicação, os demais recursos são através de requisição HTTPS.
     * @return {@link JwtDecoder} configuração do decodificador de JWTs usado pela aplicação
     */
    @Bean
    public JwtDecoder jwtDecoder(
        @Value("${ppware.oauth2.authentication.server}") String ppwOauth2Server)
    {
        var appJwksUri = ppwOauth2Server + "/oauth2/jwks";
        var googleJwksUri = "https://www.googleapis.com/oauth2/v3/certs";
        var decoders = new HashMap<String, JwtDecoder>();
        Function<String, NimbusJwtDecoder> generateDecode = url -> NimbusJwtDecoder
            .withJwkSetUri(url)
            .jwsAlgorithm(SignatureAlgorithm.RS256)
            .build();

        decoders.put(
            ppwOauth2Server,
            generateDecode.apply(appJwksUri));
        decoders.put(
            "https://accounts.google.com",
            generateDecode.apply(googleJwksUri));

        return token -> {
            log.info("Iniciando validação de JWT.");
            try {
                var jwt = JWTParser.parse(token);
                var jwtClaimsSet = jwt.getJWTClaimsSet();
                var jwtIssuer = jwtClaimsSet.getIssuer();
                log.info("Issuer do JWT: '{}'", jwtIssuer);

                var registeredDecoder = decoders.get(jwtIssuer);
                return Optional.ofNullable(registeredDecoder)
                    .map(decoder -> decoder.decode(token))
                    .orElseThrow(() -> new JwtException("Emissor JWT desconhecido: " + jwtIssuer));
            }
            catch(Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    //CORS AUTHORIZATION
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                .allowedOriginPatterns("*") //http://localhost:3000
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
            }
        };
    }

}
