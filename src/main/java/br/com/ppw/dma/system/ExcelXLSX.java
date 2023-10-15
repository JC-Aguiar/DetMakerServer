package br.com.ppw.dma.system;

import br.com.ppw.dma.job.JobSchedulePOJO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.*;

import static br.com.ppw.dma.util.FormatString.LINHA_HIFENS;
import static br.com.ppw.dma.util.FormatString.LINHA_HORINZONTAL;

@Slf4j
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExcelXLSX {

    final static int TAB = 4;
    final static Map<String, Field> MAP_CAMPOS_AGENDA = new HashMap<>();
    final String nomeArquivo;
    final List<PlanilhaExcel> planilhas = new ArrayList<>();

    static {
        Arrays.stream(JobSchedulePOJO.class.getDeclaredFields())
            .forEach(campo -> {
                val anotacao = campo.getAnnotation(PlanilhaTitulo.class);
                if(anotacao == null) return;
                val tituloColuna = anotacao.value();
                MAP_CAMPOS_AGENDA.put(tituloColuna, campo);
            });
    }

    public ExcelXLSX(@NotBlank String nomeArquivo, @NotNull File arquivo) throws IOException {
        this.nomeArquivo = nomeArquivo;
        log.info("Abrindo e lendo arquivo {}.", this.nomeArquivo);

        try(val workbook = new XSSFWorkbook(Files.newInputStream(arquivo.toPath()))) {
            log.info("Workbook = '{}'", workbook);
            log.info("Iterando planilhas disponíveis.");
            workbook.forEach(planilha -> {
                log.info("-- PLANILHA '{}' {} INICIANDO", planilha.getSheetName(), LINHA_HIFENS);
                final List<JobSchedulePOJO> listaDto = new ArrayList<>();
                val mapColunas = new HashMap<Integer, String>();
                boolean colunasFechadas = false;
                boolean coletarColunas = false;
                int indexZero = -1;
                int quantColunas = -1;

                log.info("-- PLANILHA '{}' {} LENDO CABEÇALHO", planilha.getSheetName(), LINHA_HIFENS);
                for(Row linha : planilha) {
                    JobSchedulePOJO registro = new JobSchedulePOJO();

                    for(Cell celula : linha) {
                        celula.setCellType(CellType.STRING);
                        val colIndex = celula.getColumnIndex();
                        val valor = celula.getStringCellValue();
                        //Comece a contar a quantidade de colunas ao identificar a célula "ID"
                        //Se a contagem foi iniciada e não consta finalizada, incremente a contagem
                        //Se estiver no meio da contagem de colunas e encontrar valor vazio, finalize a contagem
                        //Se a contagem estiver fechada, colete os registros cujo índice da coluna consta no mapeamento
                        if(!colunasFechadas) {
                            if(!valor.isEmpty()) {
                                log.trace("[Linha {} | Coluna {}] = '{}'",
                                    linha.getRowNum(),
                                    celula.getColumnIndex(),
                                    valor.replace("\n", "   ")
                                );
                            }
                            if(valor.equals("ID")) {
                                indexZero = colIndex;
                                coletarColunas = true;
                            }
                            if(coletarColunas) {
                                quantColunas += 1;
                                mapColunas.put(colIndex, valor);

                                if(valor.isEmpty()) {
                                    coletarColunas = false;
                                    colunasFechadas = true;
                                    log.info("Cabeçalho Identificado.");
                                    log.info("{}", String.join(", ", mapColunas.values()));
                                    log.info("Inicia na {}, finalizada na {}. Total de colunas = {}.",
                                        indexZero, quantColunas - indexZero, quantColunas);
                                    log.info("-- PLANILHA '{}' {} LENDO REGISTROS",
                                        planilha.getSheetName(), LINHA_HIFENS);
                                }
                            }
                        }
                        //Processo de preenchimento de valores,
                        //uma vez que o mapeamento das colunas já se encerrou
                        else if(colIndex >= indexZero && colIndex < quantColunas - indexZero) {
                            //Caso conteúdo do campo ID esteja vazio = pular linha
                            if(colIndex == indexZero && valor.isEmpty()) break;

                            //Confirmando se a célula da linha representa uma coluna válida pré-mapeada
                            val colunaTitulo = mapColunas.get(colIndex);
                            val colunaIdentificada = MAP_CAMPOS_AGENDA.containsKey(colunaTitulo);
                            log.trace("[Linha {} | Coluna {}] = '{}'",
                                linha.getRowNum(),
                                celula.getColumnIndex(),
                                valor.replace("\n", "   ")
                            );
                            //Se a coluna foi identificada, a célula é válida
                            if(colunaIdentificada) {
                                log.trace("Coluna mapeada no título: '{}'", colunaTitulo);
                                //Validando se o registro está riscado (inválido)
                                //val cellProps = ((XSSFRichTextString) celula
                                //    .getRichStringCellValue())
                                //    .getFontAtIndex(0);
                                //if(cellProps.getStrikeout()) {
                                //    log.info(
                                //        "[Linha {} | Coluna {}] " +
                                //        "Será ignorada, pois seu conteúdo está riscado.",
                                //        linha.getRowNum(),
                                //        celula.getColumnIndex()
                                //    );
                                //    continue;
                                //}
                                //Tentando obter e associar o valor da célula
                                val campo = MAP_CAMPOS_AGENDA.get(colunaTitulo);
                                try {
                                    Object valorFinal = null;
                                    if(colIndex == indexZero)
                                        valorFinal = Long.parseLong(valor);
                                    else
                                        valorFinal = valor;
                                    campo.setAccessible(true);
                                    campo.set(registro, valorFinal);
                                }
                                catch(Exception e) {
                                    log.error("Erro no campo '{}': {}",
                                        campo, e.getMessage());
                                }
                            }
                        }
                    }
                    //Fim looping das células
                    //Insere somente os registros (JobSchedulePOJO) preenchidos na lista de DTOs
                    //Ná próxima linha o registro será re-instanciado com campos vazios
                    if(registro.getId() == null) continue;
                    listaDto.add(registro);
                }
                //Fim looping das linhas
                log.info("Total de registros coletados = {}", listaDto.size());
                log.info("-- PLANILHA '{}' {} FINALIZADA", planilha.getSheetName(), LINHA_HIFENS);
                planilhas.add(new PlanilhaExcel(planilha.getSheetName(), listaDto));
            });
            log.info(LINHA_HORINZONTAL);
            log.info("EXCEL XLSX FINALIZADO");
        }
    }

}
