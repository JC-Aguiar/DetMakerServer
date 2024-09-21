package br.com.ppw.dma.domain.storage;

import java.util.List;

public interface JobPointer {

    String pathToJob();
    List<String> pathLog();
    List<String> pathSaida();
    List<String> pathEntrada();
    List<String> getAllTabelas();

}
