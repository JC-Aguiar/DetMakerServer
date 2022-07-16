package br.com.jcaguiar.cinephiles.util;

import br.com.jcaguiar.cinephiles.master.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Aspect
@Component
public class ConsoleLogAspect {

    final public static Logger LOGGER = LogManager.getLogger("CONSOLE LOG");
    final public static Logger CONTROLLER = LogManager.getLogger("CONTROLLER LOG");
    final public static Logger SERVICER = LogManager.getLogger("SERVICE LOG");
    final private static String LOG_MESSAGE = "%s::%s(%s)";
    final private static String FULL_LOG_MESSAGE =
        """
        Process Result: {}
        Full Process: {
            {}
        }
        """;
    final private List<ProcessLine> processLines = new ArrayList<>();
//    final private Map<String, ProcessLine> processMap = new HashMap<>();

    @Pointcut("within(br.com.jcaguiar.cinephiles..*)")
    public void log() {
    }

    @Pointcut("within(br.com.jcaguiar.cinephiles.security..*)")
    public void webFilter() {
    }

//    @Pointcut("@within(org.springframework.stereotype.Service)")
//    public void service() {
//
//    }

    @Before("log()")
    public void identify(JoinPoint joinPoint) {
        final Instant startTime = Instant.now();
        final var signature = (MethodSignature) joinPoint.getSignature();
        final var classe = getMethodSimpleName(signature);
        final var method = signature.getMethod();
        final var paramitersType = Arrays
            .stream(method.getParameterTypes())
            .map(type -> type != null ? type.getSimpleName() : "NULL")
            .toList();
        final var paramitersValues = Arrays
            .stream(joinPoint.getArgs())
            .map(obj -> obj != null ? obj.toString() : "NULL")
            .toList();
        StringBuilder parameters = new StringBuilder();
        for(int i = 0; i < paramitersType.size(); i++) {
            String args = "";
            if(i < paramitersType.size()-1) { args = ", "; }
            parameters.append(paramitersType.get(i))
                      .append(": ")
                      .append(paramitersValues.get(i))
                      .append(args);
        }
        LOGGER.info(String.format(LOG_MESSAGE,
            classe, method.getName(), parameters.toString())
        );
    }

    @Before("webFilter()")
    public void security(JoinPoint joinPoint) {
        identify(joinPoint);
    }

//    @Around("@annotation(ConsoleLog)")
//    public Object printConsoleLogBefore(ProceedingJoinPoint joinPoint) throws Throwable {
//        final long startTime = System.currentTimeMillis();
//        final Object procced = joinPoint.proceed();
//        final long endTime = startTime - System.currentTimeMillis();
//        System.out.println(String.format(
//            "%s processed in %d ms", joinPoint.getSignature(), endTime));
//        return procced;
//    }

    //SERVICE ASPECT
    @Around("@within(org.springframework.stereotype.Service)")
    public Object processLine(ProceedingJoinPoint joinPoint) throws Throwable {
        final Instant startTime = Instant.now();
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final Method method = signature.getMethod();
        ProcessLine<?> process = null;
        try {
            final Object action = joinPoint.proceed();
            process = ProcessLine.success(startTime, action);
            processLines.add(process);
            return action;
        } catch (Exception e) {
//            e.printStackTrace();
            SERVICER.error("SERVICE ERROR/EXCEPTION");
            process = ProcessLine.error(startTime, e);
            processLines.add(process);
            final String returnedType = ((MethodSignature) joinPoint.getSignature())
                .getReturnType()
                .getSimpleName();
            return compareClassNameAndGetEmptyInstance(returnedType);
        }
        finally {
            SERVICER.info(method.getName() + " - " + process.getLog());
        }
    }

    //WORKING
//    @AfterThrowing(pointcut = "@annotation(ServiceProcess)", throwing = "ex")
//    public void processLineException(Exception ex) throws Throwable {
//        final Instant startTime = Instant.now();
//        ProcessLine<?> process = null;
//        LOGGER.info("TESTE EXCEPTION!");
//        process = ProcessLine.error(startTime, ex);
//        LOGGER.info(process.getLog());
//    }

    //CONTROLLER ASPECT
    @Around("@within(org.springframework.stereotype.Controller)")
    public Object controllerService(ProceedingJoinPoint joinPoint) throws Throwable {
        return restControllerService(joinPoint);
    }

    //CONTROLLER ASPECT
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object restControllerService(ProceedingJoinPoint joinPoint) throws Throwable {
        final Instant startTime = Instant.now();
        final MasterProcessPage<List<?>> resultProcess = new MasterProcessPage<>();
        HttpStatus status = HttpStatus.OK;
        try {
            joinPoint.proceed();
            resultProcess.addProcess(processLines);
        } catch (Exception e) { //NoSuchElementException, DataIntegrityViolationException
            e.printStackTrace();
            resultProcess.addProcess(processLines);
            CONTROLLER.error("Exception occur in the Controller level! See the Stack-Trace or in the Log-Summary.");
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            resultProcess.addProcess(ProcessLine.error(startTime, e));
        }
        fullProcessMessage(resultProcess);
        processLines.clear();
        return new ResponseEntity<>(resultProcess, status);
    }

    private Object compareClassNameAndGetEmptyInstance(@NotBlank String text) {
        final String textL = text.toLowerCase(Locale.ROOT);
        if (textL.contains("optional")) return Optional.empty();
        if (textL.contains("page")) return Page.empty();
        if (textL.contains("list")) return List.of();
        if (textL.contains("map")) return Map.of();
        return Optional.empty();
    }

    private void fullProcessMessage(@NotNull MasterProcess resultProcess) {
        CONTROLLER.info(
            FULL_LOG_MESSAGE,
            resultProcess.getStatus(),
            processLines.stream()
                .map(ProcessLine::getLog)
                .collect(Collectors.joining("\n\t"))
            );
    }

    private static String getMethodSimpleName(MethodSignature signature)  {
        final String[] className = signature.getDeclaringType().toString().split("\\.");
        return className[(className.length - 1)];
    }

//} catch (NoSuchElementException e) {
//    e.printStackTrace();
//final Class returnType = ((MethodSignature) joinPoint.getSignature()).getReturnType();
//final String name = returnType.getSimpleName();
//    LOGGER.info("returnType: " + returnType);
//    LOGGER.info("name: " + name);
//    return compareClassNameAndGetEmptyInstance(name);
//    } finally {
//    processLines.clear();
//    }

//    @AfterThrowing(value = "@annotation(br.com.jcaguiar.cinephiles.util.ConsoleLog)", throwing = "exception")
//    public void printConsoleLogException(JoinPoint joinPoint, Exception exception) {
//        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//        final String className = getMethodSimpleName(signature);
//        LOGGER.error(
//            "{}::{} --> {}",
//            className,
//            exception.getClass().getSimpleName(),
//            exception.getMessage());
//    }

}
