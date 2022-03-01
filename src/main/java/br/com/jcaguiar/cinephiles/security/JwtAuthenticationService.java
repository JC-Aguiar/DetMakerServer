package br.com.jcaguiar.cinephiles.security;

import br.com.jcaguiar.cinephiles.user.UserDtoResponse;
import br.com.jcaguiar.cinephiles.user.UserEntity;
import br.com.jcaguiar.cinephiles.user.UserService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtAuthenticationService {

    @Autowired
    private UserService userService;
    private static final String SECRET_KEY = "+KaPdSgVkYp3s6v9y$B&E)H@McQeThWmZq4t7w!z%C*F-JaNdRgUjXn2r5u8x/A?D(G+KbPeShVmYp3s6v9y$B&E)H@McQfTjWnZr4t7w!z%C*F-JaNdRgUkXp2s5v8x/A?D(G+KbPeShVmYq3t6w9z$B&E)H@McQfTjWnZr4u7x!A%D*F-JaNdRgUkXp2s5v8y/B?E(H+KbPeShVmYq3t6w9z$C&F)J@NcQfTjWnZr4u7x!A%D*G-KaPdSgUkXp2s5v8y/B?E(H+MbQeThWmZq3t6w9z$C&F)J@NcRfUjXn2r5u7x!A%D*G-KaPdSgVkYp3s6v9y/B?E(H+MbQeThWmZq4t7w!z%C&F)J@NcRfUjXn2r5u8x/A?D(G-KaPdSgVkYp3s6v9y$B&E)H@MbQeThWmZq4t7w!z%C*F-JaNdRfUjXn2r5u8x/A?D(G+KbPeShVkYp3s6v9y$B&E)H@McQfTjWnZq4t7w!z%C*F-JaNdRgUkXp2s5u8x/A?D(";
    private static final Integer TOKEN_VALID_TIME = 86400000; //validation for 1 day
    private static final String ISSUER_NAME = "Cinephilos Server";

    //BUILD JWT TOKEN
    public JwtTokenResponse createToken(@NotNull Authentication auth) {
        System.out.println("createToken: getPrincipal = " + auth.getPrincipal());
        return createToken((UserDetails) auth.getPrincipal());
    }

    //BUILD JWT TOKEN
    public JwtTokenResponse createToken(@NotNull UserDetails userDetails) {
        final UserEntity user = (UserEntity) userDetails;
        final String jwtToken = craftJwt(user);
        System.out.println(jwtToken);
        return craftDtoResponse(jwtToken, user);
    }

    //BUILDING JWT TOKEN
    private String craftJwt(@NotNull UserEntity user) {
        System.out.println("CraftToken");
        final Date tokenCreationDate = new Date();
        final Date tokenExpirationDate = new Date(tokenCreationDate.getTime() + TOKEN_VALID_TIME);
        return Jwts.builder()
            .setIssuer(ISSUER_NAME)
            .setSubject(user.getId().toString())
            .setIssuedAt(tokenCreationDate)
            .setExpiration(tokenExpirationDate)
            .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
            .compact();
    }

    //CREATE RESPONSE WITH JWT TOKEN
    private JwtTokenResponse craftDtoResponse(@NotBlank String jwtToken, @NotNull UserEntity user) {
        System.out.println("CraftDtoResponse");
        final UserDtoResponse userResponse = new ModelMapper().map(user, UserDtoResponse.class);
        return new JwtTokenResponse("Bearer", jwtToken, userResponse);
    }

    //DECRYPTING JWT TOKEN
    public Authentication decodeToken(@NotBlank String bearerToken) {
        System.out.println("decodeToken");
        final String userStringId = Optional.ofNullable(
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(bearerToken).getBody().getSubject())
            .orElseThrow(() -> new JwtException("Invalid or expired JWT"));
        System.out.println("BEARER TOKEN:" + bearerToken);
        final int userId = Integer.parseInt(userStringId);
        final UserEntity user = userService.findById(userId);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
}
