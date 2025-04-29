package br.com.ppw.dma.security;

import br.com.ppw.dma.security.audition.Historico;
import br.com.ppw.dma.security.audition.HistoricoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
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
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static br.com.ppw.dma.util.FormatDate.RELOGIO;
import static br.com.ppw.dma.util.FormatString.LINHA_HORINZONTAL;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private HistoricoRepository historicoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.security.oauth2.client.provider.google.issuer}")
    private String googleIssuer;

    @Value("${spring.security.oauth2.client.provider.google.jwk-set-uri}")
    private String googleJwksUri;

    private JWKSet googleJwkSet;


    private static final int TRIAL_MAX_HOURS = 360; //15 dias
    private static final Set<String> TRIAL_FREE_IPS = Set.of("127.0.0.1"); //IPs Peopleware


    private record ValidationResult(HttpStatus status, String message) { }


    @PostConstruct
    public void init() throws IOException, ParseException {
        googleJwkSet = JWKSet.load(new URL(googleJwksUri));
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
    throws ServletException, IOException {
        log.info("{} NOVA REQUISIÇÃO {}", LINHA_HORINZONTAL, LINHA_HORINZONTAL);
        var dataHoraAgora = OffsetDateTime.now(RELOGIO);
        var httpRequest = (HttpServletRequest) request;
        var ip = httpRequest.getRemoteAddr();
        var authorizationHeader = request.getHeader("Authorization");

        var result = Optional.ofNullable(authorizationHeader)
            .map(headerValue -> {
                if(headerValue.toLowerCase().startsWith("bearer "))
                    return validateBearerToken(authorizationHeader, dataHoraAgora);
                if(headerValue.toLowerCase().startsWith("basic "))
                    return validateBsicToken(authorizationHeader, ip, dataHoraAgora);
                return null;
            })
            .filter(Objects::nonNull)
            .orElseGet(() -> new ValidationResult(BAD_REQUEST, "Requisição sem header 'Authorization'."));
        if(result.status != OK) {
            response.setStatus(result.status.value());
            response.getWriter().write(objectMapper.writeValueAsString(result));
            return;
        }
        var email = result.message;
        log.info("Usuário identificado: '{}'", email);

        var auth = new UsernamePasswordAuthenticationToken(email, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        log.info("Usuário autenticado com sucesso.");

        log.info("Salvando requisição no histórico da base.");
        var historico = Historico.builder()
            .endpoint(httpRequest.getRequestURI())
            .metodo(httpRequest.getMethod())
            .ip(ip)
            .usuario(email)
            .dispositivo(httpRequest.getHeader("User-Agent"))
            .data(dataHoraAgora)
            .build();
        log.info(historico.toString());
        historico = historicoRepository.save(historico);
        log.info("ID gerado: [{}]", historico.getId());

        filterChain.doFilter(request, response);
    }

    private ValidationResult validateBsicToken(String basicToken, String ip, OffsetDateTime dataHoraAgora) {
        var base64Credentials = basicToken.substring(6).trim();
        var bytes = Base64.getDecoder().decode(base64Credentials);
        var credentials = new String(bytes, StandardCharsets.UTF_8);
        var values = credentials.split(":", 2);
        var email = values[0];
        var secret = values[1];

        // Depois de validado
        validateTrialUser(email, ip, dataHoraAgora);
    }

    private ValidationResult validateBearerToken(String bearerToken, OffsetDateTime dataHoraAgora) {
        log.info("Iniciando validação do JWT.");
        var token = bearerToken.substring(7).trim();
        try {
            var signedJWT = SignedJWT.parse(token);
            log.info("Token obtido: '{}'", token);

            var claimsSet = signedJWT.getJWTClaimsSet();
            if(claimsSet == null)
                return new ValidationResult(UNAUTHORIZED, "JWT não possui reivindicações ('claims').");

            var emissor = claimsSet.getIssuer();
            if(!googleIssuer.equals(emissor))
                return new ValidationResult(UNAUTHORIZED, "Emissor ('iss') do token está inválido.");

            var expirationTime = claimsSet.getExpirationTime();
            if(expirationTime == null)
                return new ValidationResult(UNAUTHORIZED, "JWT não possui data de expiração ('exp').");

            var expirationOffsetDateTime = expirationTime.toInstant().atOffset(ZoneOffset.UTC);
            if(dataHoraAgora.isAfter(expirationOffsetDateTime))
                return new ValidationResult(UNAUTHORIZED, "Data do JWT consta expirada.");

            var rsaKey = googleJwkSet.getKeyByKeyId(signedJWT.getHeader().getKeyID()).toRSAKey();
            var verifier = new RSASSAVerifier(rsaKey);

            if(!signedJWT.verify(verifier))
                return new ValidationResult(UNAUTHORIZED, "Assinatura RSA não corresponde ao certificado.");

            var userId = signedJWT.getJWTClaimsSet().getSubject();
            var email = signedJWT.getJWTClaimsSet().getStringClaim("email");
            if(email == null || email.isBlank())
                return new ValidationResult(NOT_ACCEPTABLE, "E-mail não encontrado no JWT da Google.");

            return new ValidationResult(OK, email);
        }
        catch(Exception e) {
            return new ValidationResult(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private ValidationResult validateTrialUser(String email, String ip, OffsetDateTime dataHoraAgora) {
        if(email.contains("@ppware.com.br")) {
            log.info("Acesso: livre.");
            return new ValidationResult(OK, email);
        }
        log.info("Acesso: trial.");
        var isUserInTrial = historicoRepository.findMinDataByUsuario(email, ip)
            .map(data -> dataHoraAgora.minusHours(TRIAL_MAX_HOURS).isBefore(data))
            .orElse(true);
        if(!isUserInTrial)
            return new ValidationResult(UNAUTHORIZED, "Acesso trial excedeu a data limite.");

        return new ValidationResult(OK, email);
    }

}
