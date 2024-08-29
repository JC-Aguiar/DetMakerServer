package br.com.ppw.dma.net;

import br.com.ppw.dma.util.FormatString;
import lombok.val;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static br.com.ppw.dma.util.FormatDate.BRASIL_STYLE;

//TODO: Javadoc
public record RemoteFile(
    String nome,
    long tamanho,
    LocalDateTime dataCriacao,
    LocalDateTime dataModificacao,
    String conteudo) {


    public static RemoteFile addFile(
        String nome,
        long tamanho,
        int dataCriacao,
        int dataModificacao,
        String conteudo) {
        //-----------------------------------
        val zona = ZoneId.systemDefault();
        val dataLocalCriacao = LocalDateTime.ofInstant(Instant.ofEpochSecond(dataCriacao), zona);
        val dataLocalModificacao = LocalDateTime.ofInstant(Instant.ofEpochSecond(dataModificacao), zona);
        return new RemoteFile(nome, tamanho, dataLocalCriacao, dataLocalModificacao, conteudo);
    }

    //TODO: mover esse método para uma classe Utils
    private String getConteudoResumo() {
        if(conteudo == null) return "null";
        val tamanho = FormatString.contarSubstring(conteudo, "\n");
        val peso = conteudo.getBytes().length;
        return String.format("[registros=%d, peso=%dKbs]", tamanho, peso);
    }

    @Override
    public String toString() {
        return "RemoteFile {" +
            "name='" + nome + '\'' +
            ", tamanho=" + tamanho +
            ", dataCriacao=" + dataCriacao.format(BRASIL_STYLE) +
            ", dataModificacao=" + dataModificacao.format(BRASIL_STYLE) +
            ", conteudo=" + getConteudoResumo() +
            '}';
    }

    public boolean iguais(RemoteFile outro) {
        return this.nome().equals(outro.nome())
            //&& this.tamanho == outro.tamanho
            && this.dataCriacao() == outro.dataCriacao()
            && this.dataModificacao() == outro.dataModificacao();
    }

    public String statusSimilaridade(RemoteFile outro) {
        val builder = new StringBuilder();
        boolean algoIgual = false;
        if(this.nome().equals(outro.nome())) {
            builder.append("mesmo name");
            algoIgual = true;
        }
        if(this.tamanho == outro.tamanho) {
            if(algoIgual) builder.append(", ");
            builder.append("mesmo tamanho");
            algoIgual = true;
        }
        if(this.dataCriacao() == outro.dataCriacao()) {
            if(algoIgual) builder.append(", ");
            builder.append("mesma data de criação");
            algoIgual = true;
        }
        if(this.dataModificacao() == outro.dataModificacao()) {
            if(algoIgual) builder.append(", ");
            builder.append("mesma data de modificação");
            algoIgual = true;
        }
        int ultimaVirgula = builder.lastIndexOf(",");
        if(ultimaVirgula != -1)
            builder.setCharAt(ultimaVirgula, 'e');
        return "possuem " + builder;
    }
}
