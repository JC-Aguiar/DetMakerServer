package br.com.ppw.dma.domain.pipeline.execution;

import br.com.ppw.dma.domain.job.JobParamDTO;
import br.com.ppw.dma.domain.job.JobParameterType;
import br.com.ppw.dma.util.FormatDate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import static br.com.ppw.dma.domain.job.JobParameterType.DATE;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PipelineJobParamDTO extends JobParamDTO {

   String valor;


//   @JsonIgnore
//   public String formatValue() {
//      return JobParameterType.identify(getTipo())
//          .map(type -> {
//             if(type != DATE) return type.format.formatted(valor);
//             var dateFormat = FormatDate.BASH_CYBER_STYLE getFormato()
//             type.format.formatted(valor)
//          })
//          .orElse("");
//   }

}
