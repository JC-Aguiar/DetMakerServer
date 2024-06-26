package br.com.ppw.dma.execFile;

import br.com.ppw.dma.evidencia.Evidencia;
import br.com.ppw.dma.net.RemoteFile;
import br.com.ppw.dma.net.SftpFileManager;
import br.com.ppw.dma.system.Arquivos;
import br.com.ppw.dma.util.FormatString;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.proxy.HibernateProxy;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static br.com.ppw.dma.execFile.TipoExecFile.*;
import static br.com.ppw.dma.execFile.TipoExecFile.CARGA;
import static br.com.ppw.dma.execFile.TipoExecFile.LOG;
import static jakarta.persistence.FetchType.LAZY;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "PPW_EXEC_FILE")
@Table(name = "PPW_EXEC_FILE")
@SequenceGenerator(name = "SEQ_EXEC_FILE_ID", sequenceName = "RCVRY.SEQ_EXEC_FILE_ID", allocationSize = 1)
public class ExecFile {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_EXEC_FILE_ID")
    // Identificador numérico do arquivo pós-execução da evidência
    Long id;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "EVIDENCIA_ID")
    // ID da evidência relacionada com esse arquivo pós-execução
    Evidencia evidencia;

    @Column(name = "JOB_NOME", length = 100, nullable = false)
    // Nome do job que gerou esse arquivo
    String jobNome;

    @Column(name = "TIPO", length = 10)
    // Informação para descrever se o arquivo é do tipo 'carga', 'saída' ou 'log'
    TipoExecFile tipo;

    @Column(name = "ARQUIVO_NOME", length = 200, nullable = true)
    // Nome desse arquivo pós-execução
    String arquivoNome;

    @Column(name = "ARQUIVO", columnDefinition = "CLOB", nullable = true)
    // Conteúdo do arquivo pós-execução
    String arquivo;

    @Column(name = "INCONFORMIDADE", columnDefinition = "VARCHAR2(200)")
    String inconformidade;


    public static ExecFile montarEvidenciaTerminal(@NonNull Evidencia evidencia, String conteudo) {
        return ExecFile.builder()
            .evidencia(evidencia)
            .jobNome(evidencia.getJob().getNome())
            .tipo(LOG)
            .arquivoNome("Log exibido no terminal")
            .arquivo(conteudo)
            .build();
    }

    public static ExecFile montarEvidenciaCarga(
        @NonNull Evidencia evidencia,
        @NonNull SftpFileManager<File> carga) {
        //-----------------------------------------
        String nome = "";
        String conteudo = "";
        if(carga.getFile().isPresent()) {
            nome = carga.getFile().get().getName();
            conteudo = Arquivos.lerArquivo(carga.getFile().get());
        }
        return ExecFile.builder()
            .evidencia(evidencia)
            .jobNome(evidencia.getJob().getNome())
            .tipo(CARGA)
            .arquivoNome(nome)
            .arquivo(conteudo)
            .inconformidade(carga.getErro())
            .build();
    }

    public static ExecFile montarEvidenciaLog(
        @NonNull Evidencia evidencia,
        @NonNull SftpFileManager<RemoteFile> log) {
        //-----------------------------------------
        return ExecFile.montarEvidencia(evidencia,log, LOG);
    }

    public static ExecFile montarEvidenciaSaida(
        @NonNull Evidencia evidencia,
        @NonNull SftpFileManager<RemoteFile> carga) {
        //-----------------------------------------
        return ExecFile.montarEvidencia(evidencia, carga, SAIDA);
    }

    private static ExecFile montarEvidencia(
        Evidencia evidencia,
        SftpFileManager<RemoteFile> fileManager,
        TipoExecFile tipo) {
        //-----------------------------------
        String nome = "";
        String conteudo = "";
        if(fileManager.getFile().isPresent()) {
            nome = fileManager.getFile().get().nome();
            conteudo = fileManager.getFile().get().conteudo();
        }
        return ExecFile.builder()
            .evidencia(evidencia)
            .jobNome(evidencia.getJob().getNome())
            .tipo(tipo)
            .arquivoNome(nome)
            .arquivo(conteudo)
            .inconformidade(fileManager.getErro())
            .build();
    }

    @Override
    public String toString() {
        return "ExecFile{" +
            "id=" + id +
            ", evidencia=" + evidencia +
            ", jobNome='" + jobNome + '\'' +
            ", tipo='" + tipo + '\'' +
            ", arquivoNome='" + arquivoNome + '\'' +
            ", arquivo=" + getResumoArquivo() +
            '}';
    }

    //TODO: mover esse método para uma classe Utils
    private String getResumoArquivo() {
        val tamanho = FormatString.contarSubstring(arquivo, "\n");
        val peso = arquivo.getBytes().length;
        return String.format("[linhas=%d, peso=%dKbs]", tamanho, peso);
    }

    @Override
    public final boolean equals(Object o) {
        if(this == o) return true;
        if(o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ?
            ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() :
            o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ?
            ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() :
            this.getClass();
        if(thisEffectiveClass != oEffectiveClass) return false;
        ExecFile execFile = (ExecFile) o;
        return getId() != null && Objects.equals(getId(), execFile.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
