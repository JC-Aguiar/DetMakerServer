package br.com.jcaguiar.cinephiles.security;

import br.com.jcaguiar.cinephiles.master.MasterDtoResponse;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Value
@RequiredArgsConstructor
public class JwtTokenResponse implements MasterDtoResponse {

    Map<String, String> token = new HashMap<>() {{
        put("type", "");
        put("code", "");
    }};

}
