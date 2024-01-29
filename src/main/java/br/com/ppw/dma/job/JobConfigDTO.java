package br.com.ppw.dma.job;

import br.com.ppw.dma.configQuery.ComandoSql;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobConfigDTO {

   Integer ordem = -1;
   JobInfoDTO job;
   List<String> argumentos = new ArrayList<>();
   List<ComandoSql> queries = new ArrayList<>();
   List<String> cargas = new ArrayList<>();
   Boolean pronto = false;

   public JobConfigDTO addQuery(@NonNull ComandoSql query) {
      this.queries.add(query);
      return this;
   }

   public JobConfigDTO addQuery(@NonNull List<ComandoSql> queries) {
      this.queries.addAll(queries);
      return this;
   }

}
