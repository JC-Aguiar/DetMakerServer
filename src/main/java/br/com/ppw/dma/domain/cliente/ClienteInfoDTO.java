package br.com.ppw.dma.domain.cliente;

import br.com.ppw.dma.domain.ambiente.AmbienteInfoDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClienteInfoDTO {

    @NotNull Long id;
    @NotBlank String nome;
    byte[] banner;
    final List<AmbienteInfoDTO> ambientes = new ArrayList<>();

    public ClienteInfoDTO(@NonNull Cliente cliente, @NonNull List<AmbienteInfoDTO> ambientes) {
        this.id = cliente.getId();
        this.nome = cliente.getNome();
        this.banner = cliente.getBanner();
        this.ambientes.addAll(ambientes);
    }

    @Override
    public String toString() {
        return "ClienteInfoDTO{" +
            "id=" + id +
            ", name='" + nome + '\'' +
            ", banner=[" + banner.length + "Kbs]" +
            ", ambientes=" + ambientes +
            '}';
    }
}
