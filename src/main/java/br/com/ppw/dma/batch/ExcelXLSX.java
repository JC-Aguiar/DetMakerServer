package br.com.ppw.dma.batch;

import br.com.ppw.dma.agenda.AgendaDTO;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
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
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static br.com.ppw.dma.util.FormatString.LINHA_HIFENS;
import static br.com.ppw.dma.util.FormatString.LINHA_HORINZONTAL;

@Slf4j
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExcelXLSX {

    final static int TAB = 4;
    final List<PlanilhaExcel> planilhas = new ArrayList<>();
    static final Map<String, Field> MAP_CAMPOS_AGENDA = new HashMap<>();

    static {
        Arrays.stream(AgendaDTO.class.getDeclaredFields())
            .forEach(campo -> {
                val anotacao = campo.getAnnotation(PlanilhaTitulo.class);
                if(anotacao == null) return;
                val tituloColuna = anotacao.value();
                MAP_CAMPOS_AGENDA.put(tituloColuna, campo);
            });
    }

    public ExcelXLSX(@NonNull String camihnoArquivo) throws IOException {
        log.info(LINHA_HORINZONTAL);
        log.info("LEITOR EXCEL XLSX");
        camihnoArquivo = camihnoArquivo
            .replace("/", File.separator)
            .replace("\\", File.separator);
        log.info("Caminho = '{}'", camihnoArquivo);
        if(!camihnoArquivo.endsWith(".xlsx"))
            throw new RuntimeException("O arquivo informado não é um Excel XLSX.");

        log.info("Abrindo e lendo arquivo...");
        try(val workbook = new XSSFWorkbook(Files.newInputStream(Paths.get(camihnoArquivo)))) {
            log.info("Workbook = '{}'", workbook);
            log.info("Iterando planilhas disponíveis.");
            workbook.forEach(planilha -> {
                log.info("-- PLANILHA '{}' {} INICIANDO", planilha.getSheetName(), LINHA_HIFENS);
                final List<AgendaDTO> listaDto = new ArrayList<>();
                val mapColunas = new HashMap<Integer, String>();
                boolean colunasFechadas = false;
                boolean coletarColunas = false;
                int indexZero = -1;
                int quantColunas = -1;

                log.info("-- PLANILHA '{}' {} LENDO CABEÇALHO", planilha.getSheetName(), LINHA_HIFENS);
                for(Row linha : planilha) {
                    AgendaDTO registro = new AgendaDTO();

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
                        else if(colIndex >= indexZero && colIndex < quantColunas - indexZero) {
                            if(colIndex == indexZero && valor.isEmpty()) break;
                            val colunaTitulo = mapColunas.get(colIndex);
                            val colunaIdentificada = MAP_CAMPOS_AGENDA.containsKey(colunaTitulo);
                            log.trace("[Linha {} | Coluna {}] = '{}'",
                                linha.getRowNum(),
                                celula.getColumnIndex(),
                                valor.replace("\n", "   ")
                            );
                            if(colunaIdentificada) {
                                log.trace("Coluna mapeada no título: '{}'", colunaTitulo);
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
                    //Insere somente os registros (AgendaDTO) preenchidos na lista de DTOs
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
//            log.info(LINHA_HORINZONTAL);
//            log.info("FORMATANDO DADOS COLETADOS");
//            log.info("Total de registros = {}", listaDto.size());
//            final Map<String, List<String>> tabela = new HashMap<>();
//            mapColunas.values().forEach(titulo -> tabela.put(titulo, new ArrayList<>()));
//
//            for(val pojo : listaDto) {
//                val camposValores = pojo.mapearCamposValores();
//                for(String coluna : camposValores.keySet()) {
//                    val valoresDaColuna = tabela.get(coluna);
//                    valoresDaColuna.add(camposValores.get(coluna));
//                    tabela.put(coluna, valoresDaColuna);
//                }
//            }
//            log.info(LINHA_HORINZONTAL);
//            log.info("ARTEFATOS DISPONÍVEIS");
//            listaDto.stream().map(AgendaDTO::getJob).forEach(log::info);
//
//            log.info(LINHA_HORINZONTAL);
//            log.info("QUAL ARTEFATO IRÁ EXECUTAR?");

//            val input = new Scanner(System.in);
//            //Adiciona gatilho para teclado
//            try {
//                GlobalScreen.registerNativeHook();
//                GlobalScreen.addNativeKeyListener(new ExcelXLSX());
//                log.info("Digite sua opção... ");
//            }
//            catch(Exception e) {
//                log.warn(e.getMessage());
//                log.warn("Não foi possível acionar teclado para encerrar a aplicação em segurança");
//                log.warn("Se a aplicação for parada abruptamente registros no banco poderão ser perdidos");
//            }
        }
    }


    public static void tabelaFormatada(Map<String, List<String>> tabela) {
        val tabelaArray = new String[tabela.values().size()][tabela.keySet().size()];
        int index = 0;
        for(String coluna : tabela.keySet()) {
            val conteudo = tabela.get(coluna);
            conteudo.add(0, coluna);
            val conteudoFormatado = formatarColuna(conteudo);
            tabelaArray[index++] = conteudoFormatado.toArray(new String[conteudoFormatado.size()]);
        }
        for(String[] strings : tabelaArray) {
            System.out.print(strings[0].concat(" | "));
        }
    }

//    public static void printTabela(Class<?> classeAlvo, List<Objects> objs) {
//        val objsAlvo = objs.stream()
//            .filter(obj -> obj.getClass().equals(classeAlvo))
//            .collect(Collectors.toList());
//        if(objsAlvo.isEmpty())
//            throw new RuntimeException("A classe informada não consta na lista de objetos");
//
//        var camposNome = Arrays.asList(classeAlvo.getDeclaredFields());
//        var camposValores = new HashMap<Object, List<String>>();
//        val colunas = camposNome.size();
//        val linhas = objsAlvo.size();
//        val tabela = new String[colunas][linhas];
//        int index = 1;
//
//        //Preenchendo a primeira linha da tabela com o nome dos campos
//        for(int col = 0; col < camposNome.size(); col++) {
//            tabela[col][0] = camposNome.get(col).getName();
//        }
//
//        //Preenchendo as demais linhas da tabela com os valores
//        for(Object obj : objsAlvo) {
//            //Obtendo todos os valores dos campos de cada objeto
//            val valores = new ArrayList<String>();
//            Arrays.asList(obj.getClass().getDeclaredFields()).forEach(campo -> {
//                try {
//                    val valor = campo.get(obj);
//                    valores.add(String.valueOf(valor));
//                }
//                catch(IllegalAccessException e) {
//                    val mensagem = "Não foi possível coletar o valor do campo %s: %s.";
//                    System.out.printf(mensagem, campo.getName(), e.getMessage());
//                    valores.add(" ");
//                }
//            });
//            //Preenchendo na tabela os valores desse objeto
//            for(int i = 0; i < valores.size(); i++) {
//                tabela[i][index] = valores.get(0);
//            }
//            index++;
//            //camposValores.put(obj, valores);
//        }
//        //Obtendo o maior tamanho de todos os valores de cada campo
//        for(int col = 0; col < tabela.length; col++) {
//            val valoresPorColuna = new ArrayList<String>();
//            for(int lin = 0; lin < tabela[col].length; lin++) {
//
//                val listaFormatada = formatarColuna(Arrays.asList(tabela[col]));
//                tabela[col] = listaFormatada.toArray(new String[listaFormatada.size()]);
//            }
//        }
//
//        val camposString = String.join(" | ", formatarColuna(camposNome));
//        val valoresString = String.join(" | ", formatarColuna(camposValores));
//        val tabelaHeader = new StringBuilder("| ")
//            .append(String.join(" | ", camposString))
//            .append(" |")
//            .append("\n")
//            .append("")
//        System.out.println();
////        val camposNome = campoValores.keySet()
////            .stream()
////            .map(Field::getName)
////            .collect(Collectors.toList());
////        val camposValores =
//    }


    public static List<String> formatarColuna(List<String> textos) {
        //Obtendo texto com maior tamanho
        int maiorTamanho = 0;
        for(String txt : textos) {
            maiorTamanho = Math.max(txt.length(), maiorTamanho);
        }
        //Identificando diferença entre textos, ajusta e retorna as mensagens numa lista
        final int finalMaiorTamanho = maiorTamanho;
        return textos
            .stream()
            .map(txt -> {
                final int diferenca = finalMaiorTamanho - txt.length() + TAB;
                StringBuilder formatador = new StringBuilder();
                for(int i = 0; i < diferenca; i++) formatador.append(" ");
                return txt + formatador;
            }).collect(Collectors.toList());
    }

}
