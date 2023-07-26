package br.com.ppw.dma.user;

import br.com.ppw.dma.master.MasterDtoResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor
public class UserDtoResponse extends UserModel implements MasterDtoResponse {

    @JsonIgnore
    String password = null;

}
