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
   Long id;
//   String job;
   List<String> argumentos = new ArrayList<>();
   List<ComandoSql> queries = new ArrayList<>();
   List<String> cargas = new ArrayList<>();

   @JsonIgnore
   JobDTO agenda;

}
