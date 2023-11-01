package br.com.ppw.dma.job;

import br.com.ppw.dma.master.MasterRequestDTO;
import br.com.ppw.dma.util.ComandoSql;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobExecuteDTO implements MasterRequestDTO {

   Long id;
   Integer ordem;
   String argumentos;
   List<ComandoSql> queries = new ArrayList<>();
   List<String> cargas = new ArrayList<>();
   //TODO: precisa da informação da pipeline?

}
