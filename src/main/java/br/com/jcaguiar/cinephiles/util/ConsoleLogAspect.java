package br.com.jcaguiar.cinephiles.util;

import br.com.jcaguiar.cinephiles.master.MasterProcessPage;
import br.com.jcaguiar.cinephiles.master.ProcessLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Aspect
@Component
public class ConsoleLogAspect {

    final public static Logger LOGGER = LogManager.getLogger("CONSOLE LOG");
    final private static String MESSAGE = "%s::%s(%s)";
    final private List<ProcessLine<?>> processLines = new ArrayList<>();
    final private Map<String, ProcessLine> processMap = new HashMap<>();

    @Pointcut("within(br.com.jcaguiar.cinephiles..*)")
    public void log() {
    }

    @Pointcut("within(br.com.jcaguiar.cinephiles.security..*)")
    public void webFilter() {
    }

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
        LOGGER.info(String.format(
            MESSAGE,
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

    @Around("@annotation(ServiceProcess)")
    public Object processLine(ProceedingJoinPoint joinPoint) throws Throwable {
        final Instant startTime = Instant.now();

        final var signature = (MethodSignature) joinPoint.getSignature();
        final var classe = getMethodSimpleName(signature);
        final var method = signature.getMethod();
//      processMap.put(method.getName(), ProcessLine.success(startTime, action));

        ProcessLine<?> process = null;
        try {
            final Object action = joinPoint.proceed();
            process = ProcessLine.success(startTime, action);
            processLines.add(process);
            return action;
        } catch (Exception e) { //TODO: exception already been handled in the class... change that?
            process = ProcessLine.error(startTime, e);
            processLines.add(process);
            return Optional.empty();
        }
        finally {
            LOGGER.info(method.getName() + " - " + process.getLog());
        }
    }

    /**
     * CONTROLLER PROCESS
     * Todos Controllers retornam uma ResponseEntity
     * Todos os Services retornam um Objeto Entidade ou DTO
     * @param joinPoint
     * @return
     * @throws Throwable
     */

    @Around("@annotation(ControllerProcess)")
    public Object controllerService(ProceedingJoinPoint joinPoint) throws Throwable {
//        final var args = joinPoint.getArgs();
//        final var algo = ((MethodSignature) joinPoint.getSignature()).getMethod().getReturnType();
//        final List<ProcessLine> action = (List<ProcessLine>) joinPoint.proceed();
//        return new MasterProcessPage<List<ProcessLine>>(action, pageConfig);
        final Object action = joinPoint.proceed();
        final Pageable pageConfig = PageRequest.of(0, 12, Sort.by("id").ascending());
        final MasterProcessPage<List<?>> resultProcess = new MasterProcessPage(processLines,pageConfig);
        LOGGER.info("Process Result: {} - Full Process:\n\t\t\t\t\t\t{}",
            resultProcess.getStatus(),
            processLines.stream().map(ProcessLine::getLog).collect(Collectors.joining("\n\t\t\t\t\t\t"))
        );
        processLines.clear();
        return action;
    }



    @AfterThrowing(value = "@annotation(br.com.jcaguiar.cinephiles.util.ConsoleLog)", throwing = "exception")
    public void printConsoleLogException(JoinPoint joinPoint, Exception exception) {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final String className = getMethodSimpleName(signature);
        LOGGER.error(
            "{}::{} --> {}",
            className,
            exception.getClass().getSimpleName(),
            exception.getMessage());
    }

    private static String getMethodSimpleName(MethodSignature signature)
    {
        final String[] className = signature.getDeclaringType().toString().split("\\.");
        return className[(className.length - 1)];
    }

}
