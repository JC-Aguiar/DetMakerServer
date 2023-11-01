package br.com.ppw.dma.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LinhaSql {

    Long indice;
    final List<CampoSql> campos = new ArrayList<>();

    public LinhaSql addCampo(@NonNull CampoSql campo) {
        this.campos.add(campo);
        return this;
    }

    @Override
    public String toString() {
        val camposString = campos.stream()
            .map(CampoSql::toString)
            .collect(Collectors.joining(", "));
        return String.format("[#%d] %s", indice, camposString);
    }
}
