package br.com.ppw.dma.net;

import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.exception.FtpHostException;
import br.com.ppw.dma.exception.OperacaoSftpException;
import br.com.ppw.dma.system.ExitCodes;
import com.jcraft.jsch.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Slf4j
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConectorSftp {

    @Getter String server;
    @Getter int port;
    @Getter String username;
    @ToString.Exclude String password;
    @Getter Properties properties = new Properties();

    //TODO: Javadoc
    public static ConectorSftp conectar(@NonNull AmbienteAcessoDTO sftpConfig) {
        return ConectorSftp.conectar(
            sftpConfig.getConexao(),
            sftpConfig.getUsuario(),
            sftpConfig.getSenha());
    }

    public static ConectorSftp conectar(@NonNull String host, @NonNull String usuario, String senha) {
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
//        super(server, port, username, password);
        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;
        comando("hostname; whoami; date; uptime; uname -a");
        log.info("Conexão testada e estabelecida.");
    }

    /**
     * Tenta iniciar uma sessão ao ambiente configurado
     * @return {@link Session} da sessão em caso de sucesso
     * @throws JSchException em caso de impeditivo
     */
    private Session iniciarSessao() throws JSchException {
        return iniciarSessao(0);
    }

    /**
     * Tenta iniciar uma sessão ao ambiente configurado
     * @param timeout número inteiro (em milissegundos?)
     * @return {@link Session} da sessão em caso de sucesso
     * @throws JSchException em caso de impeditivo
     */
    private Session iniciarSessao(int timeout) throws JSchException {
        Session session = new JSch().getSession(username, server, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(timeout);
        return session;
    }

    //TODO: Javadoc
    public SftpFileManager<File> upload(@NonNull String dirRemoto, @NonNull File arquivo) {
        val comando = "sftp put " + dirRemoto;
        Session session = null;
        Channel channel = null;
        ChannelSftp sftpChannel = null;
        val newUpload = new SftpFileManager<>(comando, arquivo);

        log.info("Realizando upload do arquivo: '{}'.", arquivo);
        log.info("Diretório remoto de destino: '{}'.", dirRemoto);
        try {
            session = iniciarSessao();
            channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;
            sftpChannel.put(arquivo.getAbsolutePath(), dirRemoto);
            newUpload.setSuccess(true);
            log.info("Upload realizado com sucesso.");
        }
        catch(Exception e) {
            e.printStackTrace();
            newUpload.setErro(e.getMessage());
        }
        finally {
            if(sftpChannel != null) sftpChannel.exit();
            if(channel != null) channel.disconnect();
            if(session != null) session.disconnect();
        }
        return newUpload;
    }

    //TODO: Javadoc
    public SftpFileManager<RemoteFile> download(@NonNull String arquivoRemoto)
    throws OperacaoSftpException {
        Session session = null;
        Channel channel = null;
        ChannelSftp sftpChannel = null;
        InputStream entrada = null;
        SftpFileManager<RemoteFile> newDownload = null;
        val comando = "sftp get " + arquivoRemoto;

        log.info("Realizando download do arquivo: '{}'.", arquivoRemoto);
        try {
            session = iniciarSessao();
            channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;

            val arquivoNome = arquivoRemoto.substring(arquivoRemoto.lastIndexOf('/') + 1); //arquivoRemoto.split("/");
            val conteudo = new StringBuilder();

            entrada = sftpChannel.get(arquivoRemoto);
            final SftpATTRS props = sftpChannel.lstat(arquivoRemoto);
            val leitor = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8));
            String linha;

            while((linha = leitor.readLine()) != null) {
                conteudo.append(linha).append("\n");
            }
            val arquivo = RemoteFile.addFile(
                arquivoNome,
                props.getSize(),
                props.getMTime(),
                props.getATime(),
                conteudo.toString());
            newDownload = new SftpFileManager<>(comando, arquivo);
            newDownload.setSuccess(true);
            log.info("Arquivo '{}' obtido com sucesso.", arquivo.nome());
            log.info(arquivo.toString());
        }
        catch(Exception e) {
//            e.printStackTrace();
            log.error(e.getMessage());
            newDownload = new SftpFileManager<>(comando, null);
            newDownload.setErro(e.getMessage());
        }
        finally {
            if(sftpChannel != null) sftpChannel.exit();
            if(channel != null) channel.disconnect();
            if(session != null) session.disconnect();
            if(entrada != null) {
                try {
                    entrada.close();
                }
                catch(IOException e) {
                    log.warn("Erro de I/O ao tentar fechar leitor remoto SFTP: {}.", e.getMessage());
                }
            }
        }
        return newDownload;
    }

    //TODO: Javadoc
    public SftpTerminalManager comando(String comando) throws OperacaoSftpException {
        log.info("Executando comando: " + comando);
        Session session = null;
        ChannelExec channelExec = null;
        InputStream in = null;
        InputStream err = null;
        val outputBuffer = new ByteArrayOutputStream();
        val terminal = new SftpTerminalManager();
        try {
            val comandoFull = properties.keySet()
                .stream()
                .map(k -> "export " + k + "=" + properties.get(k))
                .collect(Collectors.joining(" && "))
                .concat(" && ")
                .concat(comando);
            log.info("ComandoFull: {}", comandoFull);

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
        catch(Exception e) {
            e.printStackTrace();
            throw new OperacaoSftpException(comando, e.getMessage());
            //return terminal;
        }
        finally {
            if(session != null) session.disconnect();
            if(channelExec != null) channelExec.disconnect();
            try {
                if(in != null) in.close();
                outputBuffer.close();
            }
            catch(IOException e) {
                log.warn("Erro de I/O ao tentar fechar leitor remoto SFTP: {}.", e.getMessage());
            }
        }
    }

    //TODO: Javadoc
    public static void setVivo3Properties(@NonNull ConectorSftp sftp) {
        log.info("Adicionando variáveis de ambiente VIVO3.");
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
        //properties.put("HISTTIMEFORMAT", "%Y-%m-%d %T %z");
        properties.put("HISTFILE", "/app/rcvry//.bash_history");
        properties.put("AGENT_PKG_TYPE", "rpm");
        //comando = "export ORACLE_HOME=/u01/app/oracle/product/client12.2 && "
        sftp.properties.putAll(properties);
    }

    public static void setVivo1Properties(@NonNull ConectorSftp sftp) {
        log.info("Adicionando variáveis de ambiente VIVO1.");
        var properties = new Properties();
        var env = "" +
//            "PATH=.:/usr/lib64/qt-3.3/bin:/usr/local/bin:/bin:/usr/bin:/cyberapp/rcvry/bin:/cyberapp/rcvry/shells:/cyberapp/rcvry:/cyberapp/rcvry/bin:/cyberapp/rcvry/shells:/usr/bin:/usr/bin/X11:/usr/lib:/usr/etc:/usr/etc/sec:/usr/sbin:/usr/include:/lib:/sbin:/etc:/etc/sec:/cyberapp/rcvry/bin:/cyberapp/rcvry/shells:/cyberapp/local:/cyberapp/local/bin:/bin:/opt/oracle/product/11.2.0/client_1/bin:/opt/ansic:/opt/ansic/bin:/usr/ccs/bin:/usr/contrib/bin:/opt/nettladm/bin:/opt/pd/bin:/usr/bin/X11:/usr/contrib/bin/X11:/opt/upgrade/bin:/opt/langtools/bin:/opt/graphics/OpenGL/debugger/bin:/opt/imake/bin:/opt/java/bin\n" +
//            "SSH_TTY=/dev/pts/1\n" +
//            "TERM=xterm\n" +
//            "HOST=brtlvltb0164sl\n" +
//            "HOSTNAME=brtlvltb0164sl\n" +
//            "USR_HOME=/cyberapp/rcvry\n" +
//            "USR_HLP=/cyberapp/rcvry/hlp\n" +
//            "USR_BIN=/cyberapp/rcvry/bin\n" +
//            "USR_SCR=/cyberapp/rcvry/shells\n" +
//            "USR_TMP=/cyberapp/rcvry/tmp\n" +
//            "JAVA_HOME=/opt/jdk1.6.0_33\n" +
//            "ORACLE_BASE=/opt/oracle\n" +
//            "ORACLE_HOME=/opt/oracle/product/11.2.0/client_1\n" +
//            "ORACLE_SID=CCSSIDEV\n" +
//            "ORACLE_TERM=xterm\n" +
//            "LD_LIBRARY_PATH=/opt/oracle/product/11.2.0/client_1/lib" +
            "";
        Arrays.stream(env.split("\n"))
            .filter(var -> !var.startsWith("#"))
            .filter(var -> var.contains("="))
            .forEach(var -> {
                var varArray = var.split("=");
                properties.put(varArray[0], varArray[1]);
            });
        sftp.properties.putAll(properties);
    }

    public List<SftpFileManager<RemoteFile>> downloadMaisRecente(@NonNull List<String> arquivosRemotos) {
        return downloadMaisRecente(arquivosRemotos.toArray(new String[0]));
    }

    public List<SftpFileManager<RemoteFile>> downloadMaisRecente(String...arquivosRemotos) {
        log.debug("Total de arquivos para download: {}.", arquivosRemotos.length);
        var sucessos = new AtomicInteger();
        val totalDownloads = Arrays.stream(arquivosRemotos)
            .map(this::downloadMaisRecente)
            .peek(download -> sucessos.addAndGet(download.isSuccess() ? 1 : 0))
            .toList();
        if(sucessos.get() > 0)
            log.info("Total de downloads obtidos: {}", sucessos);
        else
            log.warn("Nenhum download obtido com sucesso.");
        return totalDownloads;
    }

    //TODO: Javadoc
    public SftpFileManager<RemoteFile> downloadMaisRecente(String arquivoNome) {
        val comandoInput = "ls -t " + arquivoNome + " | head -1";
        log.info("Comando a ser executado: {}", comandoInput);
        try {
            if(arquivoNome == null || arquivoNome.isEmpty())
                throw new OperacaoSftpException(comandoInput, "Diretório/arquivo inválido.");

            val listaArquivos = comando(comandoInput).getConsoleLog();
            if(listaArquivos.isEmpty())
                throw new OperacaoSftpException(comandoInput, "Nenhum arquivo encontrado.");

            //Por algum motivo estranho o name dos arquivos retornam concatenados com '\r' e pode causar problemas.
            val arquivoMaisRecente = listaArquivos.get(0).replace("\r", "");
            log.info("Arquivo mais recente: '{}'", arquivoMaisRecente);
            return download(arquivoMaisRecente);
        }
        catch (OperacaoSftpException e) {
            val retorno = new SftpFileManager<RemoteFile>(e.comando, null);
            retorno.setErro(e.getMessage());
            return retorno;
        }
    }
    
}
