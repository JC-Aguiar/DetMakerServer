package br.com.ppw.dma.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemPilhaDTO {

   Integer ordem;
//   JobID id;
   Long id;
   List<String> argumentos = new ArrayList<>();
   List<ComandoSql> queries;
   List<String> cargas;

   @JsonIgnore
   JobDTO agenda;

}
