package br.com.ppw.dma.entidades;

import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Map;

import static lombok.AccessLevel.*;

@Data
@FieldDefaults(level = PRIVATE)
public class Task {

    Tipo tipo;
    String nome;
    String comando;
    Map<String, String> parametros;
    String descricao;


    public enum Tipo { FILE_TRANSFER, SCRIPT, REQUEST, QUERY }

}
