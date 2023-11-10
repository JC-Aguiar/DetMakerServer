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
import lombok.NonNull;
import lombok.val;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static br.com.ppw.dma.DetMakerApplication.RELOGIO;
import static br.com.ppw.dma.config.DatabaseConfig.ambienteInfo;
import static br.com.ppw.dma.util.FormatString.javascriptString;

public abstract class HtmlDet {

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
        "aplicação DET-MAKER<br> Versão ";
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
    public static final String CAMPO_DETALHES_MODULOS = "const detalhesModulos = ";
    public static final String CAMPO_DETALHES_PARAMETROS = "const detalhesParametros = ";
    public static final String CAMPO_DETALHES_DADOS = "const detalhesDados = ";
    public static final String CAMPO_DETALHES_CONFIG = "const detalhesConfig = ";
    public static final String CAMPO_DETALHES_AMBIENTE = "const detalhesAmbiente = ";

    //Campo contendo lista das Evolução dos Tabelas
    public static final String CAMPO_TABELAS_TESTECASE = "const AllTabelasTestecase = [${var}];";

    //Padrão de exibição de datas no DET
    public static final DateTimeFormatter PADRAO_DATA = DateTimeFormatter.ofPattern("YYYY/MM/dd HH:mm:ss");

    //TODO: javadoc
    public static File gerarNovoDet(
        @NonNull PipelineRelatorioDTO pipelineRelatorio,
        @NotEmpty List<UserInfoDTO> userInfo)
    throws IOException, URISyntaxException {
        //IDENTIFICAÇÃO 1) SOBRE O PROJETO
        val projeto = pipelineRelatorio.getRelatorio()
            .getNomeProjeto()
            .replace("-", " ")
            .replace("_", " ")
            .split(" ");
        val projetoId = projeto[0];
        val projetoNome = projeto.length > 1 ? projeto[1] : projeto[0];

        //IDENTIFICAÇÃO 2)SOBRE OS TESTES
        val testeTipo = CAMPO_TESTE_TIPO_VALOR;
        val testeSistema = ambienteInfo.sistema();

        //IDENTIFICAÇÃO 3) SOBRE OS ENVOLVIDOS
        val userNome = userInfo.size() > 0 ? userInfo.get(0).getNome() : ""; //TODO
        val userPapel = userInfo.size() > 0 ? userInfo.get(0).getPapel() : ""; //TODO
        val userEmpresa = userInfo.size() > 0 ? userInfo.get(0).getEmpresa() : ""; //TODO
        val userEmail = userInfo.size() > 0 ? userInfo.get(0).getEmail() : ""; //TODO
        val userTelefone = userInfo.size() > 0 ? userInfo.get(0).getTelefone() : ""; //TODO

        //DADOS DO DET
        val dataHoraHoje = LocalDateTime.now(RELOGIO).format(PADRAO_DATA);
        val atividade = pipelineRelatorio.getRelatorio().getNomeAtividade();
        val assinatura = CAMPO_ASSINATURA_VALOR + "1.0<br>" + dataHoraHoje;

        //DETALHAMENTO DOS TESTES
        val modulos = ambienteInfo.sistema();
        val parametrosDoSistema = pipelineRelatorio.getRelatorio()
            .getEvidencias()
            .stream()
            .sorted(Comparator.comparing(EvidenciaInfoDTO::getOrdem))
            .map(ev -> ev.getJob() +" "+ ev.getArgumentos())
            .map(txt -> txt.replace("null", ""))
            .collect(Collectors.joining("\n"));
        val dadosTeste = pipelineRelatorio.getPipelineDescricao();
        val configuracao = pipelineRelatorio.getRelatorio().getConfiguracao();
        val ambiente = ambienteInfo.nome();

        //Scripts a serem inseridos no HTML
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
        val scriptDetalhamento =
            CAMPO_DETALHES_MODULOS    + javascriptString(modulos)             + "; \n" +
            CAMPO_DETALHES_PARAMETROS + javascriptString(parametrosDoSistema) + "; \n" +
            CAMPO_DETALHES_DADOS      + javascriptString(dadosTeste)          + "; \n" +
            CAMPO_DETALHES_CONFIG     + javascriptString(configuracao)        + "; \n" +
            CAMPO_DETALHES_AMBIENTE   + javascriptString(ambiente)            + "; \n";
        val listaTestecases = new ArrayList<String>();

        //Preparando script JS das Testecase
        int countEvidencias = 0;
        int countAnexos = 0;
        int countTabelas = 0;
        val tabelasPreJobFunction = new HashMap<String, String>();  // K:function-name, V:function-code
        val tabelasPosJobFunction = new HashMap<String, String>();  // K:function-name, V:function-code
        val logsConteudoENome = new HashMap<String, String>();      // K:function-name, V:file-name
        val logsFunction = new HashMap<String, String>();           // K:function-name, V:function-code
        val cargasConteudoENome = new HashMap<String, String>();    // K:function-name, V:file-name
        val cargasFunction = new HashMap<String, String>();         // K:function-name, V:function-code
        val saidasConteudoENome = new HashMap<String, String>();    // K:function-name, V:file-name
        val saidasFunction = new HashMap<String, String>();         // K:function-name, V:function-code
        val evidenciasDto = pipelineRelatorio.getRelatorio().getEvidencias();
        for(val evidenciaDto : evidenciasDto) {
            for(val conteudo : evidenciaDto.getTabelasPreJob()) {
                countTabelas += 1;
                val nomeFunction = "conteudoTabela" + countTabelas + "()";
                val codeFunction = gerarFunctionTestcaseAnexo(nomeFunction, conteudo);
                tabelasPreJobFunction.put(nomeFunction, codeFunction);
            }
            for(val conteudo : evidenciaDto.getTabelasPosJob()) {
                countTabelas += 1;
                val nomeFunction = "conteudoTabela" + countTabelas + "()";
                val codeFunction = gerarFunctionTestcaseAnexo(nomeFunction, conteudo);
                tabelasPosJobFunction.put(nomeFunction, codeFunction);
            }
            for(val infoDto : evidenciaDto.getLogs()) {
                countAnexos += 1;
                val nomeArquivo = infoDto.nome();
                val nomeFunction = "conteudoAnexo" + countAnexos + "()";
                val codeFunction = gerarFunctionTestcaseAnexo(nomeFunction, infoDto.conteudo());
                logsConteudoENome.put(nomeFunction, nomeArquivo);
                logsFunction.put(nomeFunction, codeFunction);
            }
            for(val infoDto : evidenciaDto.getCargas()) {
                countAnexos += 1;
                val nomeArquivo = infoDto.nome();
                val nomeFunction = "conteudoAnexo" + countAnexos + "()";
                val codeFunction = gerarFunctionTestcaseAnexo(nomeFunction, infoDto.conteudo());
                cargasConteudoENome.put(nomeFunction, nomeArquivo);
                cargasFunction.put(nomeFunction, codeFunction);
            }
            for(val infoDto : evidenciaDto.getSaidas()) {
                countAnexos += 1;
                val nomeArquivo = infoDto.nome();
                val nomeFunction = "conteudoAnexo" + countAnexos + "()";
                val codeFunction = gerarFunctionTestcaseAnexo(nomeFunction, infoDto.conteudo());
                saidasConteudoENome.put(nomeFunction, nomeArquivo);
                saidasFunction.put(nomeFunction, codeFunction);
            }
            countEvidencias += 1;
            val executarApos = countEvidencias <= 1 ?
                "" : "Executar após job " +evidenciasDto.get(countEvidencias-1).getJob();
            val expectativa = "Sucesso";            //TODO: implementar melhoria PÓS-MVP
            val resultado = evidenciaDto.getSucesso() ? "Sucesso" : "Falha";
            val anexosNome = logsConteudoENome.values().stream().toList();
            val anexosTipo = evidenciaDto.getAnexos()
                .stream()
                .map(AnexoInfoDTO::tipo)
                .toList();

            //String que contêm um elemento do array dentro do script JS
            val camposTestecase = preencherTestcase(                //Parâmetros da function 'TabelaTestcase'
                "EVIDÊNCIA ID " + evidenciaDto.getId(),             //titulo
                evidenciaDto.getJob(),                              //nome
                evidenciaDto.getJobDescricao(),                     //descricao
                executarApos,                                       //preCondicoes
                expectativa,                                        //expectativa
                resultado,                                          //resultado
                "(a completar)",                                    //status
                "(a completar)",                                    //responsavel
                evidenciaDto.getData().format(PADRAO_DATA),         //data
                evidenciaDto.getQueries(),                          //queries
                evidenciaDto.getTabelasNome(),                      //tabelasNome
                tabelasPreJobFunction.keySet().stream().toList(),   //tabelasPreJob
                tabelasPreJobFunction.keySet().stream().toList(),   //tabelasPosJob
                logsFunction.keySet().stream().toList(),            //logsConteudo
                logsConteudoENome.values().stream().toList(),       //logsNome
                cargasFunction.keySet().stream().toList(),          //cargasConteudo
                cargasConteudoENome.values().stream().toList(),     //cargasNome
                saidasFunction.keySet().stream().toList(),          //saidasConteudo
                saidasConteudoENome.values().stream().toList()      //saidasNome
            );
            listaTestecases.add(camposTestecase);
        }
        //Gerando script JS das Testecase
        val scriptTestecase = CAMPO_TABELAS_TESTECASE.replace(
            "${var}", String.join(", ", listaTestecases)
        ) + "\n";

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
            String.join("\n\n", logsFunction.values()) +
            String.join("\n\n", tabelasPreJobFunction.values()) +
            String.join("\n\n", tabelasPosJobFunction.values()) +
            "\n</script>";
        val conteudoCss = "<style>\n" +
            Arquivos.lerArquivo(arquivoCss) +
            "\n</style>";
        val conteudoHtml = Arquivos.lerArquivo(arquivoHtml)
            .replace(TAG_JS, conteudoJs)
            .replace(TAG_CSS, conteudoCss);

        //Criando arquivo local
        val diretorio = DetMakerApplication.DIR_PIPELINE +
            pipelineRelatorio.pipelineNome().replace(" ", "_") +
            File.separator;
        //TODO: cria método que refina nomes para diretórios e arquivos

        val dataHoraDet = dataHoraHoje.replace("/", "")
            .replace(":", "")
            .replace(" ", "_");
        val arquivoNome = "DET_" +
            String.join("_", projeto) + "_" +
            dataHoraDet + ".html";
        val arquivoFinal = Arquivos.criarEscrever(diretorio, arquivoNome, conteudoHtml);

        return arquivoFinal;
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
            "`)}";
    }

    public static String preencherTestcase(
        String evidenciaId, String nome, String descricao, String preCondicoes,
        String expectativa, String resultado, String parametros, String revisor,
        String data, List<String> queries, List<String> tabelasNome,
        List<String> tabelasPreJob, List<String>  tabelasPosJob,
        List<String> logsConteudo, List<String> logsNome, List<String> cargasConteudo,
        List<String> cargasNome, List<String> saidasConteudo, List<String> saidasNome){
        //--------------------------------------------------------------------------
        return "TabelaTestcase("
            + javascriptString(evidenciaId)     + ", "  //evidenciaId
            + javascriptString(nome)            + ", "  //nome
            + javascriptString(descricao)       + ", "  //descricao
            + javascriptString(preCondicoes)    + ", "  //preCondicoes
            + javascriptString(expectativa)     + ", "  //expectativa
            + javascriptString(resultado)       + ", "  //resultado
            + javascriptString(parametros)      + ", "  //parametros
            + javascriptString(revisor)         + ", "  //revisor
            + javascriptString(data)            + ", "  //data
            + javascriptString(queries)         + ", "  //queries
            + javascriptString(tabelasNome)     + ", "  //tabelasNome
            + tabelasPreJob                     + ", "  //tabelasPreJob
            + tabelasPosJob                     + ", "  //tabelasPosJob
            + logsConteudo                      + ", "  //logsConteudo
            + javascriptString(logsNome)        + ", "  //logsNome
            + cargasConteudo                    + ", "  //cargasConteudo
            + javascriptString(cargasNome)      + ", "  //cargasNome
            + saidasConteudo                    + ", "  //saidasConteudo
            + javascriptString(saidasNome)              //saidasNome
            + ")";
    }

}
