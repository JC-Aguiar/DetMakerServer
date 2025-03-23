package br.com.ppw.dma;

import br.com.ppw.dma.domain.ambiente.Ambiente;
import br.com.ppw.dma.domain.job.Job;
import br.com.ppw.dma.domain.job.JobResource;
import br.com.ppw.dma.domain.job.JobResourceAccessType;
import br.com.ppw.dma.domain.job.JobResourceType;
import br.com.ppware.git_delivery.domain.SftpProps;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j
public class BasicTestSftpConnection {

    public static Ambiente ambiente;
    public static SftpProps sftpProps;

    @BeforeAll
    public static void criandoAmbienteVivo3Dev() {
        log.info("Criando ambiente VIVO3 DEV.");
        ambiente = Ambiente.builder()
            .nome("Vivo3")
            .nome("DEV")
            .conexaoSftp("${DETMAKER_HOST_3}:22")
            .usuarioSftp("rcvry")
            .senhaSftp("Ppw@1022")
            .conexaoBanco("10.129.164.205:1521:cyb3dev")
            .usuarioBanco("rcvry")
            .senhaBanco("rcvry")
            .build();
        log.info(ambiente.toString());
        sftpProps = SftpProps.builder()
            .host("${DETMAKER_HOST_3}")
            .port(22)
            .user("rcvry")
            .password("Ppw@1022")
            .build();
    }

    @Test
    public void acionandoJobComApacheMina() throws Exception {
        log.info("Criando Job de teste.");
        var job = Job.builder()
            .nome("cy3_shell_kafka_close_consumers.ksh")
            .caminhoExec("/app/rcvry/shells")
            .descricao("Sinaliza e aguarda a finalização dos processos consumer para que novos processos sejam disparados pelo novo ciclo diário iniciado")
            .mascarasLog(List.of(
                JobResource.builder()
                    .acesso(JobResourceAccessType.FTP)
                    .diretorio("/app/rcvry/cy3/log")
                    .mascara("cy3_shell_kafka_close_consumers_????????_??????.log")
                    .tipo(JobResourceType.LOG)
                    .build()
            ))
            .build();
        job.getMascarasLog().forEach(log -> log.setJob(job));
        log.info(job.toString());

        try(var operator = sftpProps.conect()) {
            operator.bashCommand("ls -ltr")
                .handle(System.out::println, System.err::println);
        }
    }

}
