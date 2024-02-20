package br.com.ppw.dma.configQuery;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfigQueryInfoDTO {

    @NotNull Long id;
    @NotNull Long jobId;
    @NotNull ComandoSql comandosSql;

    public ConfigQueryInfoDTO(@NonNull ConfigQuery configQuery) {
        this.id = configQuery.getId();
        this.jobId = configQuery.getJob().getId();
        this.comandosSql = new ComandoSql(configQuery);
    }

}
