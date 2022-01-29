package br.com.jcaguiar.cinephiles.security;

import br.com.jcaguiar.cinephiles.master.MasterDtoResponse;
import br.com.jcaguiar.cinephiles.user.UserDtoResponse;
import br.com.jcaguiar.cinephiles.user.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.modelmapper.ModelMapper;

import java.util.HashMap;
import java.util.Map;

@Value
@RequiredArgsConstructor
public class JwtTokenResponse implements MasterDtoResponse {

    @JsonIgnore
    String type;

    @JsonIgnore
    String code;
    Map<String, String> token = new HashMap<>() {{
        put("type", type.trim());
        put("code", code.trim());
    }};
    UserDtoResponse user;
}
