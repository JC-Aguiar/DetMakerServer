package br.com.ppw.dma.domain.execFile;

import br.com.ppw.dma.domain.evidencia.Evidencia;
import br.com.ppw.dma.domain.master.MasterEntity;
import br.com.ppw.dma.net.RemoteFile;
import br.com.ppw.dma.net.SftpFileManager;
import br.com.ppw.dma.net.SftpTerminalManager;
import br.com.ppw.dma.util.FormatString;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Comment;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

import static br.com.ppw.dma.domain.execFile.TipoExecFile.*;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static lombok.AccessLevel.PRIVATE;


@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
@Entity(name = "PPW_EXEC_FILE")
@Table(name = "PPW_EXEC_FILE")
public class ExecFile implements MasterEntity<Long> {

    @Id
    @SequenceGenerator(name = "SEQ_EXEC_FILE_ID", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SEQ_EXEC_FILE_ID")
    Long id;

    @Column(name = "TICKET", length = 100, nullable = false)
    @Comment("Identificador da solicitação de um acionamento")
    String ticket;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "EVIDENCIA_ID")
    @Comment("ID da evidência relacionada com esse arquivo pós-execução")
    Evidencia evidencia;

    @Column(name = "JOB_NOME", length = 100, nullable = false)
    @Comment("Nome do Job que gerou esse arquivo")
    String jobNome;

    @Column(name = "COMANDO", length = 200, nullable = false)
    @Comment("Comando executado pelo Job para obter o arquivo")
    String comando;

    @Column(name = "MASCARA", length = 200)
    @Comment("Comando executado pelo Job para obter o arquivo")
    String mascara;

    @Column(name = "TIPO", length = 10, nullable = false)
    @Comment("Indica se o arquivo é do tipo 'carga', 'remessa' ou 'log'")
    TipoExecFile tipo;

    @Column(name = "ARQUIVO_NOME", length = 200, nullable = true)
    @Comment("Nome do arquivo")
    String arquivoNome;

    @ToString.Exclude
    @Column(name = "ARQUIVO", columnDefinition = "CLOB", nullable = true)
    @Comment("Conteúdo do arquivo")
    String arquivo;

    @Column(name = "INCONFORMIDADE", length = 200)
    @Comment("Mensagens de erro durante coleta do arquivo")
    String inconformidade;


    public static ExecFile montarEvidenciaTerminal(
        @NonNull Evidencia evidencia,
        @NonNull SftpTerminalManager terminal) {

        return ExecFile.builder()
            .evidencia(evidencia)
            .ticket(evidencia.getTicket())
            .jobNome(evidencia.getJobNome())//.getNome())
            .tipo(LOG)
            .arquivoNome("Log exibido no terminal")
            .arquivo(String.join("\n", terminal.getConsoleLog()))
            .comando(terminal.getComando())
            .build();
    }

    public static ExecFile montarEvidenciaCarga(
        @NonNull Evidencia evidencia,
        @NonNull SftpFileManager<RemoteFile> carga) {
        //-----------------------------------------
        return ExecFile.montarEvidencia(evidencia, carga, CARGA);
//        String nome = "";
//        String conteudo = "";
//        if(carga.getFile().isPresent()) {
//            nome = carga.getFile().get().getName();
//            conteudo = FileSystemService.readFile(carga.getFile().get());
//        }
//        return ExecFile.builder()
//            .evidencia(evidencia)
//            .ticket(evidencia.getTicket())
//            .jobNome(evidencia.getJobNome()) //.getNome())
//            .tipo(CARGA)
//            .arquivoNome(nome)
//            .arquivo(conteudo)
//            .inconformidade(carga.getErro())
//            .comando(carga.getComando())
//            .build();
    }

    public static ExecFile montarEvidenciaLog(
        @NonNull Evidencia evidencia,
        @NonNull SftpFileManager<RemoteFile> log) {
        //-----------------------------------------
        return ExecFile.montarEvidencia(evidencia, log, LOG);
    }

    public static ExecFile montarEvidenciaRemessa(
        @NonNull Evidencia evidencia,
        @NonNull SftpFileManager<RemoteFile> remessa) {
        //-----------------------------------------
        return ExecFile.montarEvidencia(evidencia, remessa, REMESSA);
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
            .ticket(evidencia.getTicket())
            .jobNome(evidencia.getJobNome()) //.getNome())
            .tipo(tipo)
            .arquivoNome(nome)
            .arquivo(conteudo)
            .comando(fileManager.getComando())
            .mascara(fileManager.getFileMask())
            .inconformidade(fileManager.getErro())
            .build();
    }

    //TODO: mover esse método para uma classe Utils
    @ToString.Include(name = "arquivo")
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
