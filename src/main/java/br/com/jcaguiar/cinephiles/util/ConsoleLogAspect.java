package br.com.jcaguiar.cinephiles.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class ConsoleLogAspect {

    @Before(value = "@annotation(br.com.jcaguiar.cinephiles.util.ConsoleLog)")
    public void printConsoleLog(JoinPoint joinPoint)
    {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final String[] className = signature.getDeclaringType().toString().split("\\.");
        final String simpleClassName = className[(className.length - 1)];
        final String methodName = signature.getMethod().getName();
        final String methodArgTypes = Arrays.stream(signature.getParameterTypes())
            .map(Class::toString).collect(Collectors.joining(", "));
        final String methodArgValues= Arrays.stream(joinPoint.getArgs())
            .map(Object::toString).collect(Collectors.joining(", "));
        System.out.printf("%s: %s(%s) = %s", simpleClassName, methodName, methodArgTypes,methodArgValues);
    }

}
