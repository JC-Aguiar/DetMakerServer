package br.com.ppw.dma.util;

import br.com.ppw.dma.DetMakerApplication;
import br.com.ppw.dma.evidencia.AnexoInfoDTO;
import br.com.ppw.dma.evidencia.EvidenciaInfoDTO;
import br.com.ppw.dma.pipeline.DetDTO;
import br.com.ppw.dma.system.Arquivos;
import br.com.ppw.dma.user.UserInfoDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static br.com.ppw.dma.util.FormatDate.FORMAL_STYLE;
import static br.com.ppw.dma.util.FormatString.javascriptString;

@Slf4j
public class DetHtml {

    //Atributos
    @Getter final File documento;
    ResourceLoader resourceLoader;
    int countEvidencias = 0;
    int countTabelas = 0;
    int countAnexos = 0;
    final String dataHoraHoje = FormatDate.formalStyle();
    final Map<String, String> tabelasPreJobMap = new HashMap<>();       // K:function-name, V:function-code
    final Map<String, String> tabelasPosJobMap = new HashMap<>();       // K:function-name, V:function-code
    final Map<String, String> logsMap = new HashMap<>();                // K:function-name, V:file-name
    final Map<String, String> cargasMap = new HashMap<>();              // K:function-name, V:file-name
    final Map<String, String> saidasMap = new HashMap<>();              // K:function-name, V:file-name
    final Map<String, String> functionToAdd = new HashMap<>();          // K:function-name, V:function-code

    //Apontamentos dos arquivos externos
    public static final String TEMPLATE_HTML = "template/template.html"
        .replace("/", File.separator);
    public static final String TEMPLATE_JS = "template/template.js"
        .replace("/", File.separator);
    public static final String TEMPLATE_CSS = "template/template.css"
        .replace("/", File.separator);

    //Tags dentro do html a serem substituídas
    public static final String TAG_JS = "<script src=\"./template-content.js\"></script>";
    public static final String TAG_CSS = "<link rel=\"stylesheet\" href=\"./template.css\">";

    //Campos de Identificação
    public static final String CAMPO_ASSINATURA = "const assinatura = ";
    public static final String CAMPO_ATIVIDADE_NOME = "const atividadeNome = ";
    public static final String CAMPO_PROJETO_ID = "const projetoId = ";
    public static final String CAMPO_PROJETO_NOME = "const projetoNome = ";
    public static final String CAMPO_TESTE_TIPO = "const testeTipo = ";
    public static final String CAMPO_TESTE_SISTEMA = "const testeSistema = ";
    public static final String CAMPO_USER_NOME = "const userNome = ";
    public static final String CAMPO_USER_CARGO = "const userCargo = ";
    public static final String CAMPO_USER_EMPRESA = "const userEmpresa = ";
    public static final String CAMPO_USER_EMAIL = "const userEmail = ";
    public static final String CAMPO_USER_PHONE = "const userPhone = ";

    //Valores de Identificação
    public static final String VALOR_ASSINATURA = "Documento gerado automaticamente pela " +
        "aplicação DET-MAKER " + DetMakerApplication.getAppVersion();

    //Campos de Detalhamento dos Testes
    public static final String CAMPO_DETALHES_PIPELINE = "const detalhesModulos = ";
    public static final String CAMPO_DETALHES_PARAMETROS = "const detalhesParametros = ";
    public static final String CAMPO_DETALHES_DADOS = "const detalhesDados = ";
    public static final String CAMPO_DETALHES_CONFIG = "const detalhesConfig = ";
    public static final String CAMPO_DETALHES_AMBIENTE = "const detalhesAmbiente = ";

    //Campo contendo lista das Evolução dos Tabelas
    public static final String CAMPO_TABELAS_TESTECASE = "const AllTabelasTestecase = [${var}];";


    //TODO: javadoc
    public DetHtml(
        @NonNull ResourceLoader resourceLoader,
        @NonNull DetDTO dto,
        @NotEmpty List<UserInfoDTO> userInfo)
    throws IOException, URISyntaxException {
        log.info("Criando novo relatório DET.");

        this.resourceLoader = resourceLoader;
        val userNome = userInfo.size() > 0 ? userInfo.get(0).getNome() : ""; //TODO
        val userPapel = userInfo.size() > 0 ? userInfo.get(0).getPapel() : ""; //TODO
        val userEmpresa = userInfo.size() > 0 ? userInfo.get(0).getEmpresa() : ""; //TODO
        val userEmail = userInfo.size() > 0 ? userInfo.get(0).getEmail() : ""; //TODO
        val userTelefone = userInfo.size() > 0 ? userInfo.get(0).getTelefone() : ""; //TODO
        //TODO: log.info()

        //DADOS DO DET
        val assinatura = VALOR_ASSINATURA + "<br/>" + dataHoraHoje;
        log.debug("Assinatura: '{}'.", assinatura);

        //DETALHAMENTO DOS TESTES
        val parametrosDaPipeline = dto.getRelatorio()
            .getEvidencias()
            .stream()
            .sorted(Comparator.comparing(EvidenciaInfoDTO::getOrdem))
            .map(ev -> "ksh " +ev.getJob()+ " " +ev.getArgumentos())
            .map(txt -> txt.replace(" null", ""))
            .collect(Collectors.joining("\n"));

        log.debug("Criando scripts JS a serem inseridos no HTML.");
        val listaTestecases = new ArrayList<String>();
        val scriptIdentificacao =
            CAMPO_ASSINATURA     + javascriptString(assinatura)                         + "; \n" +
            CAMPO_PROJETO_ID     + javascriptString(dto.relatorio().getIdProjeto())     + "; \n" +
            CAMPO_PROJETO_NOME   + javascriptString(dto.relatorio().getNomeProjeto())   + "; \n" +
            CAMPO_ATIVIDADE_NOME + javascriptString(dto.relatorio().getNomeAtividade()) + "; \n" +
            CAMPO_TESTE_TIPO     + javascriptString(dto.relatorio().getTesteTipo())     + "; \n" +
            CAMPO_TESTE_SISTEMA  + javascriptString(dto.relatorio().getSistema())       + "; \n" +
            CAMPO_USER_NOME      + javascriptString(userNome)                           + "; \n" +
            CAMPO_USER_CARGO     + javascriptString(userPapel)                          + "; \n" +
            CAMPO_USER_EMPRESA   + javascriptString(userEmpresa)                        + "; \n" +
            CAMPO_USER_EMAIL     + javascriptString(userEmail)                          + "; \n" +
            CAMPO_USER_PHONE     + javascriptString(userTelefone)                       + "; \n";
        log.debug("Script-Identificacao:");
        Arrays.stream(scriptIdentificacao.split("\n")).forEach(log::debug);

        val scriptDetalhamento =
            CAMPO_DETALHES_PIPELINE   + javascriptString(dto.pipelineNome())                    + "; \n" +
            CAMPO_DETALHES_PARAMETROS + javascriptString(parametrosDaPipeline)                  + "; \n" +
            CAMPO_DETALHES_DADOS      + javascriptString(dto.getPipelineDescricao())            + "; \n" +
            CAMPO_DETALHES_CONFIG     + javascriptString(dto.getRelatorio().getConsideracoes())  + "; \n" +
            CAMPO_DETALHES_AMBIENTE   + javascriptString(dto.relatorio().getAmbiente())         + "; \n";
        log.debug("Script-Detalhamento:");
        Arrays.stream(scriptDetalhamento.split("\n")).forEach(log::debug);

        //Preenchendo o script 'listaTestecases'
        val evidenciasDto = dto.getRelatorio().getEvidencias();
        log.info("Total de Evidências: {}.", evidenciasDto.size());
        for(val evidenciaDto : evidenciasDto) {
            countEvidencias += 1;
            evidenciaDto.getTabelasPreJob().forEach(this::atualizarTabelaPreJob);
            evidenciaDto.getTabelasPosJob().forEach(this::atualizarTabelaPosJob);
            evidenciaDto.getLogs().forEach(this::atualizarLogsMap);
            evidenciaDto.getCargas().forEach(this::atualizarCargasMap);
            evidenciaDto.getSaidas().forEach(this::atualizarSaidasMap);
            val executarApos = (countEvidencias < 2) ?
                ("") : ("Executar após job " + evidenciasDto.get(countEvidencias-2).getJob());
            val expectativa = evidenciaDto.getComentario();
            val resultado = evidenciaDto.getResultado();

            //String que contêm um elemento do array dentro do script JS
            val camposTestecase = preencherTestcase(                //Parâmetros da function 'TabelaTestcase'
                "EVIDÊNCIA ID " + evidenciaDto.getId(),             //evidenciaId
                evidenciaDto.getJob(),                              //nome
                evidenciaDto.getJobDescricao(),                     //descricao
                executarApos,                                       //preCondicoes
                expectativa,                                        //expectativa
                resultado,                                          //resultado
                evidenciaDto.getArgumentos(),                       //parametros
                evidenciaDto.getRevisor(),                          //revisor
                evidenciaDto.getDataInicio().format(FORMAL_STYLE),  //data
                evidenciaDto.getQueries(),                          //queries
                evidenciaDto.getTabelasNome(),                      //tabelasNome
                tabelasPreJobMap.keySet().stream().toList(),        //tabelasPreJob
                tabelasPosJobMap.keySet().stream().toList(),        //tabelasPosJob
                logsMap.keySet().stream().toList(),                 //logsConteudo
                logsMap.values().stream().toList(),                 //logsNome
                cargasMap.keySet().stream().toList(),               //cargasConteudo
                cargasMap.values().stream().toList(),               //cargasNome
                saidasMap.keySet().stream().toList(),               //saidasConteudo
                saidasMap.values().stream().toList()                //saidasNome
            );
            listaTestecases.add(camposTestecase);

            //Resetando as coleções de anexos e tabelas, para não acumular nas próximas evidências
            logsMap.clear();
            cargasMap.clear();
            saidasMap.clear();
            tabelasPreJobMap.clear();
            tabelasPosJobMap.clear();
        }
        //Gerando script JS das Testecase
        val scriptTestecase = CAMPO_TABELAS_TESTECASE.replace(
            "${var}", String.join(",\n\t", listaTestecases)
        ) + "\n";
        log.debug("Script-Testecases:");
        listaTestecases.forEach(log::debug);

        //Carregando arquivos HTML, CSS e JS
        val arquivoHtml = carregarRecurso(TEMPLATE_HTML);
        val arquivoJs = carregarRecurso(TEMPLATE_JS);
        val arquivoCss = carregarRecurso(TEMPLATE_CSS);

        //Unificando arquivos CSS e JS no HTML
        val conteudoJs = "<script>\n" +
            arquivoJs + //Arquivos.lerArquivo(arquivoJs) +
            scriptIdentificacao +
            scriptDetalhamento +
            scriptTestecase +
            String.join("\n\n", functionToAdd.values()) +
            String.join("\n\n", tabelasPreJobMap.values()) +
            String.join("\n\n", tabelasPosJobMap.values()) +
            "\n</script>";
        val conteudoCss = "<style>\n" +
            arquivoCss + //Arquivos.lerArquivo(arquivoCss) +
            "\n</style>";
        val conteudoHtml = arquivoHtml//Arquivos.lerArquivo(arquivoHtml)
            .replace(TAG_JS, conteudoJs)
            .replace(TAG_CSS, conteudoCss);

        //Criando arquivo local
        //TODO: cria método que refina nomes para diretórios e arquivos
        val direotrioNome = dto.pipelineNome()
            .replace(" ", "_")
            .replace(".", "_");
        val diretorioFinal = DetMakerApplication.DIR_PIPELINE + direotrioNome + File.separator;
        val idPorjeto = FormatString.nomeParaArquivo(dto.relatorio().getIdProjeto());
        val nomePorjeto = FormatString.nomeParaArquivo(dto.relatorio().getNomeProjeto());
        val nomeAtividade = FormatString.nomeParaArquivo(dto.relatorio().getNomeAtividade());
        val dataHoraDet = FormatDate.fileNameStyle();
        val arquivoNome = "DET_" +idPorjeto+ "_" +nomePorjeto+ "_" +nomeAtividade+ "_" +dataHoraDet+ ".html";
        documento = Arquivos.criarEscrever(diretorioFinal, arquivoNome, conteudoHtml);
    }

    private String carregarRecurso(@NotBlank String arquivoNome) throws IOException {
        val recurso = resourceLoader.getResource("classpath:" + arquivoNome);
        val conteudo = new StringBuilder();

        try (val in = new InputStreamReader(recurso.getInputStream(), StandardCharsets.UTF_8);
             val reader = new BufferedReader(in)) {
            //-------------------------------------------------------------------------------
            String line;
            while((line = reader.readLine()) != null) {
                conteudo.append(line).append(System.lineSeparator());
            }
        }
        return conteudo.toString();
        //val recurso = Optional.ofNullable(Main.class.getClassLoader().getResource(arquivoNome))
        //    .orElseThrow(() -> new FileNotFoundException("Não foi possível localizar recurso " +arquivoNome));
        //return new File(recurso.toURI());
    }

    public static String gerarFunctionTestcaseAnexo(String nomeVariavel, String conteudoAnexo) {
        conteudoAnexo = Base64.getEncoder()
            .encodeToString(conteudoAnexo.getBytes(StandardCharsets.UTF_8));

        return "function " +nomeVariavel+ " { return (`" +
            "\n" +conteudoAnexo+ "\n" +
            "`)}\n";
    }

    private void atualizarTabelaPreJob(@NonNull String conteudo) {
        atualizarTabelaMap(tabelasPreJobMap, conteudo);
    }

    private void atualizarTabelaPosJob(@NonNull String conteudo) {
        atualizarTabelaMap(tabelasPosJobMap, conteudo);
    }

    private void atualizarTabelaMap(@NonNull Map<String, String> mapa, @NonNull String conteudo) {
        countTabelas += 1;
        val nomeFunction = "conteudoTabela" + countTabelas + "()";
        val codeFunction = gerarFunctionTestcaseAnexo(nomeFunction, conteudo);
        mapa.put(nomeFunction, codeFunction);
        functionToAdd.put(nomeFunction, codeFunction);
    }

    private void atualizarLogsMap(@NonNull AnexoInfoDTO anexo) {
        atualizarAnexoMap(logsMap, anexo);
    }

    private void atualizarCargasMap(@NonNull AnexoInfoDTO anexo) {
        atualizarAnexoMap(cargasMap, anexo);
    }

    private void atualizarSaidasMap(@NonNull AnexoInfoDTO anexo) {
        atualizarAnexoMap(saidasMap, anexo);
    }

    private void atualizarAnexoMap(@NonNull Map<String, String> mapa, @NonNull AnexoInfoDTO anexo) {
        countAnexos += 1;
        val nomeArquivo = anexo.nome();
        val nomeFunction = "conteudoAnexo" + countAnexos + "()";
        val codeFunction = gerarFunctionTestcaseAnexo(nomeFunction, anexo.conteudo());
        mapa.put(nomeFunction, nomeArquivo);
        functionToAdd.put(nomeFunction, codeFunction);
    }

    private static String preencherTestcase(
        String evidenciaId, String nome, String descricao, String preCondicoes,
        String expectativa, String resultado, String parametros, String revisor,
        String data, List<String> queries, List<String> tabelasNome,
        List<String> tabelasPreJob, List<String>  tabelasPosJob,
        List<String> logsConteudo, List<String> logsNome, List<String> cargasConteudo,
        List<String> cargasNome, List<String> saidasConteudo, List<String> saidasNome){
        //--------------------------------------------------------------------------
        return "\n\tTabelaTestcase(\n\t\t"
            + javascriptString(evidenciaId)     + ",\n\t\t"  //evidenciaId
            + javascriptString(nome)            + ",\n\t\t"  //nome
            + javascriptString(descricao)       + ",\n\t\t"  //descricao
            + javascriptString(preCondicoes)    + ",\n\t\t"  //preCondicoes
            + javascriptString(expectativa)     + ",\n\t\t"  //expectativa
            + javascriptString(resultado)       + ",\n\t\t"  //resultado
            + javascriptString(parametros)      + ",\n\t\t"  //parametros
            + javascriptString(revisor)         + ",\n\t\t"  //revisor
            + javascriptString(data)            + ",\n\t\t"  //data
            + javascriptString(queries)         + ",\n\t\t"  //queries
            + javascriptString(tabelasNome)     + ",\n\t\t"  //tabelasNome
            + tabelasPreJob                     + ",\n\t\t"  //tabelasPreJob
            + tabelasPosJob                     + ",\n\t\t"  //tabelasPosJob
            + logsConteudo                      + ",\n\t\t"  //logsConteudo
            + javascriptString(logsNome)        + ",\n\t\t"  //logsNome
            + cargasConteudo                    + ",\n\t\t"  //cargasConteudo
            + javascriptString(cargasNome)      + ",\n\t\t"  //cargasNome
            + saidasConteudo                    + ",\n\t\t"  //saidasConteudo
            + javascriptString(saidasNome)      + "\n\t\t"   //saidasNome
            + ")\n";
    }


}
