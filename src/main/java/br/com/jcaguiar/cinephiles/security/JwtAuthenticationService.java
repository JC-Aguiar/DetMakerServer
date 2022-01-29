package br.com.jcaguiar.cinephiles.security;

import br.com.jcaguiar.cinephiles.user.UserDtoResponse;
import br.com.jcaguiar.cinephiles.user.UserEntity;
import br.com.jcaguiar.cinephiles.user.UserService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import javax.naming.AuthenticationException;
import java.util.Date;
import java.util.Optional;

public class JwtAuthenticationService {

    @Autowired
    private UserService userService;
    private static final String SECRET_KEY = "+KaPdSgVkYp3s6v9y$B&E)H@McQeThWmZq4t7w!z%C*F-JaNdRgUjXn2r5u8x/A?D(G+KbPeShVmYp3s6v9y$B&E)H@McQfTjWnZr4t7w!z%C*F-JaNdRgUkXp2s5v8x/A?D(G+KbPeShVmYq3t6w9z$B&E)H@McQfTjWnZr4u7x!A%D*F-JaNdRgUkXp2s5v8y/B?E(H+KbPeShVmYq3t6w9z$C&F)J@NcQfTjWnZr4u7x!A%D*G-KaPdSgUkXp2s5v8y/B?E(H+MbQeThWmZq3t6w9z$C&F)J@NcRfUjXn2r5u7x!A%D*G-KaPdSgVkYp3s6v9y/B?E(H+MbQeThWmZq4t7w!z%C&F)J@NcRfUjXn2r5u8x/A?D(G-KaPdSgVkYp3s6v9y$B&E)H@MbQeThWmZq4t7w!z%C*F-JaNdRfUjXn2r5u8x/A?D(G+KbPeShVkYp3s6v9y$B&E)H@McQfTjWnZq4t7w!z%C*F-JaNdRgUkXp2s5u8x/A?D(";
    private static final Integer TOKEN_VALID_TIME = 86400000; //validation for 1 day
    private static final String ISSUER_NAME = "Cinephilos Server";

    //BUILDING JWT TOKEN (RESPONSE)
    public JwtTokenResponse createToken(Authentication auth) {
        final UserEntity user = userService.getUserByEmail(auth.getName());
        final String signature = "Elleirbag17";
        final Date tokenCreationDate = new Date();
        final Date tokenExpirationDate = new Date(tokenCreationDate.getTime() + TOKEN_VALID_TIME);
        final String jwtToken = Jwts.builder()
            .setIssuer(ISSUER_NAME)
            .setIssuedAt(tokenCreationDate)
            .setExpiration(tokenExpirationDate)
            .setSubject(user.getId().toString())
            .signWith(SignatureAlgorithm.HS384, SECRET_KEY)
            .compact();
        final UserDtoResponse userResponse = new ModelMapper().map(user, UserDtoResponse.class);
        return new JwtTokenResponse("Bearer", jwtToken, userResponse);
    }

    //DECRYPTING JWT-TOKEN (REQUEST)
    public UserEntity decodeToken(String jwtToken) {
        final String userStringId = Optional.of(
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJwt(jwtToken).getBody().getSubject()
        ).orElseThrow(() -> { throw new JwtException(""); });
        final int userId = Integer.parseInt(userStringId);
        return userService.getUserById(userId);
    }
}
