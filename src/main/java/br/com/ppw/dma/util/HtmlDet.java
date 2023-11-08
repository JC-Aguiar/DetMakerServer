package br.com.ppw.dma.util;

import br.com.ppw.dma.DetMakerApplication;
import br.com.ppw.dma.evidencia.EvidenciaInfoDTO;
import br.com.ppw.dma.pipeline.PipelineRelatorioDTO;
import br.com.ppw.dma.system.Arquivos;
import br.com.ppw.dma.user.UserInfoDTO;
import com.sun.tools.javac.Main;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;

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
import static br.com.ppw.dma.config.DatabaseConfig.BancoAmbiente;
import static br.com.ppw.dma.config.DatabaseConfig.ambienteInfo;
import static br.com.ppw.dma.util.FormatString.javascriptString;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

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

        //Gerando script JS das Testecase
        int countEvidencias = 0;
        int countLogs = 0;
        val anexosConteudoENome = new HashMap<String, String>(); //<function-name, file-name>
        val functionsAnexos = new HashMap<String, String>();     //<function-name, function-code>
        val evidencias = pipelineRelatorio.getRelatorio().getEvidencias();
        for(val evidencia : evidencias) {
            for(val log : evidencia.getLogs()) {
                countLogs += 1;
                val nomeArquivo = evidencia.getLogsNome().size() >= countLogs-1 ?
                    evidencia.getLogsNome().get(countLogs-1) : "LOG-SEM-NOME.log";
                val nomeAnexo = "conteudoAnexo" + countLogs + "()";
                val funcAnexo = gerarFunctionTestcaseAnexo(nomeAnexo, log);
                functionsAnexos.put(nomeAnexo, funcAnexo);
                anexosConteudoENome.put(nomeAnexo, nomeArquivo);
            }
            countEvidencias += 1;
            val nomeDoTeste = "Teste"; //TODO
            val executarApos = countEvidencias <= 1 ?
                "" : "Executar após job " +evidencias.get(countEvidencias-1).getJob();
            val resultado = evidencia.getSucesso() ? "Aprovado" : "Reprovado";

            //String que contêm um elemento do array dentro do script JS
            val camposTestecase = preencherTestcase(
                "TU" +countEvidencias,
                nomeDoTeste,
                evidencia.getJobDescricao(),
                executarApos,
                "Sucesso",
                resultado,
                "(a completar)",
                "(a completar)",
                evidencia.getData().format(PADRAO_DATA),
                functionsAnexos.keySet().stream().toList(),
                anexosConteudoENome.values().stream().toList());
            listaTestecases.add(camposTestecase);
        }
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
            String.join("\n\n", functionsAnexos.values()) +
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
        String titulo, String nome, String descricao, String preCondicoes,
        String expectativa, String resultadoFinal, String status, String responsavel,
        String data, List<String> listaAnexosVar, List<String> listaNomes){
        //--------------------------------------------------------------------------
        listaNomes = listaNomes.stream()
            .map(FormatString::javascriptString)
            .toList();
        return "TabelaTestcase("
            + javascriptString(titulo)          + ", "
            + javascriptString(nome)            + ", "
            + javascriptString(descricao)       + ", "
            + javascriptString(preCondicoes)    + ", "
            + javascriptString(expectativa)     + ", "
            + javascriptString(resultadoFinal)  + ", "
            + javascriptString(status)          + ", "
            + javascriptString(responsavel)     + ", "
            + javascriptString(data)            + ", "
            + listaAnexosVar                    + ", "
            + listaNomes
            + ")";
    }

}
