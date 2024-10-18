package br.com.ppw.dma.domain.pipeline.execution;

import br.com.ppw.dma.domain.evidencia.Evidencia;
import br.com.ppw.dma.domain.job.JobInfoDTO;
import br.com.ppw.dma.util.FormatString;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.*;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PipelineJobInputDTO {

   @NotNull @Min(0) Long id;
   @NotNull @Min(0) Integer ordem;
   String argumentos;
//   List<ComandoSql> queries = new ArrayList<>();
   Set<PipelineQueryInputDTO> queries = new HashSet<>();
   List<PipelineJobCargaDTO> cargas = new ArrayList<>();

   @JsonIgnore
   Map<String, String> variaveis = new HashMap<>();


   public PipelineJobInputDTO(@NonNull Evidencia evidencia) {
//      id = evidencia.getJob().getId();
      ordem = evidencia.getOrdem();
//      argumentos = evidencia.getParametros();
      queries = evidencia.getQueries()
          .stream()
          .map(PipelineQueryInputDTO::new)
          .collect(Collectors.toSet());
      cargas = evidencia.getCargas()
          .stream()
          .map(PipelineJobCargaDTO::new)
          .toList();
   }

   public PipelineJobInputDTO addQuery(@NonNull PipelineQueryInputDTO...query) {
      return addQuery(Set.of(query));
   }

   public PipelineJobInputDTO addQuery(@NonNull Set<PipelineQueryInputDTO> queries) {
      this.queries.addAll(queries);
      return this;
   }

   public void aplicarConfiguracoes(@NonNull JobInfoDTO jobInfo) {
      var parametros = jobInfo.getParametros();
      var paramInputs = argumentos.split(" ");

      //Mapeando parâmetros. Chave = índice. Valor = valor declarado na variável (se houver).
      var parametrosPreenchidos = parametros.stream().collect(
         Collectors.toMap(
            parametros::indexOf,
            param -> variaveis.getOrDefault(param, "")
      ));
      //Atualiza mapa dos parâmetros para cada parâmetro declarado diretamente no JobInput
      for(var i = 0; i < paramInputs.length; i++) {
         if(i < parametrosPreenchidos.size() && !paramInputs[i].isBlank())
            parametrosPreenchidos.put(i, paramInputs[i]);
      }
      //TODO: usar?
      //Identifica parâmetros pendentes
//		var parametrosPendentes = parametrosPreenchidos.keySet()
//			.stream()
//			.filter(chave -> parametrosPreenchidos.get(chave).isBlank())
//			.map(chave -> infoDto.getDescricaoParametros().get(chave))
//			.filter(paramDesc -> !paramDesc.toLowerCase().contains("opcional"))
//			.filter(paramDesc -> !paramDesc.toLowerCase().contains("facultativo"))
//			.toList();
//
//		if(parametrosPendentes.size() > 0) {
//			throw new RuntimeException("Parâmetros obrigatórios incompletos para Job '"
//				+ infoDto.getNome() + "': "
//				+ String.join(", ", parametrosPendentes));
//		}
      //Aplicando as variáveis nos parâmetros
      argumentos = String.join(" ", parametrosPreenchidos.values());

      //Aplicando as variáveis nas queries
      queries.forEach(queryInput -> {
         var novaSql = FormatString.substituirVariaveis(queryInput.getSql(), variaveis);
         queryInput.setSql(novaSql);
      });
   }

}
