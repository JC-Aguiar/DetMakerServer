package br.com.ppw.dma.security;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Slf4j
@Setter
@Component
public class JwtMultiConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Value("${ppware.oauth2.authentication.server}")
    private String ppwOauth2Server;

    private final JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
    private final JwtGrantedAuthoritiesConverter roleConverter = new JwtGrantedAuthoritiesConverter();


    {
        //Interpretando e tratando escopos do JWT
        scopeConverter.setAuthorityPrefix("SCOPE_");
        scopeConverter.setAuthoritiesClaimName("scope");

        //Interpretando e tratando permissões do JWT
        scopeConverter.setAuthorityPrefix("ROLE_");
        scopeConverter.setAuthoritiesClaimName("role");
    }

    //Interpretando e tratando permissões do JWT para os diferentes emissores (issuers)
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        var issuer = jwt.getClaimAsString("iss");
        log.info("Emissor: '{}'", issuer);

        if(ppwOauth2Server.equals(issuer)) {
            var principal = jwt.getClaimAsString("sub");
            var authorities = scopeConverter.convert(jwt);
            authorities.addAll(roleConverter.convert(jwt));

            log.info("Usuário: '{}'", principal);
            log.info("Permissões: {}", authorities);
            return new JwtAuthenticationToken(jwt, authorities, principal);
        }
        if("https://accounts.google.com".equals(issuer)) {
            var principal = jwt.getClaimAsString("email");
            var authorities = new ArrayList<SimpleGrantedAuthority>(1);

            if(principal.contains("@ppware.com.br"))
                authorities.add(new SimpleGrantedAuthority("ROLE_APP:ADM"));
            else
                authorities.add(new SimpleGrantedAuthority("ROLE_APP:USER"));

            log.info("Usuário: '{}'", principal);
            log.info("Permissões: {}", authorities);
            return new JwtAuthenticationToken(jwt, authorities, principal);
        }
        throw new OAuth2AuthenticationException("Emissor de JWT inválido: " + issuer);
    }

}
