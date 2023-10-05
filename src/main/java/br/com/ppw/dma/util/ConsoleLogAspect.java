package br.com.ppw.dma.util;

import jakarta.validation.constraints.NotBlank;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

@Aspect
@Component
public class ConsoleLogAspect {

    MethodSignature signature;
    String className;
    Method methodName;
    List<String> parametersType;
    List<String> paramitersValues;
    final private List<MasterServiceResult> servicesResult = new ArrayList<>();
    final public static Logger LOGGER = LogManager.getLogger("CONSOLE LOG");
    final public static Logger CONTROLLER = LogManager.getLogger("CONTROLLER LOG");
    final public static Logger SERVICER = LogManager.getLogger("SERVICE LOG");
    final private static String LOG_FORMAT = "%s::%s(%s)";

    @Pointcut("(execution(* br.com.ppw.dma..*(..))) && !within(is(FinalType))")
    public void log() {    }

    @Before("log()")
    public void identify(JoinPoint joinPoint) {
        val parameters = new StringBuilder();

        //Getting method name
        signature = (MethodSignature) joinPoint.getSignature();
        className = getMethodSimpleName(signature);
        methodName = signature.getMethod();


        //Getting all parameters types
        parametersType = Arrays
            .stream(methodName.getParameterTypes())
            .map(type -> type != null ? type.getSimpleName() : "NULL")
            .toList();

        //Getting all parameters names
        paramitersValues = Arrays
            .stream(joinPoint.getArgs())
            .map(obj -> obj != null ? obj.toString() : "NULL")
            .toList();

        //Getting all parameters values
        for(int i = 0; i < parametersType.size(); i++) {
            String args = "";
            if(i < parametersType.size()-1) { args = ", "; }
            parameters.append(parametersType.get(i))
                      .append(": ")
                      .append(paramitersValues.get(i))
                      .append(args);
        }
        LOGGER.info(String.format(LOG_FORMAT, className, methodName.getName(), parameters));
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
