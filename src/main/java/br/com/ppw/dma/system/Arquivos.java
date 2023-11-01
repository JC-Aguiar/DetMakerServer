package br.com.ppw.dma.system;

import br.com.ppw.dma.exception.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static br.com.ppw.dma.system.TipoPermissao.*;


@Slf4j
public class Arquivos {
    
    private static final String ERRO_PARAMETRO_DIRETORIO = "É obrigatório informar o diretório a ser localizado";
    private static final String ERRO_PARAMETRO_PERMISSOES = "É obrigatório informar o diretório a ser localizado";

    public static void validarCriarDiretorio(String dir) throws DiretorioSemPermissaoException {
        Path diretorio = Paths.get(dir);
        if(!diretorio.isAbsolute()) {
            validarCriarDiretorio(
                new File("")
                    .getAbsolutePath()
                    .concat(File.separator)
                    .concat(dir)
            );
            return;
        }
        if(diretorio.toFile().exists()) {
            log.info("Diretório '{}' já consta disponível", dir);
            validaPermissoesDiretorio(diretorio, Arrays.asList(ESCREVER, LER));
            return;
        };
        validaPermissoesDiretorio(diretorio.getParent(), Arrays.asList(ESCREVER, LER));
        if(diretorio.toFile().mkdir())
            log.info("Diretório '{}' criado com sucesso", dir);
        else
            throw new DiretorioSemPermissaoException(diretorio.toFile().getAbsolutePath(), ESCREVER);
    }

    /**
     * Método que localiza se o arquivo informado existe localmente.
     * @param caminho {@link String} diretório do arquivo alvo
     * @param arquivoNome {@link String} nome do arquivo alvo
     * @param permissoes lista {@link TipoPermissao} das permissões a validar
     * @throws CheckedException se o arquivo/diretório não existem, ou não possuem permissões
     */
    public static void localizar(
        @NotBlank(message = ERRO_PARAMETRO_DIRETORIO) String caminho,
        String arquivoNome,
        @NotEmpty(message = ERRO_PARAMETRO_PERMISSOES) List<TipoPermissao> permissoes)
    throws CheckedException {
        log.trace("Localizando existência do caminho '{}{}'...", caminho, arquivoNome);
        val permissoesString = permissoes.stream()
            .map(t -> t.operacao)
            .collect(Collectors.joining(" "));

        log.trace("Validando diretório e permissões: {}", permissoesString);
        Path path = Paths.get(caminho);
        if(!Files.exists(path) || !Files.isDirectory(path))
            throw new DiretorioAusenteException(caminho);
        log.trace("Diretório localizado");
        validaPermissoesDiretorio(path, permissoes);
        log.trace("Diretório com as devidas permissões");

        if(arquivoNome == null || arquivoNome.isEmpty()) return;
        log.trace("Validando arquivo e permissões: {}", permissoesString);
        path = Paths.get(caminho + arquivoNome);
        if(!Files.exists(path) || !Files.isRegularFile(path))
            throw new ArquivoAusenteException(arquivoNome, caminho);
        log.trace("Arquivo localizado");
        validaPermissoesArquivo(path, permissoes);
        log.trace("Arquivo com as devidas permissões");
    }

    //TODO: javadoc
    public static void validaPermissoesDiretorio(Path path, List<TipoPermissao> permissoes)
    throws DiretorioSemPermissaoException {
        for(TipoPermissao p : permissoes) {
            switch(p) {
                case LER:
                    if(!Files.isReadable(path))
                        throw new DiretorioSemPermissaoException(path.toFile().getAbsolutePath(), LER);
                    break;
                case EXECUTAR:
                    if(!Files.isExecutable(path))
                        throw new DiretorioSemPermissaoException(path.toFile().getAbsolutePath(), EXECUTAR);
                    break;
                case ESCREVER:
                    if(!Files.isWritable(path))
                        throw new DiretorioSemPermissaoException(path.toFile().getAbsolutePath(), ESCREVER);
                    break;
            }
        }
    }

    //TODO: javadoc
    public static void validaPermissoesArquivo(Path path, List<TipoPermissao> permissoes)
    throws ArquivoSemPermissaoException {
        for (TipoPermissao p : permissoes) {
            switch(p) {
                case LER:
                    if(!Files.isReadable(path))
                        throw new ArquivoSemPermissaoException(path.toFile().getAbsolutePath(), LER);
                    break;
                case EXECUTAR:
                    if(!Files.isExecutable(path))
                        throw new ArquivoSemPermissaoException(path.toFile().getAbsolutePath(), EXECUTAR);
                    break;
                case ESCREVER:
                    if(!Files.isWritable(path))
                        throw new ArquivoSemPermissaoException(path.toFile().getAbsolutePath(), ESCREVER);
                    break;
            }
        }
    }
    
    //TODO: javadoc
    public static File criarDiretorio(String dirEntrada) {
        log.trace("Criando novo diretorio '{}'...", dirEntrada);
        val path = Paths.get(dirEntrada);
        if(Files.exists(path)) {
            log.trace("Diretorio '{}' já existe", dirEntrada);
            return path.toFile();
        }
        val novoArquivo = new File(dirEntrada);
        if(novoArquivo.mkdir()) {
            log.trace("Novo diretório gerado com sucesso");
            return novoArquivo;
        }
        throw new RuntimeException("Algo inesperado impediu a criação do diretório '"  + dirEntrada + "'");
    }
    
    //TODO: javadoc
    public static File criarEscrever(String dirEntrada, String nome, String linha)
    throws IOException {
        val arquivo = criar(dirEntrada, nome).orElseThrow(
            () -> new RuntimeException("Algo inesperado impediu a criação do arquivo '" + nome + "'")
        );
        escreverArquivo(arquivo, linha);
        return arquivo;
    }

    //TODO: javadoc
    public static Optional<File> criar(String dirEntrada, String nome) {
        val path = Paths.get(dirEntrada, nome);
        if(path.toFile().exists()) return Optional.of(path.toFile());

        log.trace("Criando novo arquivo '{}'...", path.toFile().getAbsolutePath());
        if(Files.exists(path)) {
            log.error("Arquivo '{}' já existe! Operação cancelada...", path.toFile().getAbsolutePath());
            return Optional.empty();
        }
        val novoArquivo = new File(dirEntrada, nome);
        try {
            if(novoArquivo.createNewFile()) {
                log.trace("Novo arquivo gerado com sucesso");
                return Optional.of(novoArquivo);
            }
            log.error("Algo bloqueou a criação do arquivo '{}'", path.toFile().getAbsolutePath());
            return Optional.empty();
        }
        catch(IOException e) {
            e.printStackTrace();
            log.error("Erro inesperado ao tentar criar o arquivo '{}'", path.toFile().getAbsolutePath());
            return Optional.empty();
        }
    }
    
    //TODO: javadoc
    public static void escreverArquivo(File arquivo, String linha) throws IOException {
        log.trace("Escrevendo no arquivo '{}'...", arquivo.getName());
        log.trace("Conteúdo: {}", linha);
        val writer = new BufferedWriter(new FileWriter(arquivo, true));  //true -> permite não sobre-escrever
        writer.write(linha);
        writer.close();
        log.trace("Arquivo preenchido com sucesso");
    }

    //TODO: javadoc
    public static void sobrescrever(File arquivo, String linha) throws IOException {
        log.trace("Sobrescrevendo arquivo '{}'...", arquivo.getName());
        log.trace("Novo conteúdo: {}", linha);
        val writer = new BufferedWriter(new FileWriter(arquivo));
        writer.write(linha);
        writer.close();
        log.trace("Arquivo atualizado com sucesso");
    }

    public static List<File> loadArquivos(@NonNull Path path) {
        return loadArquivos(path, "*");
    }

    public static List<File> loadArquivos(@NonNull Path path, @NotBlank String nome) {
        val complemento = nome.equals("*") ? "" : ", padrão: '" +nome+ "'";
        log.info("Carregando arquivos do path: '{}' {}", path, complemento);
        val arquivos = new ArrayList<File>();
        try(DirectoryStream<Path> diretorio = Files.newDirectoryStream(path, nome)) {
            for(Path arquivoPath : diretorio) {
                if(Files.isRegularFile(arquivoPath)) {
                    arquivos.add(arquivoPath.toFile());
                }
            }
            return arquivos;
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String lerArquivo(@NonNull File arquivo) {
        val arquivoString = new StringBuilder();
        try (val reader = new BufferedReader(new FileReader(arquivo))) {
            String line;
            while((line = reader.readLine()) != null) {
                arquivoString.append(line);
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return arquivoString.toString();
    }
}
