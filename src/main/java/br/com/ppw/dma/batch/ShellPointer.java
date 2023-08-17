package br.com.ppw.dma.batch;

import java.util.List;

public interface ShellPointer {

    String pathShell();
    List<String> pathLog();
    List<String> pathSaida();
    List<String> pathEntrada();
    List<String> getAllTabelas();

}
