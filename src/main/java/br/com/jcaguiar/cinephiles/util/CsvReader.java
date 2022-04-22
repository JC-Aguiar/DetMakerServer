package br.com.jcaguiar.cinephiles.util;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CsvReader {

    /**
     * Reads a csv file and returns a list of String arrays
     *
     * @param path The path to the CSV file.
     * @return A list of String arrays.
     */
    public List<String[]> read(String path) throws IOException, CsvException {
        final Path file = Paths.get(path);
        final CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        final BufferedReader reader = Files.newBufferedReader(
            file, StandardCharsets.UTF_8);
        final CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build();
        csvReader.skip(1);
        return csvReader.readAll();
//        } catch (FileNotFoundException e) {
//            System.out.println("Error: File not found");
//            e.printStackTrace();
//        } catch (IOException e) {
//            System.out.println("Error: Falha na leitura/escrita");
//            e.printStackTrace();
//        } catch (CsvException e) {
//            System.out.println("Error: Problema no manuseio do csv");
//            e.printStackTrace();
//        }
    }

}
