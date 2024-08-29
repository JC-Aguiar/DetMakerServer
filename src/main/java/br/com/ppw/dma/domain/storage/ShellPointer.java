package br.com.ppw.dma.domain.storage;

import java.util.List;

public interface ShellPointer {

    String pathShell();
    List<String> pathLog();
    List<String> pathSaida();
    List<String> pathEntrada();
    List<String> getAllTabelas();

}
