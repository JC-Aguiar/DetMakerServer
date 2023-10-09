//package br.com.ppw.dma.security;
//
//import br.com.ppw.dma.master.MasterDtoResponse;
//import br.com.ppw.dma.user.UserDtoResponse;
//import lombok.Value;
//
//import javax.validation.constraints.NotBlank;
//import javax.validation.constraints.NotNull;
//import java.util.HashMap;
//import java.util.Map;
//
//@Value
//public class JwtTokenResponse implements MasterDtoResponse {
//
//    Map<String, String> token = new HashMap<>() {{
//        put("type", "");
//        put("code", "");
//    }};
//    UserDtoResponse user;
//
//    JwtTokenResponse(@NotBlank String type, @NotBlank String code, @NotNull UserDtoResponse user) {
//        this.token.put("type", type.trim());
//        this.token.put("code", code.trim());
//        this.user = user;
//    }
//}
