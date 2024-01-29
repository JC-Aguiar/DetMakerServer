package br.com.ppw.dma.configQuery;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfigQueryInfoDTO extends ConfigQueryBasicDTO {

    Long id;
    Long pipeline;
    Long job;

    public ConfigQueryInfoDTO(@NonNull ConfigQuery configQuery) {
        super(configQuery);
        this.id = configQuery.getId();
        this.job = configQuery.getJob().getId();
    }

}
