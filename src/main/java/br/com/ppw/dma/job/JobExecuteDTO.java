package br.com.ppw.dma.job;

import br.com.ppw.dma.configQuery.ComandoSql;
import br.com.ppw.dma.evidencia.Evidencia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Range;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobExecuteDTO {

   @NotNull @Positive Long id;
   @NotNull @Range(max = 999) Integer ordem;
   String argumentos;
   List<ComandoSql> queries = new ArrayList<>();
   List<JobCarga> cargas = new ArrayList<>();


   public JobExecuteDTO(@NonNull Evidencia evidencia) {
      id = evidencia.getJob().getId();
      ordem = evidencia.getOrdem();
      argumentos = evidencia.getArgumentos();
      queries = evidencia.getBanco()
          .stream()
          .map(ComandoSql::new)
          .toList();
      cargas = evidencia.getCargas()
          .stream()
          .map(JobCarga::new)
          .toList();
   }


   public JobExecuteDTO addQuery(@NonNull ComandoSql query) {
      this.queries.add(query);
      return this;
   }

   public JobExecuteDTO addQuery(@NonNull List<ComandoSql> queries) {
      this.queries.addAll(queries);
      return this;
   }

}
