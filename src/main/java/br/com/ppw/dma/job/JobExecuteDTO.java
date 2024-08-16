package br.com.ppw.dma.job;

import br.com.ppw.dma.configQuery.ComandoSql;
import br.com.ppw.dma.evidencia.Evidencia;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Range;

import java.util.*;

@Valid
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobExecuteDTO {

   @NotNull @Range(min = 0) Long id;
   @NotNull @Range(min = 0, max = 999) Integer ordem;
   String argumentos;
   List<ComandoSql> queries = new ArrayList<>();
   List<JobCarga> cargas = new ArrayList<>();

   @JsonIgnore
   Map<String, String> variaveis = new HashMap<>();


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

   public JobExecuteDTO addQuery(@NonNull ComandoSql...query) {
      return addQuery(List.of(query));
   }

   public JobExecuteDTO addQuery(@NonNull Collection<ComandoSql> queries) {
      this.queries.addAll(queries);
      return this;
   }

}
