package br.com.ppw.dma.system;

public enum ScheduleColunas {

    ID("ID"),
    APOS_JOB("Executar após o Job:"),
    GRUPO_CONCORRENCIA("Grupo de Concorrência"),
    FASE("Fase"),
    JOB("JOB"),
    DESCRICAO("Descrição"),
    GRUPO_UDA("Grupo (UDAx)"),
    PROGRAMA("Programa"),
    TABELAS("Tabelas Atualizadas"),
    SERVIDOR("Servidor"),
    DIRETORIO_EXECUCAO("Caminho de Execução"),
    PARAMETROS("Parâmetros"),
    PARAMETROS_DESCRICAO("Descrição dos ParÂmetros"),
    DIRETORIO_ENTRADA("Caminho Arquivo de Entrada"),
    MASCARA_ENTRADA("Máscara Arquivo Entrada"),
    DIRETORIO_SAIDA("Caminho Arquivo de Saída"),
    MASCARA_SAIDA("Máscara Arquivo Saída"),
    DIRETORIO_LOG("Caminho de logs"),
    MASCARA_LOG("Máscara do Log Principal"),
    TRATAMENTO("Tratamento"),
    ESCALATION("Escalation"),
    DATA_ATUALIZACAO("Data de Atualização"),
    ATUALIZADO_POR("Atualizado por:");

    public final String titulo;

    ScheduleColunas(String titulo) {
        this.titulo = titulo;
    }
}
