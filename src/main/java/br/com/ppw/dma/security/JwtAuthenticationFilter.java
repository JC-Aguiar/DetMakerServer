package br.com.ppw.dma.security;

import br.com.ppw.dma.security.audition.Historico;
import br.com.ppw.dma.security.audition.HistoricoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;

import static br.com.ppw.dma.util.FormatString.LINHA_HORINZONTAL;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private HistoricoRepository historicoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.security.google.jwt.issuer}")
    private String issuer;

    @Value("${spring.security.google.jwt.jwks-uri}")
    private String jwksUri;


    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
    throws ServletException, IOException {
        log.info("{} NOVA REQUISIÇÃO {}", LINHA_HORINZONTAL, LINHA_HORINZONTAL);
        log.info("Salvando requisição no histórico da base.");
        try {
            var authorizationHeader = request.getHeader("Authorization");
            var result = validateBearerToken(authorizationHeader);
            if(result.status != OK) {
                response.setStatus(result.status.value());
                response.getWriter().write(objectMapper.writeValueAsString(result));
                return;
            }
            var httpRequest = (HttpServletRequest) request;
            var historico = Historico.builder()
                .endpoint(httpRequest.getRequestURI())
                .metodo(httpRequest.getMethod())
                .ip(httpRequest.getRemoteAddr())
                .usuario(SecurityContextHolder.getContext().getAuthentication().getName())
                .dispositivo(httpRequest.getHeader("User-Agent"))
                .build();
            log.info(historico.toString());

            historico = historicoRepository.save(historico);
            log.info("ID gerado: [{}]", historico.getId());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        filterChain.doFilter(request, response);
    }

    record BearerTokenValidation(HttpStatus status, String message) { }

    private BearerTokenValidation validateBearerToken(String header)
    throws ParseException, IOException, JOSEException {
        if(header == null || !header.startsWith("Bearer "))
            return new BearerTokenValidation(BAD_REQUEST, "Requisição sem token Bearar declarado");

        var token = header.substring(7);
        var signedJWT = SignedJWT.parse(token);
        log.info("Token obtido: '{}'", token);

        log.info("Iniciando validação do JWT.");
        if(!issuer.equals(signedJWT.getJWTClaimsSet().getIssuer()))
            return new BearerTokenValidation(UNAUTHORIZED, "Emissor ('iss') do token está inválido.");
        if(signedJWT.getJWTClaimsSet().getExpirationTime().before(new java.util.Date()))
            return new BearerTokenValidation(UNAUTHORIZED, "Data do token ('exp') consta expirada.");

        var jwkSet = JWKSet.load(new URL(jwksUri));
        var rsaKey = jwkSet.getKeyByKeyId(signedJWT.getHeader().getKeyID()).toRSAKey();
        var verifier = new RSASSAVerifier(rsaKey);

        if(!signedJWT.verify(verifier))
            return new BearerTokenValidation(UNAUTHORIZED, "Assinatura RSA não corresponde ao certificado.");

        var userId = signedJWT.getJWTClaimsSet().getSubject();
        var email = signedJWT.getJWTClaimsSet().getStringClaim("email");
        if(email == null || email.isBlank())
            return new BearerTokenValidation(NOT_ACCEPTABLE, "E-mail não encontrado no JWT da Google.");

        var auth = new UsernamePasswordAuthenticationToken(email, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        return new BearerTokenValidation(OK, null);
    }

}
