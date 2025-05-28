package br.com.ppw.dma.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Map;

@RestController
@RequestMapping("props")
public class AppPropsController {

    @Autowired UrlBasedCorsConfigurationSource corsConfiguration;
    @Value("${ppware.oauth2.authentication.server}") String  oauth2AuthenticationServer;


    private record AppPropsResponseDTO(
        Map<String, String> props,
        UrlBasedCorsConfigurationSource corsConfiguration) {
    }
    @GetMapping
    public ResponseEntity<AppPropsResponseDTO> getProps() {
        var acceptedIssuers = Map.of(
            "OAuth2 Google Issuer", "https://accounts.google.com",
            "OAuth2 Peopleware Issuer", oauth2AuthenticationServer
        );
        return ResponseEntity.ok(new AppPropsResponseDTO(
            acceptedIssuers,
            corsConfiguration)
        );
    }

}
