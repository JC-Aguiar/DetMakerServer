package br.com.ppw.dma.security;


import com.nimbusds.jwt.JWTParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Slf4j
@Profile("!test")
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain configure(
        HttpSecurity http,
        UrlBasedCorsConfigurationSource corsConfig,
        AuthenticationFilter jwtAuthFilter,
        JwtDecoder jwtDecoder,
        JwtMultiConverter jwtConverter)
    throws Exception {
        http.authorizeHttpRequests(registry -> registry
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/csp-report").permitAll()
                .anyRequest().authenticated()
            )
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfig))
            .sessionManagement(sessionManager ->  sessionManager.sessionCreationPolicy(STATELESS))
            .addFilterAfter(jwtAuthFilter, BearerTokenAuthenticationFilter.class)
            .oauth2ResourceServer(config -> config.jwt(
                jwt -> jwt.decoder(jwtDecoder)
                    .jwtAuthenticationConverter(jwtConverter)
            ))
            .headers(headers -> headers
                .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED)) // Adiciona X-XSS-Protection: 1; mode=block
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'none'; " +
                    "img-src 'self' data:; " +
                    "media-src 'self'; " +
                    "connect-src 'self'; " +
                    "object-src 'none'; " +
                    "script-src 'none'; " +
                    "style-src 'none'; " +
                    "frame-ancestors 'none'; " +
                    "base-uri 'none'; " +
                    "form-action 'none'"
                ))
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny) // Impede clickjacking
            )
        ;
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
    public UrlBasedCorsConfigurationSource corsConfig(
        @Value("${ppware.detmaker.client.uri}") String detmakerClientUri)
    {
        var config = new CorsConfiguration();
        config.setAllowCredentials(true); // Permitir credenciais (Authorization)
        config.setAllowedOrigins(List.of(detmakerClientUri, "http://localhost"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setMaxAge(60000L); // Cache do preflight

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
