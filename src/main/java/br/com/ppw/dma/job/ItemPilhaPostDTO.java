package br.com.ppw.dma.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemPilhaPostDTO {

   Integer ordem;
   AgendaID id;
   List<String> argumentos = new ArrayList<>();
   List<ComandoSqlPOJO> queries;
   List<String> cargas;

   @JsonIgnore
   AgendaDTO agenda;

   @Setter
   @Getter
   @NoArgsConstructor
   public class ComandoSqlPOJO {
      private String tabela;
      private String query;
   }
}
