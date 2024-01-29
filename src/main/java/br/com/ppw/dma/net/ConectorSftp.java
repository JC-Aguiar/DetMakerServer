package br.com.ppw.dma.net;

import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.exception.FtpHostException;
import br.com.ppw.dma.system.ExitCodes;
import com.jcraft.jsch.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConectorSftp extends Uploader {

    //TODO: Javadoc
    public static ConectorSftp conectar(@NotBlank String host, @NotBlank String usuario, String senha) {
        log.info("Validando IP e PORTA para conexão.");
        val ipPorta = List.of(host.split(":"));
        if(ipPorta.size() < 2) throw new FtpHostException();

        try {
            val ip = ipPorta.get(0);
            val porta = Integer.parseInt(ipPorta.get(1));
            Long.parseLong(ip.trim().replace(".", ""));
            log.info("Validação aprovada.");

            log.info("Testando conexão remota...");
            return new ConectorSftp(ip, porta, usuario, senha);
        }
        catch(NumberFormatException e) {
            throw new FtpHostException();
        }
        catch(IOException e) {
            throw new RuntimeException("Erro inesperado: " + e.getMessage());
        }
    }

    //TODO: Javadoc
    private ConectorSftp(String server, int port, String username, String password) throws IOException {
        super(server, port, username, password);
        comando("hostname; whoami; date; uptime; uname -a");
        log.info("Conexão testada e estabelecida.");
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
    public List<RemoteFile> download(@NonNull String...arquivosRemotos) {
        Session session = null;
        Channel channel = null;
        ChannelSftp sftpChannel = null;
        val arquivosObtidos = new ArrayList<RemoteFile>();
        
        log.info("Quantidade de arquivos remotos para baixar: " + Arrays.stream(arquivosRemotos).count());
        log.info("Arquivos para download:");
        Arrays.stream(arquivosRemotos).forEach(info -> log.info(" - {}", info));
        try {
            session = iniciarSessao();
            channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;
            //val regex = "^(?!.*\\s)[^\\s]*\\.[^\\s/]*(/[^\\s/]*)*$";

            log.info("Iniciando downloads...");
            for(val arquivoRemoto : arquivosRemotos) {
                //if(!Arquivos.validarCaminho(arquivoRemoto)) continue;

                log.info("Realizando download do arquivo: '{}'.", arquivoRemoto);
                val arquivoNome = arquivoRemoto.split("/");
                final SftpATTRS props = sftpChannel.lstat(arquivoRemoto);
                val conteudo = new StringBuilder();
                //sftpChannel.get(arquivoRemoto, pathLocalAbsoluto);

                try(val entrada = sftpChannel.get(arquivoRemoto)) {
                    val leitor = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8));
                    String linha;
                    while((linha = leitor.readLine()) != null) {
                        conteudo.append(linha).append("\n");
                    }
                    //arquivosObtidos.add(new File(pathLocalAbsoluto, arquivoNome[arquivoNome.length-1]));
                    val arquivo = RemoteFile.addFile(
                        arquivoNome[arquivoNome.length-1],
                        props.getSize(),
                        props.getMTime(),
                        props.getATime(),
                        conteudo.toString());
                    log.info("Arquivo '{}' salvo em memória com sucesso.", arquivo.nome());
                    log.info(arquivo.toString());
                    arquivosObtidos.add(arquivo);
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
        if(arquivosObtidos.isEmpty()) log.warn("Nenhum arquivo foi baixado com sucesso.");

        //TODO: print das informações do arquivo no diretório local
        return arquivosObtidos;
    }

    //TODO: Javadoc
    public TerminalManager comando(String comando) throws IOException {
        log.info("Executando comando: " + comando);
        Session session = null;
        ChannelExec channelExec = null;
        InputStream in = null;
        InputStream err = null;
        val outputBuffer = new ByteArrayOutputStream();
        val terminal = new TerminalManager();
        try {
            val properties = getShellProperties();
            val comandoFull = properties.keySet()
                .stream()
                .map(k -> "export " + k + "=" + properties.get(k))
                .collect(Collectors.joining(" && "))
                .concat(" && ")
                .concat(comando);

            session = iniciarSessao();
            session.setTimeout(36000);  //timeout de conexão ao ambiente
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(comandoFull);
            channelExec.setPty(true);
            channelExec.setErrStream(outputBuffer, true);
            channelExec.setOutputStream(outputBuffer, true);
            channelExec.connect(4000); //timeout para tentativa de conexão
            in = channelExec.getInputStream();
            err = channelExec.getExtInputStream();

            //Lendo resultado exibido no servidor remoto
//            log.debug(LINHA_HIFENS + LINHA_HIFENS);
            while(true) {
                while(in.available() > 0) {
                    outputBuffer.write(in.readAllBytes());
                }
                while(err.available() > 0) {
                    outputBuffer.write(err.readAllBytes());
                }
                if(channelExec.isClosed()) {
                    if((in.available() > 0) || (err.available() > 0)) continue;

                    terminal.setExitCode(channelExec.getExitStatus());
                    log.info("Código de retorno: {} ({})",
                        terminal.getExitCode(),
                        ExitCodes.getDescriptionFromCode(terminal.getExitCode())
                    );
                    break;
                }
                try { Thread.sleep(500); }
                catch(Exception ignore) {}
            }
            for(val linha : outputBuffer.toString(StandardCharsets.UTF_8).split("\n")) {
                log.info("(TERMINAL) {}", linha); //TODO: mover para debug?
                terminal.addPrintedLine(linha);
            }
            return terminal;
        }
        catch(JSchException e) {
            //e.printStackTrace();
            //log.error("Erro ao tentar executar o comando '{}'.", comando);
            throw new RuntimeException("Erro ao tentar executar o comando '" +comando+ "': " +e.getMessage());
            //return terminal;
        }
        finally {
            if(session != null) session.disconnect();
            if(channelExec != null) channelExec.disconnect();
            if(in != null) in.close();
            outputBuffer.close();
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
    public List<DownloadManager> downloadMaisRecentePreJob(List<String> arquivosRemotos) {
        return downloadMaisRecentePreJob(arquivosRemotos.toArray(new String[0]));
    }

    public List<DownloadManager> downloadMaisRecentePreJob(String...arquivosRemotos) {
        val totalDownloads = new ArrayList<DownloadManager>();

        log.debug("Total de referências a coletar: {}.", arquivosRemotos.length);
        log.debug(" - {}", String.join(", ", arquivosRemotos));

        for(val arquivoRemoto : arquivosRemotos) {
            val download = listarArquivoDownload(arquivoRemoto);
            totalDownloads.add(new DownloadManager(arquivoRemoto, download));
        }
        if(!totalDownloads.isEmpty())
            log.info("Total de totalDownloads: {}", totalDownloads.size());
        else
            log.warn("Nenhum download realizado com sucesso.");

        return totalDownloads;
    }

    //TODO: Javadoc
    public void downloadMaisRecentePosJob(List<DownloadManager> gerenciadores) {
        downloadMaisRecentePosJob(gerenciadores.toArray(new DownloadManager[0]));
    }

    public void downloadMaisRecentePosJob(DownloadManager...gerenciadores) {
        log.debug("Total de referências a coletar: {}.", gerenciadores.length);
        for(val g : gerenciadores) log.debug(" - {}", g.reference);

        for(val gerenciador : gerenciadores) {
            val download = listarArquivoDownload(gerenciador.reference);
             gerenciador.setPostFile(gerenciador.reference, download);
        }
        val sucessos = Arrays.stream(gerenciadores)
            .map(DownloadManager::getPostFile)
            .filter(Objects::nonNull)
            .toList()
            .size();
        if(sucessos > 0)
            log.info("Total de downloads: {}", sucessos);
        else
            log.warn("Nenhum download realizado com sucesso.");
    }

    //TODO: Javadoc
    private Optional<RemoteFile> listarArquivoDownload(String arquivoNome) {
        if(arquivoNome == null || arquivoNome.isEmpty()) {
            log.warn("Nenhuma referência de diretório/arquivo especificada para download.");
            return Optional.empty();
        }
        try {
            val listaArquivos = comando("ls -t " + arquivoNome + " | head -1").getConsoleLog();
            if(listaArquivos.isEmpty()) {
                log.warn("Nenhum arquivo encontrado para '{}'.", arquivoNome);
                return Optional.empty();
            }
            //Por algum motivo estranho o nome dos arquivos retornam concatenados com '\r' e pode causar problemas.
            val arquivoMaisRecente = listaArquivos.get(0).replace("\r", "");
            log.info("Arquivo mais recente: '{}'", arquivoMaisRecente);
            return download(arquivoMaisRecente).stream().findFirst();
        }
        catch(IOException e) {
            log.error("Erro ao fechar leitor remoto do arquivo '{}': {}.", arquivoNome, e.getMessage());
            return Optional.empty();
        }
    }
    
}
