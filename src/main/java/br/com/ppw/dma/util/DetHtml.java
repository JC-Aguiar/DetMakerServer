package br.com.ppw.dma.util;

import br.com.ppw.dma.DetMakerApplication;
import br.com.ppw.dma.evidencia.AnexoInfoDTO;
import br.com.ppw.dma.evidencia.EvidenciaInfoDTO;
import br.com.ppw.dma.pipeline.PipelineRelatorioDTO;
import br.com.ppw.dma.system.Arquivos;
import br.com.ppw.dma.user.UserInfoDTO;
import com.sun.tools.javac.Main;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static br.com.ppw.dma.DetMakerApplication.RELOGIO;
import static br.com.ppw.dma.config.DatabaseConfig.ambienteInfo;
import static br.com.ppw.dma.util.FormatString.javascriptString;

@Slf4j
public class DetHtml {

    //Atributos
    @Getter final File documento;
    int countEvidencias = 0;
    int countTabelas = 0;
    int countAnexos = 0;
    final String dataHoraHoje = LocalDateTime.now(RELOGIO).format(PADRAO_DATA);
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
    public static final String CAMPO_ASSINATURA_VALOR = "Documento gerado automaticamente pela " +
        "aplicação DET-MAKER v";
    public static final String CAMPO_ATIVIDADE_NOME = "const atividadeNome = ";
    public static final String CAMPO_PROJETO_ID = "const projetoId = ";
    public static final String CAMPO_PROJETO_NOME = "const projetoNome = ";
    public static final String CAMPO_TESTE_TIPO = "const testeTipo = ";
    public static final String CAMPO_TESTE_TIPO_VALOR = "Teste Unitário";
    public static final String CAMPO_TESTE_SISTEMA = "const testeSistema = ";
    public static final String CAMPO_USER_NOME = "const userNome = ";
    public static final String CAMPO_USER_CARGO = "const userCargo = ";
    public static final String CAMPO_USER_EMPRESA = "const userEmpresa = ";
    public static final String CAMPO_USER_EMAIL = "const userEmail = ";
    public static final String CAMPO_USER_PHONE = "const userPhone = ";

    //Campos de Detalhamento dos Testes
    public static final String CAMPO_DETALHES_PIPELINE = "const detalhesModulos = ";
    public static final String CAMPO_DETALHES_PARAMETROS = "const detalhesParametros = ";
    public static final String CAMPO_DETALHES_DADOS = "const detalhesDados = ";
    public static final String CAMPO_DETALHES_CONFIG = "const detalhesConfig = ";
    public static final String CAMPO_DETALHES_AMBIENTE = "const detalhesAmbiente = ";

    //Campo contendo lista das Evolução dos Tabelas
    public static final String CAMPO_TABELAS_TESTECASE = "const AllTabelasTestecase = [${var}];";

    //Padrão de exibição de datas no DET
    public static final DateTimeFormatter PADRAO_DATA = DateTimeFormatter.ofPattern("YYYY/MM/dd HH:mm:ss");

    //TODO: javadoc
    public DetHtml(
        @NonNull PipelineRelatorioDTO pipelineRelatorio,
        @NotEmpty List<UserInfoDTO> userInfo)
    throws IOException, URISyntaxException {
        log.info("Criando novo relatório DET.");

        //IDENTIFICAÇÃO 1) SOBRE O PROJETO
        val projeto = pipelineRelatorio.getRelatorio()
            .getNomeProjeto()
            .replace("-", " ")
            .replace("_", " ")
            .split(" ");
        val projetoId = projeto[0];
        val projetoNome = projeto.length > 1 ? projeto[1] : projeto[0];
        log.info("Projeto ID: '{}'.  Projeto Nome: '{}'.", projetoId, projetoNome);

        //IDENTIFICAÇÃO 2)SOBRE OS TESTES
        val testeTipo = CAMPO_TESTE_TIPO_VALOR;
        val testeSistema = ambienteInfo.sistema();
        log.info("Tipo de Teste: '{}'.  Sistema: '{}'.", testeTipo, testeSistema);

        //IDENTIFICAÇÃO 3) SOBRE OS ENVOLVIDOS
        val userNome = userInfo.size() > 0 ? userInfo.get(0).getNome() : ""; //TODO
        val userPapel = userInfo.size() > 0 ? userInfo.get(0).getPapel() : ""; //TODO
        val userEmpresa = userInfo.size() > 0 ? userInfo.get(0).getEmpresa() : ""; //TODO
        val userEmail = userInfo.size() > 0 ? userInfo.get(0).getEmail() : ""; //TODO
        val userTelefone = userInfo.size() > 0 ? userInfo.get(0).getTelefone() : ""; //TODO
        //TODO: log.info()

        //DADOS DO DET
        val atividade = pipelineRelatorio.getRelatorio().getNomeAtividade();
        val assinatura = CAMPO_ASSINATURA_VALOR + "1.0<br>" + dataHoraHoje;
        log.info("Atividade: '{}'.  Assinatura: '{}'.", atividade, assinatura);

        //DETALHAMENTO DOS TESTES
        val parametrosDaPipeline = pipelineRelatorio.getRelatorio()
            .getEvidencias()
            .stream()
            .sorted(Comparator.comparing(EvidenciaInfoDTO::getOrdem))
            .map(ev -> ev.getJob() +" "+ ev.getArgumentos())
            .map(txt -> txt.replace("null", ""))
            .collect(Collectors.joining("\n"));
        val dadosTeste = pipelineRelatorio.getPipelineDescricao();
        val configuracao = pipelineRelatorio.getRelatorio().getConfiguracao();
        val ambiente = ambienteInfo.nome();
        log.info("Parâmetros da Pipeline: {}.", parametrosDaPipeline.replace("\n", " "));
        log.info("Dados do Teste: {}.", dadosTeste.replace("\n", " "));
        log.info("Configuração: {}.", configuracao.replace("\n", " "));
        log.info("Ambiente: '{}'.", ambiente);

        log.debug("Criando scripts JS a serem inseridos no HTML.");
        val listaTestecases = new ArrayList<String>();
        val scriptIdentificacao =
            CAMPO_ASSINATURA     + javascriptString(assinatura)   + "; \n" +
            CAMPO_ATIVIDADE_NOME + javascriptString(atividade)    + "; \n" +
            CAMPO_PROJETO_ID     + javascriptString(projetoId)    + "; \n" +
            CAMPO_PROJETO_NOME   + javascriptString(projetoNome)  + "; \n" +
            CAMPO_TESTE_TIPO     + javascriptString(testeTipo)    + "; \n" +
            CAMPO_TESTE_SISTEMA  + javascriptString(testeSistema) + "; \n" +
            CAMPO_USER_NOME      + javascriptString(userNome)     + "; \n" +
            CAMPO_USER_CARGO     + javascriptString(userPapel)    + "; \n" +
            CAMPO_USER_EMPRESA   + javascriptString(userEmpresa)  + "; \n" +
            CAMPO_USER_EMAIL     + javascriptString(userEmail)    + "; \n" +
            CAMPO_USER_PHONE     + javascriptString(userTelefone) + "; \n";
        log.debug("Script-Identificacao:");
        Arrays.stream(scriptIdentificacao.split("\n")).forEach(log::debug);

        val scriptDetalhamento =
            CAMPO_DETALHES_PIPELINE   + javascriptString(pipelineRelatorio.pipelineNome()) + "; \n" +
            CAMPO_DETALHES_PARAMETROS + javascriptString(parametrosDaPipeline) + "; \n" +
            CAMPO_DETALHES_DADOS      + javascriptString(dadosTeste)          + "; \n" +
            CAMPO_DETALHES_CONFIG     + javascriptString(configuracao)        + "; \n" +
            CAMPO_DETALHES_AMBIENTE   + javascriptString(ambiente)            + "; \n";
        log.debug("Script-Detalhamento:");
        Arrays.stream(scriptDetalhamento.split("\n")).forEach(log::debug);

        //Preenchendo o script 'listaTestecases'
        val evidenciasDto = pipelineRelatorio.getRelatorio().getEvidencias();
        log.info("Total de Evidências: {}.", evidenciasDto.size());
        for(val evidenciaDto : evidenciasDto) {
            countEvidencias += 1;
            evidenciaDto.getTabelasPreJob().forEach(this::atualizarTabelaPreJob);
            evidenciaDto.getTabelasPosJob().forEach(this::atualizarTabelaPosJob);
            evidenciaDto.getLogs().forEach(this::atualizarLogsMap);
            evidenciaDto.getCargas().forEach(this::atualizarCargasMap);
            evidenciaDto.getSaidas().forEach(this::atualizarSaidasMap);
            val executarApos = countEvidencias < 2 ?
                "" : "Executar após job " +evidenciasDto.get(countEvidencias-2).getJob();
            val expectativa = "Sucesso";            //TODO: implementar melhoria PÓS-MVP
            val resultado = evidenciaDto.getSucesso() ? "Sucesso" : "Falha";

            //String que contêm um elemento do array dentro do script JS
            val camposTestecase = preencherTestcase(                //Parâmetros da function 'TabelaTestcase'
                "EVIDÊNCIA ID " + evidenciaDto.getId(),             //evidenciaId
                evidenciaDto.getJob(),                              //nome
                evidenciaDto.getJobDescricao(),                     //descricao
                executarApos,                                       //preCondicoes
                expectativa,                                        //expectativa
                resultado,                                          //resultado
                evidenciaDto.getArgumentos(),                       //parametros
                "(completar)",                                      //revisor
                evidenciaDto.getData().format(PADRAO_DATA),         //data
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
            Arquivos.lerArquivo(arquivoJs) +
            scriptIdentificacao +
            scriptDetalhamento +
            scriptTestecase +
            String.join("\n\n", functionToAdd.values()) +
            String.join("\n\n", tabelasPreJobMap.values()) +
            String.join("\n\n", tabelasPosJobMap.values()) +
            "\n</script>";
        val conteudoCss = "<style>\n" +
            Arquivos.lerArquivo(arquivoCss) +
            "\n</style>";
        val conteudoHtml = Arquivos.lerArquivo(arquivoHtml)
            .replace(TAG_JS, conteudoJs)
            .replace(TAG_CSS, conteudoCss);

        //Criando arquivo local
        //TODO: cria método que refina nomes para diretórios e arquivos
        val direotrioNome = pipelineRelatorio.pipelineNome()
            .replace(" ", "_")
            .replace(".", "_");
        val diretorioFinal = DetMakerApplication.DIR_PIPELINE + direotrioNome + File.separator;
        val dataHoraDet = dataHoraHoje.replace("/", "")
            .replace(":", "")
            .replace(" ", "_");
        val arquivoNome = "DET_" +
            String.join("_", projeto) + "_" +
            dataHoraDet +
            ".html";
        documento = Arquivos.criarEscrever(diretorioFinal, arquivoNome, conteudoHtml);
    }

    private static File carregarRecurso(@NotBlank String arquivoNome)
    throws FileNotFoundException, URISyntaxException {
        val recurso = Optional.ofNullable(Main.class.getClassLoader().getResource(arquivoNome))
            .orElseThrow(() -> new FileNotFoundException("Não foi possível localizar recurso " +arquivoNome));
        return new File(recurso.toURI());
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
        val nomeFunction = "conteudoLog" + countAnexos + "()";
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
