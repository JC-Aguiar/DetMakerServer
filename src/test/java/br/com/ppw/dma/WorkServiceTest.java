package br.com.ppw.dma;

import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.lang.Nullable;
import org.yaml.snakeyaml.error.MissingEnvironmentVariableException;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

import static br.com.ppw.dma.Work.ResponseType.JSON;
import static br.com.ppw.dma.WorkValidationFilter.*;
import static br.com.ppw.dma.WorkValidationFilter.JSON_PATH;
import static lombok.AccessLevel.PRIVATE;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@FieldDefaults(level = PRIVATE)
public class WorkServiceTest {

    private static String bashPath;

    record WorkExecutionResult(boolean fatalError, int code, @NonNull List<String> messages) {
        public String concatAllMessages() {
            return String.join("\n", messages);
        }
    }

    @BeforeEach
    public void prepararTestes() {
        bashPath = Optional.ofNullable(System.getenv("BASH_PATH"))
            .map(Path::of)
            .map(Path::toAbsolutePath)
            .map(Path::toString)
            .orElseThrow(() -> new MissingEnvironmentVariableException("BASH_PATH"));
    }

    public WorkExecutionResult executeWork(@Nullable Work work) {
        var isComandoValido = Optional.ofNullable(work)
            .map(Work::getComando)
            .filter(Objects::nonNull)
            .filter(Predicate.not(String::isBlank))
            .isPresent();

        if(!isComandoValido)
            return new WorkExecutionResult(true, -1, List.of("Comando cURL não declarado."));

        Function<Exception, WorkExecutionResult> errorHandler = (e) -> {
            log.warn("Falha: {}", e.getMessage());
            var message = Optional.ofNullable(e.getMessage())
                .orElseGet(() -> e.getClass().getSimpleName());
            return new WorkExecutionResult(true, -1, List.of(message));
        };
        try {
            var comando = work.getComando();
            log.info("Executando comando cURL nativamente.");
            log.info("Comando: '{}'", comando);
            var processBuilder = new ProcessBuilder(bashPath, "-c", comando);
            return processBuilder.start()
                .onExit()
                .thenApply(p -> {
                    try(var info = p.inputReader(); var error = p.errorReader()) {
                        var result = new ArrayList<>(info.lines().toList());
                        if(p.exitValue() != 0)
                            result.addAll(error.lines().toList());
                        log.info("Resultado:");
                        result.forEach(log::info);
                        return new WorkExecutionResult(false, p.exitValue(), result);
                    }
                    catch(Exception e) {
                        return errorHandler.apply(e);
                    }
                })
                .get(30, TimeUnit.SECONDS);
        }
        catch(Exception e) {
            return errorHandler.apply(e);
        }
    }

    record WorkValidationResult(
        boolean approved,
        @NonNull Map<String, String> variables,
        @NonNull List<String> failures)
    {}

    public WorkValidationResult executeWorkValidation(List<WorkValidation> validations, WorkExecutionResult result) {
        var content = result.concatAllMessages();
        var mapaVariaveis = new HashMap<String, String>(validations.size());
        var erros = new ArrayList<String>(validations.size());

        for(var validation : validations) {
            validation.check(content).ifPresentOrElse(
                value -> validation.getVariavel().ifPresent(var -> mapaVariaveis.put(var, value)),
                () -> erros.add(validation.getUnapprovedMessage(content))
            );
        }
        log.info("Total de validações aprovadas: [{}]", validations.size() - erros.size());
        log.info("Total de validações desaprovadas: [{}]", erros.size());
        if(!erros.isEmpty()) erros.forEach(log::info);
        return new WorkValidationResult(erros.isEmpty(), mapaVariaveis, erros);
    }

    @Test
    public void testWorkBashExecution_curlVersionCommand() {
        log.info("Testando comando Bash com {}.", Work.class.getSimpleName());
        var work = Work.ssh(
            "curl --version",
            CONTAINS.value("alt-svc AsynchDNS HSTS HTTPS-proxy")
        );
        log.info(work.toString());

        var workResult = executeWork(work);
        assertNotNull(workResult);
        assertFalse(workResult.fatalError);
        assertEquals(workResult.code, 0);
        assertNotNull(workResult.messages);
        assertNotEquals(workResult.messages.size(), 0);

        var validationResult = executeWorkValidation(work.getValidacoes(), workResult);
        assertTrue(validationResult.approved);
        assertEquals(validationResult.failures.size(), 0);
        assertEquals(validationResult.variables.size(), 0);
    }

    @Test
    public void testWorkExecution_curlRequestCommand() {
        assertNotNull(System.getenv("BASH_PATH"));

        log.info("Testando comando cURL com {}.", Work.class.getSimpleName());
        var curl = """
        curl --location 'https://autenticaint.vivo.com.br/ms_oauth/oauth2/endpoints/vivooauthservice/tokens' 
        --header 'Authorization: Basic ZmRmMmNlYTZhMzdlNGM1OGIwMDAwMWY5NzVmNDVjMzU6MTgxNDNlNDZmNzc2NDZiOA==' 
        --header 'Content-Type: application/x-www-form-urlencoded;charset=UTF-8' 
        --data 'grant_type=password&scope=ServiceAccount.Profile&username=svc-cyber3&password=,qU#-yVp0cm*'
        """.replace("\n" ," ");

        var work = Work.ssh(
            curl,
            JSON_PATH.value("$.access_token")
        );
        log.info(work.toString());

        var workResult = executeWork(work);
        assertNotNull(workResult);
        assertFalse(workResult.fatalError);
        assertEquals(0, workResult.code);
        assertNotNull(workResult.messages);
        assertNotEquals(workResult.messages.size(), 0);

        var validationResult = executeWorkValidation(work.getValidacoes(), workResult);
        assertTrue(validationResult.approved);
        assertEquals(validationResult.failures.size(), 0);
        assertEquals(validationResult.variables.size(), 0);
    }

    @Test
    public void testWorkExecution_curlIncompleteRequestCommand() {
        assertNotNull(System.getenv("BASH_PATH"));

        log.info("Testando comando cURL com {}.", Work.class.getSimpleName());
        var curl = """
        curl --location 'https://autenticaint.vivo.com.br/ms_oauth/oauth2/endpoints/vivooauthservice/tokens' 
        --header  
        --header 'Content-Type: application/x-www-form-urlencoded;charset=UTF-8' 
        --data 'grant_type=password&scope=ServiceAccount.Profile&username=svc-cyber3&password=,qU#-yVp0cm*'
        """.replace("\n" ," ");

        var work = Work.ssh(
            curl,
            JSON,
            JSON_PATH.value("$.access_token")
        );
        log.info(work.toString());

        var workResult = executeWork(work);
        assertNotNull(workResult);
        assertFalse(workResult.fatalError);
        assertEquals(0, workResult.code);
        assertNotNull(workResult.messages);
        assertNotEquals(workResult.messages.size(), 0);

        var validationResult = executeWorkValidation(work.getValidacoes(), workResult);
        assertFalse(validationResult.approved);
        assertEquals(validationResult.failures.size(), 1);
        assertEquals(validationResult.variables.size(), 0);
    }

    @Test
    public void testWorkExecution_curlInvalidRequestCommand() {
        assertNotNull(System.getenv("BASH_PATH"));

        log.info("Testando comando cURL com {}.", Work.class.getSimpleName());
        var curl = """
        curl --location 'https://autenticaint.vivo.com.br/ms_oauth/oauth2/endpoints/vivooauthservice/tokens
        """;

        var work = Work.ssh(
            curl,
            JSON_PATH.value("$.access_token")
        );
        log.info(work.toString());

        var workResult = executeWork(work);
        assertNotNull(workResult);
        assertFalse(workResult.fatalError);
        assertEquals(0, workResult.code);
        assertNotNull(workResult.messages);
        assertNotEquals(workResult.messages.size(), 0);

        var validationResult = executeWorkValidation(work.getValidacoes(), workResult);
        assertFalse(validationResult.approved);
        assertEquals(validationResult.failures.size(), 1);
        assertEquals(validationResult.variables.size(), 0);
    }

    @Test
    public void testWorkExecution_curlRequestCommand() {
        assertNotNull(System.getenv("BASH_PATH"));

        log.info("Testando comando cURL com {}.", Work.class.getSimpleName());
        var curl = """
        curl --location 'https://autenticaint.vivo.com.br/ms_oauth/oauth2/endpoints/vivooauthservice/tokens' 
        --header 'Authorization: Basic ZmRmMmNlYTZhMzdlNGM1OGIwMDAwMWY5NzVmNDVjMzU6MTgxNDNlNDZmNzc2NDZiOA==' 
        --header 'Content-Type: application/x-www-form-urlencoded;charset=UTF-8' 
        --data 'grant_type=password&scope=ServiceAccount.Profile&username=svc-cyber3&password=,qU#-yVp0cm*'
        """.replace("\n" ," ");

        var work = Work.ssh(
            curl,
            JSON_PATH.value("$.access_token")
        );
        log.info(work.toString());

        var workResult = executeWork(work);
        assertNotNull(workResult);
        assertFalse(workResult.fatalError);
        assertEquals(0, workResult.code);
        assertNotNull(workResult.messages);
        assertNotEquals(workResult.messages.size(), 0);

        var validationResult = executeWorkValidation(work.getValidacoes(), workResult);
        assertTrue(validationResult.approved);
        assertEquals(validationResult.failures.size(), 0);
        assertEquals(validationResult.variables.size(), 0);
    }



//    public WorkExecutionResult executeRequest(Work request) {
//        var curlCommand = request.getCommand();
//        if(request.getProtocol() != REQUEST || !curlCommand.startsWith("curl ")) {
//            return new WorkExecutionResult(false, "Invalid curl command");
//        }
//        try {
//            // Extrair método (padrão: -X POST ou --request POST)
//            var method = "GET";
//            var methodPattern = Pattern.compile("-X\\s+(\\w+)|--request\\s+(\\w+)");
//            var methodMatcher = methodPattern.matcher(curlCommand);
//            if(methodMatcher.find()) {
//                method = methodMatcher.group(1) != null ? methodMatcher.group(1) : methodMatcher.group(2);
//            }
//            // Extrair URL (última parte não iniciada por -)
//            var urlPattern = Pattern.compile("(?<!-)\\s*https?://[^\\s]+");
//            var urlMatcher = urlPattern.matcher(curlCommand);
//            if(!urlMatcher.find()) {
//                return new WorkExecutionResult(false, "URL not found in curl command");
//            }
//            var url = urlMatcher.group().trim();
//
//            // Extrair headers (-H ou --header)
//            var headers = new HttpHeaders();
//            var headerPattern = Pattern.compile("-H\\s+\"([^\"]+)\"|--header\\s+\"([^\"]+)\"");
//            var headerMatcher = headerPattern.matcher(curlCommand);
//            while(headerMatcher.find()) {
//                var header = headerMatcher.group(1) != null ? headerMatcher.group(1) : headerMatcher.group(2);
//                var keyValue = header.split(":\\s*", 2);
//                if(keyValue.length == 2) {
//                    headers.add(keyValue[0], keyValue[1]);
//                }
//            }
//            // Extrair body (-d ou --data)
//            String body = null;
//            var dataPattern = Pattern.compile("-d\\s+(['\"])(.*?)\\1|--data\\s+(['\"])(.*?)\\3");
//            var dataMatcher = dataPattern.matcher(curlCommand);
//            if(dataMatcher.find()) {
//                body = dataMatcher.group(2) != null ? dataMatcher.group(2) : dataMatcher.group(4);
//            }
//            // Configurar entidade HTTP
//            HttpEntity<?> entity;
//            if(headers.getContentType() != null && headers.getContentType().includes(MediaType.APPLICATION_FORM_URLENCODED)) {
//                var formData = new LinkedMultiValueMap<String, String>();
//                if(body != null) {
//                    for(var pair : body.split("&")) {
//                        var keyValue = pair.split("=", 2);
//                        formData.add(keyValue[0], keyValue.length > 1 ? keyValue[1] : "");
//                    }
//                }
//                entity = new HttpEntity<>(formData, headers);
//            } else {
//                entity = new HttpEntity<>(body, headers);
//            }
//            // Executar requisição
//            ResponseEntity<String> response = restTemplate.exchange(
//                url,
//                HttpMethod.valueOf(method),
//                entity,
//                String.class
//            );
//            var result = Optional.ofNullable(response.getBody())
//                .orElseGet(() -> "Requisição feita com sucesso, mas nenhum conteúdo retornado na resposta.");
//
//            return new WorkExecutionResult(true, result);
//        }
//        catch(Exception e) {
//            return new WorkExecutionResult(false, e.getMessage());
//        }
//    }

}
