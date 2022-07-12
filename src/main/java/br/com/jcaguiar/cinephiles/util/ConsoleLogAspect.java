package br.com.jcaguiar.cinephiles.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.*;

@Aspect
@Component
public class ConsoleLogAspect {

    final public static Logger LOGGER = LogManager.getLogger("CONSOLE LOG");
    final private static String MESSAGE = "%s::%s(%s)";


    @Pointcut("within(br.com.jcaguiar.cinephiles..*)")
    public void log() {
    }

    @Pointcut("within(br.com.jcaguiar.cinephiles.security..*)")
    public void webFilter() {
    }

    @Before("log()")
    public void identify(JoinPoint joinPoint) {
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
        //TODO:formatar string
        LOGGER.info(String.format(
            MESSAGE,
            classe, method.getName(), parameters.toString()));
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



    @AfterThrowing(value = "@annotation(br.com.jcaguiar.cinephiles.util.ConsoleLog)", throwing = "exception")
    public void printConsoleLogException(JoinPoint joinPoint, Exception exception) {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final String className = getMethodSimpleName(signature);
        System.out.printf("[CONSOLE-LOG] %s::%s! --> %s \n",
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
