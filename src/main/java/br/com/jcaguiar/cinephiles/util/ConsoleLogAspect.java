package br.com.jcaguiar.cinephiles.util;

import br.com.jcaguiar.cinephiles.master.MasterControllerResult;
import br.com.jcaguiar.cinephiles.master.MasterServiceResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Aspect
@Component
public class ConsoleLogAspect {

    MethodSignature signature;
    String className;
    Method methodName;
    String processName;
    List<String> paramitersType;
    List<String> paramitersValues;
    final private List<MasterServiceResult> servicesResult = new ArrayList<>();
    final public static Logger LOGGER = LogManager.getLogger("CONSOLE LOG");
    final public static Logger CONTROLLER = LogManager.getLogger("CONTROLLER LOG");
    final public static Logger SERVICER = LogManager.getLogger("SERVICE LOG");
    final private static String LOG_FORMAT = "%s::%s(%s)";

    @Pointcut("(execution(* br.com.jcaguiar..*(..))) && !within(is(FinalType))")
    public void log() {    }

    @Before("log()")
    public void identify(JoinPoint joinPoint) {
        signature = (MethodSignature) joinPoint.getSignature();
        className = getMethodSimpleName(signature);
        methodName = signature.getMethod();
        processName = className + methodName.getName() + LocalDateTime.now().toString();
        paramitersType = Arrays
            .stream(methodName.getParameterTypes())
            .map(type -> type != null ? type.getSimpleName() : "NULL")
            .toList();
        paramitersValues = Arrays
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
        LOGGER.info(String.format(LOG_FORMAT, className, methodName.getName(), parameters));
    }

    //SERVICE-LOG: @Service
    @Around("@within(org.springframework.stereotype.Service)")
    public Object serviceAspect(ProceedingJoinPoint joinPoint) throws Throwable {
        final Instant startTime = Instant.now();
        try {
            final Object serviceAction = joinPoint.proceed();
            servicesResult.add(MasterServiceResult.success(
                className, methodName.getName(), startTime, serviceAction));
            return serviceAction;
        } catch (Exception e) {
            e.printStackTrace();
            servicesResult.add(MasterServiceResult.error(
                className, methodName.getName(), startTime, e));
            final String returnedType = ((MethodSignature) joinPoint.getSignature())
                    .getReturnType()
                    .getSimpleName();
            return compareClassNameAndGetEmptyInstance(returnedType);
        }
    }

    //CONTROLLER-LOG: @Controller
    @Around("@within(org.springframework.stereotype.Controller)")
    public Object controllerAspect(ProceedingJoinPoint joinPoint) throws Throwable {
        return restControllerAspect(joinPoint);
    }

    //CONTROLLER-LOG: @RestController
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object restControllerAspect(ProceedingJoinPoint joinPoint) throws Throwable {
        final Instant startTime = Instant.now();
        final String controllerName = className + "." + methodName.getName();
        final String controllerProcessName = processName;
        HttpStatus status = HttpStatus.OK;
        MasterControllerResult controllerResult;
        Object controllerAction = "";
        String resultLog;
        try {
            controllerAction = joinPoint.proceed();
            resultLog = MasterControllerResult.buildControllerLog(
                controllerName,
                servicesResult,
                startTime,
                null);
            System.out.println(resultLog);
            controllerResult = new MasterControllerResult(controllerAction, resultLog);
            servicesResult.clear();
        } catch (Exception e) {
            resultLog = MasterControllerResult.buildControllerLog(
                controllerName,
                servicesResult,
                startTime,
                e);
            System.out.println(resultLog);
            e.printStackTrace();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            controllerResult = new MasterControllerResult(controllerAction, resultLog);
            servicesResult.clear();
        }
        return new ResponseEntity<>(controllerResult, status);
    }

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

//} catch (NoSuchElementException e) {
//    e.printStackTrace();
//final Class returnType = ((MethodSignature) joinPoint.getSignature()).getReturnType();
//final String name = returnType.getSimpleName();
//    LOGGER.info("returnType: " + returnType);
//    LOGGER.info("name: " + name);
//    return compareClassNameAndGetEmptyInstance(name);
//    } finally {
//    masterServiceLogs.clear();
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
