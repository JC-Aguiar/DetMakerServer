package br.com.ppw.dma.security;


import com.nimbusds.jwt.JWTParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.HashMap;
import java.util.Optional;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain configure(HttpSecurity http, AuthenticationFilter jwtAuthFilter)
    throws Exception {
        http.authorizeHttpRequests(registry -> registry.anyRequest().authenticated())
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .sessionManagement(sessionManager ->  sessionManager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2Login(Customizer.withDefaults())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
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
        @Value("${spring.security.app.jwt.issuer-uri}") String appIssuer,
        @Value("${spring.security.app.jwt.jwk-set-uri}") String appJwksUri,
        @Value("${spring.security.oauth2.client.provider.google.issuer}") String googleIssuer,
        @Value("${spring.security.oauth2.client.provider.google.jwk-set-uri}") String googleJwksUri)
//        @Value("${spring.security.microsoft.jwt.issuer}") String microsoftIssuer,
//        @Value("${spring.security.microsoft.jwt.jwks-uri}") String microsoftJwksUri)
    {
        var decoders = new HashMap<String, JwtDecoder>();
        decoders.put(appIssuer, NimbusJwtDecoder.withJwkSetUri(appJwksUri).build());
        decoders.put(googleIssuer, NimbusJwtDecoder.withJwkSetUri(googleJwksUri).build());
//        decoders.put(microsoftIssuer, NimbusJwtDecoder.withJwkSetUri(microsoftJwksUri).build());
        return token -> {
            try {
                var jwt = JWTParser.parse(token);
                var jwtClaimsSet = jwt.getJWTClaimsSet();
                var jwtIssuer = jwtClaimsSet.getIssuer();
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
