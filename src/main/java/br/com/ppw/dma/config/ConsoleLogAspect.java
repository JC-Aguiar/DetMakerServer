package br.com.ppw.dma.config;

import ch.qos.logback.core.CoreConstants;
import jakarta.validation.constraints.NotBlank;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static br.com.ppw.dma.util.FormatDate.FORMAL_STYLE;
import static br.com.ppw.dma.util.FormatDate.RELOGIO;

@Aspect
@Component
public class ConsoleLogAspect {

//    MethodSignature signature;
//    String className;
//    Method methodName;
//    List<String> parametersType;
//    List<String> paramitersValues;

//    @Pointcut("(execution(* br.com.ppw.dma..*(..))) && !within(is(FinalType))")
    @Pointcut("within(br.com.ppw.dma..*)")
    public void log() {    }

    @Before("log()")
    public void identifyBefore(JoinPoint joinPoint) {
        val stringBuilder = new StringBuilder();

        var signature = (MethodSignature) joinPoint.getSignature();
        var className = getMethodSimpleName(signature);
        var methodName = signature.getMethod().getName();
        String[] parameterNames = signature.getParameterNames();
        Object[] parameterValues = joinPoint.getArgs();

//        var log = LogManager.getLogger(className + "." + methodName);

        stringBuilder
            .append(CoreConstants.LINE_SEPARATOR)
            .append(LocalDateTime.now(RELOGIO).format(FORMAL_STYLE))
            .append(" -- ")
            .append(Thread.currentThread().getName())
            .append(" -- ")
            .append(className + "." + methodName)
            .append(CoreConstants.LINE_SEPARATOR)
            .append("INICIOU")
            .append(parameterNames.length);

        for (int i = 0; i < parameterNames.length; i++) {
            String parameterName = parameterNames[i];
            Class<?> parameterType = signature.getParameterTypes()[i];
            Object parameterValue = parameterValues[i];
            parameterValue = printObject(parameterValue);

            //TODO: implementar anotação para se usar nos parâmetros, não que aqui não exiba senhas, cpfs e etcs
            stringBuilder
                .append(CoreConstants.LINE_SEPARATOR)
                .append("  ")
                .append(i+1)
                .append(". ")
                .append(parameterName)
                .append(" ")
                .append(parameterType.getSimpleName())
                .append(" = ")
                .append(parameterValue);
        }
        System.out.println();
//        log.info(stringBuilder.toString());
    }

    @AfterReturning(pointcut = "log()", returning = "result")
    public void identifyAfter(JoinPoint joinPoint, Object result) {
        val stringBuilder = new StringBuilder();

        var signature = (MethodSignature) joinPoint.getSignature();
        var className = getMethodSimpleName(signature);
        var method = signature.getMethod();

        var log = LogManager.getLogger(className + "." + method.getName());

        stringBuilder.append("Concluído.");
        if (method.getReturnType() != void.class)
            stringBuilder.append(" Objeto retornado: ").append(printObject(result));

        log.info(stringBuilder.toString());
    }

    private static String printObject(Object object) {
        return switch (object) {
            case null -> "null";
            case Object[] array -> Arrays.toString(array);
            case Collection<?> collection -> collection.toString();
            case String string -> string;
            default -> String.valueOf(object);
        };
    }

    //SERVICE-LOG: @Service
//    @Around("@within(org.springframework.stereotype.Service)")
//    public Object serviceAspect(ProceedingJoinPoint joinPoint) throws Throwable {
//        final Instant startTime = Instant.now();
//        try {
//            final Object serviceAction = joinPoint.proceed();
//            servicesResult.add(MasterServiceResult.success(
//                className, methodName.getName(), startTime, serviceAction));
//            return serviceAction;
//        } catch (Exception e) {
//            e.printStackTrace();
//            servicesResult.add(MasterServiceResult.error(
//                className, methodName.getName(), startTime, e));
//            final String returnedType = ((MethodSignature) joinPoint.getSignature())
//                    .getReturnType()
//                    .getSimpleName();
//            return compareClassNameAndGetEmptyInstance(returnedType);
//        }
//    }

    //CONTROLLER-LOG: @Controller
//    @Around("@within(org.springframework.stereotype.Controller)")
//    public Object controllerAspect(ProceedingJoinPoint joinPoint) throws Throwable {
//        return restControllerAspect(joinPoint);
//    }

    //CONTROLLER-LOG: @RestController
//    @Around("@within(org.springframework.web.bind.annotation.RestController)")
//    public Object restControllerAspect(ProceedingJoinPoint joinPoint) throws Throwable {
//        final Instant startTime = Instant.now();
//        final String controllerName = className + "." + methodName.getName();
//        final String controllerProcessName = processName;
//        HttpStatus status = HttpStatus.OK;
//        MasterControllerResult controllerResult;
//        Object controllerAction = "";
//        String resultLog;
//        try {
//            controllerAction = joinPoint.proceed();
//            resultLog = MasterControllerResult.buildControllerLog(
//                controllerName,
//                servicesResult,
//                startTime,
//                null);
//            System.out.println(resultLog);
//            controllerResult = new MasterControllerResult(controllerAction, resultLog);
//            servicesResult.clear();
//        } catch (Exception e) {
//            resultLog = MasterControllerResult.buildControllerLog(
//                controllerName,
//                servicesResult,
//                startTime,
//                e);
//            System.out.println(resultLog);
//            e.printStackTrace();
//            status = HttpStatus.INTERNAL_SERVER_ERROR;
//            controllerResult = new MasterControllerResult(controllerAction, resultLog);
//            servicesResult.clear();
//        }
//        return new ResponseEntity<>(controllerResult, status);
//    }

    private Object compareClassNameAndGetEmptyInstance(@NotBlank String className) {
        final String lowerClassName = className.toLowerCase(Locale.ROOT);
        if (lowerClassName.contains("optional")) return Optional.empty();
        if (lowerClassName.contains("page")) return Page.empty();
        if (lowerClassName.contains("list")) return List.of();
        if (lowerClassName.contains("map")) return Map.of();
        if (lowerClassName.contains("set")) return Set.of();
        return Optional.empty();
    }

//    private void fullProcessMessage(@NotNull MasterProcessManager resultProcess) {
//        CONTROLLER.info(
//            FULL_LOG_MESSAGE,
//            resultProcess.getStatus(),
//            masterServiceLogs.stream()
//                             .map(MasterServiceResult::getLog)
//                             .collect(Collectors.joining("\n\t"))
//            );
//    }

    private static String getMethodSimpleName(MethodSignature signature)  {
        final String[] className = signature.getDeclaringType().toString().split("\\.");
        return className[(className.length - 1)];
    }

}
