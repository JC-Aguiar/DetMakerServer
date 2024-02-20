package br.com.ppw.dma.job;

import br.com.ppw.dma.configQuery.ComandoSql;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobExecuteDTO {

   Long id;
   Integer ordem;
   String argumentos;
   List<ComandoSql> queries = new ArrayList<>();
//   List<MultipartFile> cargas = new ArrayList<>();
   List<JobCarga> cargas = new ArrayList<>();
   //TODO: precisa da informação da pipeline?


   public JobExecuteDTO addQuery(@NonNull ComandoSql query) {
      this.queries.add(query);
      return this;
   }

   public JobExecuteDTO addQuery(@NonNull List<ComandoSql> queries) {
      this.queries.addAll(queries);
      return this;
   }

}
