package br.com.jcaguiar.cinephiles.user;

import br.com.jcaguiar.cinephiles.master.MasterDtoRequest;
import lombok.ToString;
import lombok.Value;

@Value
@ToString(callSuper = true)
public class UserDtoRequest extends UserModel implements MasterDtoRequest {

}
