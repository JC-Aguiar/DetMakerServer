package br.com.ppw.dma.job;

import br.com.ppw.dma.master.MasterDtoRequest;
import br.com.ppw.dma.util.ComandoSql;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobExecuteDTO implements MasterDtoRequest {

   Long id;
   Integer ordem;
   List<String> argumentos = new ArrayList<>();
   List<ComandoSql> queries = new ArrayList<>();
   List<String> cargas = new ArrayList<>();

}
