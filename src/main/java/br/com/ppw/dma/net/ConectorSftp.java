package br.com.ppw.dma.net;

import com.jcraft.jsch.*;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.com.ppw.dma.util.FormatString.LINHA_HIFENS;


@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConectorSftp extends Uploader {

    //TODO: criar uma throw customizado
    public static ConectorSftp conectar(String ip, int porta, String usuario, String senha) {
        log.info("Testando conexão remota...");
        try {
            Long.parseLong(ip.trim().replace(".", ""));
            return new ConectorSftp(ip, porta, usuario, senha);
        }
        catch(NumberFormatException e) {
            throw new RuntimeException("O host informado está fora do padrão IPv4: " + e.getMessage());
        }
        catch(IOException e) {
            throw new RuntimeException("Erro inesperado: " + e.getMessage());
        }
    }

    //TODO: Javadoc
    private ConectorSftp(String server, int port, String username, String password) throws IOException {
        super(server, port, username, password);
        comando("hostname; whoami; date; uptime; uname -a");
    }

    //TODO: Javadoc
    private Session iniciarSessao() throws JSchException {
        return iniciarSessao(0);
    }

    //TODO: Javadoc
    private Session iniciarSessao(int timeout) throws JSchException {
        Session session = new JSch().getSession(getUsername(), getServer(), getPort());
        session.setPassword(getPassword());
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(timeout);
        return session;
    }

    //TODO: Javadoc
    @Override
    public int upload(@NonNull String dirRemoto, @NonNull File...arquivos) {
        int sucessos = 0;
        Session session = null;
        Channel channel = null;
        ChannelSftp sftpChannel = null;
        
        log.info("Estabelecendo conexão SFTP.");
        log.info("Quantidade de arquivos locais a serem enviados: " + arquivos.length);
        log.info("Diretório remoto de destino: " + dirRemoto);
        Arrays.stream(arquivos).forEach(
            arquivo -> log.info("Arquivo: " + arquivo.getAbsolutePath())
        );
        try {
            session = iniciarSessao();
            channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;
            for(val arq : arquivos) {
                try {
                    log.info("Realizando upload do arquivo '{}'.", arq.getAbsolutePath());
                    sftpChannel.put(arq.getAbsolutePath(), dirRemoto);
                    log.info("Upload do arquivo '{}' realizado com sucesso.", arq.getName());
                    sucessos += 1;
                }
                catch(SftpException e) {
                    log.error(
                        "Erro ao tentar realizar upload do arquivo '{}': {}",
                        arq.getName(), e.getMessage()
                    );
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            log.error("Erro inesperado curante conexão para download: {}", e.getMessage());
        }
        finally {
            if(sftpChannel != null) sftpChannel.exit();
            if(channel != null) channel.disconnect();
            if(session != null) session.disconnect();
        }
        if(sucessos > 0) log.info("Quantidade de uploads: " + sucessos);
        else log.warn("Nenhum arquivo foi enviado com sucesso.");
        return sucessos;
    }

    //TODO: Javadoc
    public int download(@NonNull Path pathLocal, @NonNull String...dirArquivosRemotos) {
        int sucessos = 0;
        Session session = null;
        Channel channel = null;
        ChannelSftp sftpChannel = null;
        val pathLocalAbsoluto = pathLocal.toFile().getAbsolutePath();
        
        log.info("Estabelecendo conexão SFTP.");
        log.info("Quantidade de arquivos remotos para baixar: " + Arrays.stream(dirArquivosRemotos).count());
        log.info("Diretório local de destino: " + pathLocalAbsoluto);
        Arrays.stream(dirArquivosRemotos).forEach(
            arquivoRemoto -> log.info("Arquivo: " + arquivoRemoto)
        );
        try {
            session = iniciarSessao();
            channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;

            log.info("Iniciando downloads...");
            for(val arquivoRemoto : dirArquivosRemotos) {
                try {
                    log.info("Realizando download do arquivo: " + arquivoRemoto);
                    sftpChannel.get(arquivoRemoto, pathLocalAbsoluto);
                    sucessos += 1;
                    log.info("Download do arquivo '{}' realizado com sucesso.", arquivoRemoto);
                }
                catch(SftpException e) {
                    e.printStackTrace();
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            log.error("Erro inesperado curante conexão para download: {}", e.getMessage());
        }
        finally {
            if(sftpChannel != null) sftpChannel.exit();
            if(channel != null) channel.disconnect();
            if(session != null) session.disconnect();
        }
        if(sucessos > 0) log.info("Quantidade de downloads: " + sucessos);
        else log.warn("Nenhum arquivo foi baixado com sucesso.");
        return sucessos;
        //TODO: print das informações do arquivo no diretório local
    }

    //TODO: Javadoc
    public List<String> comando(String comando) throws IOException {
        log.info("Estabelecendo conexão SFTP.");
        log.info("Executando comando: " + comando);
        Session session = null;
        ChannelExec channelExec = null;
        InputStream in = null;
        try {
            val properties = getShellProperties();
            val comandoFull = properties.keySet()
                .stream()
                .map(k -> "export " + k + "=" + properties.get(k))
                .collect(Collectors.joining(" && "))
                .concat(" && ")
                .concat(comando);

            session = iniciarSessao();
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(comandoFull);
            channelExec.setPty(true);
            channelExec.setErrStream(System.err, true);
            channelExec.setOutputStream(System.err, true);
            channelExec.connect();  // sugestão de timeout: 5000
            in = channelExec.getInputStream();

            //Lendo resultado exibido no servidor remoto
            log.info(LINHA_HIFENS + LINHA_HIFENS);
            val retorno = new ArrayList<String>();
            val tmp = new byte[1024];
            while(true) {
                while(in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if(i < 0) break;
                    val linhas = new String(tmp, 0, i).split("\n");
                    Stream.of(linhas).forEach(linha -> {
                        retorno.add(linha);
                        log.info(linha);
                    });
                }
                if(channelExec.isClosed()) {
                    if(in.available() > 0) continue;
                    log.info(LINHA_HIFENS + LINHA_HIFENS);
                    log.info("Código de retorno: " + channelExec.getExitStatus());
                    break;
                }
                try { Thread.sleep(1000); }
                catch(Exception ee) { }
            }
            log.info(LINHA_HIFENS + LINHA_HIFENS);
            log.info("Comando '{}' executado com sucesso.", comando);
            return retorno;
        }
        catch(JSchException | IOException e) {
            e.printStackTrace();
            log.error("Erro ao tentar executar o comando '{}'.", comando);
            return List.of();
        }
        finally {
            if(session != null) session.disconnect();
            if(channelExec != null) channelExec.disconnect();
            if(in != null) in.close();
        }
    }

    //TODO: Javadoc
    private Properties getShellProperties() {
        val properties = new Properties();
        properties.put("ORACLE_HOME", "/u01/app/oracle/product/client12.2");
        properties.put("DETECTION_AGENT_CAP", "cap_dac_override,cap_setfcap");
        properties.put("XDG_SESSION_ID", "54968");
        properties.put("HOSTNAME", "brtlvltb0169co");
        properties.put("TERM", "xterm");
        properties.put("AGENTS_RELEASE", "a46476d1cf");
        //properties.put("SSH_CLIENT", "10.238.101.33 61680 22");
        properties.put("SSH_TTY", "/dev/pts/0");
        properties.put("HISTFILESIZE", "2000");
        properties.put("LD_LIBRARY_PATH", "/u01/app/oracle/product/client12.2/lib:/lib:/usr/lib");
        properties.put("ORACLE_SID", "CYB3DEV");
        properties.put("GC_MAN_DIR", "/usr/local/share/man");
        properties.put("ORACLE_BASE", "/oracle/app/");
        properties.put("MAIL", "/var/spool/mail/rcvry");
        properties.put("PATH", "/u01/app/oracle/product/client12.2/bin:/usr/local/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/usr/local/bin:/bin:/usr/bin:/usr/X11R6/bin:/app/rcvry//bin:/app/rcvry//bin");
        properties.put("PWD", "/app/rcvry/");
        properties.put("LANG", "en_US.UTF-8");
        properties.put("GC_LOGS", "/var/log");
        properties.put("AGENTS_VERSION", "5.42.22229.16916");
        //properties.put("PS1", "[\t] \\u@$HOSTNM:\\w\\$");
        properties.put("ORACLE_SID_NODE", "CYB3DEV");
        properties.put("GC_LIB_PATH", "/usr/lib/guardicore");
        properties.put("LAUNCHER_CAP", "cap_dac_override,cap_net_admin,cap_setfcap,cap_sys_ptrace");
        properties.put("USR_HOME", "/app/rcvry");
        properties.put("REVEAL_AGENT_CAP", "cap_dac_override,cap_net_admin,cap_sys_ptrace");
        //properties.put("SSH_CONNECTION", "10.238.101.33 61680 10.129.164.206 22");
        properties.put("LESSOPEN", "||/usr/bin/lesspipe.sh %s");
        properties.put("GC_ROOT", "/var/lib/guardicore");
        properties.put("ORACLE_HOME", "/u01/app/oracle/product/client12.2");
        //properties.put("HISTTIMEFORMAT", "%Y-%m-%d %T %z");
        properties.put("HISTFILE", "/app/rcvry//.bash_history");
        properties.put("AGENT_PKG_TYPE", "rpm");
        //comando = "export ORACLE_HOME=/u01/app/oracle/product/client12.2 && "
        return properties;
    }

    //TODO: Javadoc
    public int downloadMaisRecente(Path pathLocal, String...dirArquivosRemotos) {
        return Stream.of(dirArquivosRemotos)
            .mapToInt(dir -> listarArquivoDownload(dir, pathLocal))
            .sum();
    }

    //TODO: Javadoc
    private int listarArquivoDownload(String dirArquivoNome, Path pathLocal) {
        try {
            final List<String> listaArquivos = comando("ls -t " + dirArquivoNome);
            if(listaArquivos.isEmpty()) {
                log.warn("Nenhum arquivo encontrado para '{}'", dirArquivoNome);
                return 0;
            }
            //Por algum motivo estranho o nome dos arquivos retornam com sufixo '\r' e pode causar problemas.
            val arquivoMaisRecente = listaArquivos.get(0).replace("\r", "");
            return download(pathLocal, arquivoMaisRecente);
        }
        catch(IOException e) {
            log.error("Erro ao tentar fechar leitor remoto do arquivo '{}': {}",
                dirArquivoNome, e.getMessage());
            return 0;
        }
    }
    
}
