package br.com.ppw.dma.domain.relatorio;

import br.com.ppw.dma.domain.user.UserInfoDTO;
import lombok.NonNull;

import java.util.List;


//@Data
//@FieldDefaults(level = AccessLevel.PRIVATE)
public record DetDTO(
    @NonNull String pipelineNome,
    @NonNull String pipelineDescricao,
    @NonNull RelatorioHistoricoDTO relatorio,
    @NonNull List<UserInfoDTO> users) {

    public static DetDTO from(Relatorio relatorio, List<UserInfoDTO> users) {
        if(relatorio.getPipeline() == null)
            throw new RuntimeException("Relatório sem relacionamento com uma Evidência");

        return new DetDTO(
//            relatorio.getPipeline().getProps().getNome(),
            relatorio.getPipeline().getNome(),
            relatorio.getPipeline().getDescricao(),
            new RelatorioHistoricoDTO(relatorio),
            users
        );
    }

    //TODO: mover para o DetHtml
//    public enum DetCampos {
//
//        ASSINATURA("assinatura"),
//        ATIVIDADE_NOME("atividadeNome"),
//        PROJETO_ID("projetoId"),
//        PROJETO_NOME("projetoNome"),
//        TESTE_TIPO("testeTipo"),
//        TESTE_SISTEMA("testeSistema"),
//        USER_NOME("userNome"),
//        USER_CARGO("userCargo"),
//        USER_EMPRESA("userEmpresa"),
//        USER_EMAIL("userEmail"),
//        USER_PHONE("userPhone"),
//        DETALHES_PIPELINE("detalhesModulos"),
//        DETALHES_PARAMETROS("detalhesParametros"),
//        DETALHES_DADOS("detalhesDados"),
//        DETALHES_CONFIG("detalhesConfig"),
//        DETALHES_AMBIENTE("detalhesAmbiente");
//
//        public final String variavelNome;
//
//        DetCampos(String variavelNome) {
//            this.variavelNome = variavelNome;
//        }
//        public String js(String variaelValor) {
//            return String.format("const %s = %s;", variavelNome, javascriptString(variaelValor));
//        }
//    }
//    public void algo() {
//        val userNome = users.size() > 0 ? users.get(0).getNome() : ""; //TODO
//        val userPapel = users.size() > 0 ? users.get(0).getPapel() : ""; //TODO
//        val userEmpresa = users.size() > 0 ? users.get(0).getEmpresa() : ""; //TODO
//        val userEmail = users.size() > 0 ? users.get(0).getEmail() : ""; //TODO
//        val userTelefone = users.size() > 0 ? users.get(0).getTelefone() : ""; //TODO
//        val parametrosDaPipeline = relatorio.getEvidencias()
//            .stream()
//            .sorted(Comparator.comparing(EvidenciaInfoDTO::getOrdem))
//            .map(ev -> "ksh " +ev.getJob()+ " " +ev.getArgumentos()+ "\n")
//            .map(txt -> txt.replace(" null", ""))
//            .collect(Collectors.joining("\n"));
//
//        List<String> identificacao = List.of(
//            javascript( ASSINATURA,          VALOR_ASSINATURA),
//            javascript( ATIVIDADE_NOME,      relatorio().getIdProjeto()),
//            javascript( PROJETO_ID,          relatorio().getNomeProjeto()),
//            javascript( PROJETO_NOME,        relatorio().getNomeAtividade()),
//            javascript( TESTE_TIPO,          relatorio().getTesteTipo()),
//            javascript( TESTE_SISTEMA,       relatorio().getCliente()),
//            javascript( USER_NOME,           userNome),
//            javascript( USER_CARGO,          userPapel),
//            javascript( USER_EMPRESA,        userEmpresa),
//            javascript( USER_EMAIL,          userEmail),
//            javascript( USER_PHONE,          userTelefone),
//            javascript( DETALHES_PIPELINE,   pipelineNome),
//            javascript( DETALHES_PARAMETROS, parametrosDaPipeline),
//            javascript( DETALHES_DADOS,      pipelineDescricao()),
//            javascript( DETALHES_CONFIG,     relatorio().getConsideracoes()),
//            javascript( DETALHES_AMBIENTE,   relatorio().getAmbiente())
//        );
//    }
//    private String javascript(DetHtml.DetCampos campo, String valor) {
//        return String.format("const %s = %s;", campo.variavelNome, javascriptString(valor));
//    }

}
