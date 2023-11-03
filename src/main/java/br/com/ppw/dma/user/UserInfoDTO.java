package br.com.ppw.dma.user;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInfoDTO {
    String nome;
    String papel;
    String empresa;
    String email;
    String telefone;
}
