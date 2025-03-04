package br.com.ppw.dma.net;

import br.com.ppw.dma.domain.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.exception.FtpHostException;
import br.com.ppw.dma.exception.OperacaoSftpException;
import br.com.ppw.dma.util.ExitCodes;
import br.com.ppw.dma.util.FormatDate;
import com.jcraft.jsch.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
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
//        dirRemoto = dirRemoto.startsWith("/") ? dirRemoto : "/" + dirRemoto;
        val comando = "sftp put " + dirRemoto;
//        val comando = dirRemoto;
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
            //TODO: avaliar uma forma de avaliar a extensão do arquivo para mudar o modo de envio entre texto e binário
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
//        val comando = arquivoRemoto;

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
            newDownload.setFileMask(arquivoRemoto);
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
        val terminal = new SftpTerminalManager(comando);
        try {
            val comandoFull = properties.keySet()
                .stream()
                .map(k -> "export " + k + "=" + properties.get(k))
                .collect(Collectors.joining(" && "))
                .concat(properties.size() > 0 ? " && " : "")
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
                terminal.addPrintedLine(linha.replace("\r", ""));
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
        sftp.properties.clear();
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

    public static void setVivo2Properties(@NonNull ConectorSftp sftp) {
        log.info("Adicionando variáveis de ambiente VIVO2.");
        sftp.properties.clear();
        val properties = new Properties();
        properties.setProperty("HOSTNAME", "svuxdjob2");
        properties.setProperty("TERM", "xterm");
//        properties.setProperty("SSH_CLIENT", "10.238.101.33 56708 22");
        properties.setProperty("QTDIR", "/usr/lib64/qt-3.3");
        properties.setProperty("OLDPWD", "/app/rcvry/");
        properties.setProperty("QTINC", "/usr/lib64/qt-3.3/include");
        properties.setProperty("SSH_TTY", "/dev/pts/0");
        properties.setProperty("LD_LIBRARY_PATH", "/oracle/app/11.2.0/client_2/lib:/lib:/usr/lib");
//        properties.setProperty("LS_COLORS", "rs=0:di=01;34:ln=01;36:mh=00:pi=40;33:so=01;35:do=01;35:bd=40;33;01:cd=40;33;01:or=40;31;01:su=37;41:sg=30;43:ca=30;41:tw=30;42:ow=34;42:st=37;44:ex=01;32:*.tar=01;31:*.tgz=01;31:*.arj=01;31:*.taz=01;31:*.lzh=01;31:*.lzma=01;31:*.tlz=01;31:*.txz=01;31:*.zip=01;31:*.z=01;31:*.Z=01;31:*.dz=01;31:*.gz=01;31:*.lz=01;31:*.xz=01;31:*.bz2=01;31:*.bz=01;31:*.tbz=01;31:*.tbz2=01;31:*.tz=01;31:*.deb=01;31:*.rpm=01;31:*.jar=01;31:*.rar=01;31:*.ace=01;31:*.zoo=01;31:*.cpio=01;31:*.7z=01;31:*.rz=01;31:*.jpg=01;35:*.jpeg=01;35:*.gif=01;35:*.bmp=01;35:*.pbm=01;35:*.pgm=01;35:*.ppm=01;35:*.tga=01;35:*.xbm=01;35:*.xpm=01;35:*.tif=01;35:*.tiff=01;35:*.png=01;35:*.svg=01;35:*.svgz=01;35:*.mng=01;35:*.pcx=01;35:*.mov=01;35:*.mpg=01;35:*.mpeg=01;35:*.m2v=01;35:*.mkv=01;35:*.ogm=01;35:*.mp4=01;35:*.m4v=01;35:*.mp4v=01;35:*.vob=01;35:*.qt=01;35:*.nuv=01;35:*.wmv=01;35:*.asf=01;35:*.rm=01;35:*.rmvb=01;35:*.flc=01;35:*.avi=01;35:*.fli=01;35:*.flv=01;35:*.gl=01;35:*.dl=01;35:*.xcf=01;35:*.xwd=01;35:*.yuv=01;35:*.cgm=01;35:*.emf=01;35:*.axv=01;35:*.anx=01;35:*.ogv=01;35:*.ogx=01;35:*.aac=00;36:*.au=00;36:*.flac=00;36:*.mid=00;36:*.midi=00;36:*.mka=00;36:*.mp3=00;36:*.mpc=00;36:*.ogg=00;36:*.ra=00;36:*.wav=00;36:*.axa=00;36:*.oga=00;36:*.spx=00;36:*.xspf=00;36:");
        properties.setProperty("ORACLE_SID", "qacyb");
        properties.setProperty("ORACLE_BASE", "/oracle/app/");
        properties.setProperty("MAIL", "/var/spool/mail/rcvry");
        properties.setProperty("PATH", "/oracle/app/11.2.0/client_2/bin:/usr/lib64/qt-3.3/bin:/usr/local/bin:/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/sbin:/usr/local/bin:/bin:/usr/bin:/usr/X11R6/bin:/app/rcvry//bin:/app/rcvry//bin");
        properties.setProperty("PWD", "/app/rcvry/shells");
        properties.setProperty("LANG", "en_US.UTF-8");
        properties.setProperty("ORACLE_SID_NODE", "qacyb");
        properties.setProperty("SSH_ASKPASS", "/usr/libexec/openssh/gnome-ssh-askpass");
        properties.setProperty("USR_HOME", "/app/rcvry");
        properties.setProperty("QTLIB", "/usr/lib64/qt-3.3/lib");
        properties.setProperty("CVS_RSH", "ssh");
//        properties.setProperty("SSH_CONNECTION", "10.238.101.33 56708 10.42.252.76 22");
        properties.setProperty("LESSOPEN", "|/usr/bin/lesspipe.sh %s");
        properties.setProperty("ORACLE_HOME", "/oracle/app/11.2.0/client_2");
        properties.setProperty("G_BROKEN_FILENAMES", "1");
        sftp.properties.putAll(properties);
    }

    public static void setVivo1Properties(@NonNull ConectorSftp sftp) {
        log.info("Adicionando variáveis de ambiente VIVO1.");
        sftp.properties.clear();
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
        try {
            if(arquivoNome == null || arquivoNome.isEmpty())
                throw new OperacaoSftpException(comandoInput, "Diretório/arquivo inválido.");

            //Por algum motivo estranho o name dos arquivos retornam concatenados com '\r' e pode causar problemas.
            val arquivoMaisRecente = comando(comandoInput).getConsoleLog()
                .stream()
                .findFirst()
                .map(maisRecente -> maisRecente.replace("\r", ""))
                .orElseThrow(() -> new OperacaoSftpException(comandoInput, "Nenhum arquivo encontrado."));
            log.info("Arquivo mais recente: '{}'", arquivoMaisRecente);
            var retorno = download(arquivoMaisRecente);
            retorno.setFileMask(arquivoNome);
            return retorno;
        }
        catch(OperacaoSftpException e) {
            val retorno = new SftpFileManager<RemoteFile>(e.comando, null);
            retorno.setErro(e.getMessage());
            retorno.setFileMask(arquivoNome);
            return retorno;
        }
    }

    public List<SftpFileManager<RemoteFile>> downloadAll(String arquivoNome) {
        val comandoInput = "ls -t " + arquivoNome;
        try {
            if(arquivoNome == null || arquivoNome.isEmpty())
                throw new OperacaoSftpException(comandoInput, "Diretório/arquivo inválido.");

            //Por algum motivo estranho o name dos arquivos retornam concatenados com '\r' e pode causar problemas.
            val listaArquivos = comando(comandoInput).getConsoleLog()
                .stream()
                .map(maisRecente -> maisRecente.replace("\r", ""))
                .filter(Predicate.not(String::isBlank))
                .toList();
            if(listaArquivos.isEmpty())
                throw new OperacaoSftpException(comandoInput, "Nenhum arquivo encontrado.");
            log.info("Arquivos:");
            listaArquivos.forEach(log::info);
            log.info("Iniciando fluxo de download dos arquivos listados.");
            return listaArquivos.stream()
                .peek(log::info)
                .map(this::download)
                .peek(retorno -> retorno.setFileMask(arquivoNome))
                .toList();
        }
        catch(OperacaoSftpException e) {
            val retorno = new SftpFileManager<RemoteFile>(e.comando, null);
            retorno.setErro(e.getMessage());
            retorno.setFileMask(arquivoNome);
            return List.of(retorno);
        }
    }

    public List<SftpFileManager<RemoteFile>> downloadAllRecente(String arquivoNome, LocalDateTime data) {
        var dataString = data.format(FormatDate.BASH_PARAMETER_STYLE);
        val comandoInput = String.format("find %s -type f -newermt \"%s\" -print", arquivoNome, dataString);
        try {
            if(arquivoNome == null || arquivoNome.isEmpty())
                throw new OperacaoSftpException(comandoInput, "Diretório/arquivo inválido.");

            //Por algum motivo estranho o name dos arquivos retornam concatenados com '\r' e pode causar problemas.
            val listaArquivos = comando(comandoInput).getConsoleLog()
                .stream()
                .map(maisRecente -> maisRecente.replace("\r", ""))
                .filter(Predicate.not(String::isBlank))
                .toList();
            if(listaArquivos.isEmpty())
                throw new OperacaoSftpException(comandoInput, "Nenhum arquivo encontrado.");
            log.info("Arquivos:");
            listaArquivos.forEach(log::info);
            log.info("Iniciando fluxo de download dos arquivos listados.");
            return listaArquivos.stream()
                .peek(log::info)
                .map(this::download)
                .peek(retorno -> retorno.setFileMask(arquivoNome))
                .toList();
        }
        catch(OperacaoSftpException e) {
            val retorno = new SftpFileManager<RemoteFile>(e.comando, null);
            retorno.setErro(e.getMessage());
            retorno.setFileMask(arquivoNome);
            return List.of(retorno);
        }
    }
    
}
