package com.hikmethankolay.user_auth_system.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Aspect
@Component
public class AuthAspect {

    private final Logger myLogger = Logger.getLogger(getClass().getName());

    @Pointcut("execution(* com.hikmethankolay.user_auth_system.controller.*.*(..))")
    public void forControllerPackage() {}

    @Pointcut("execution(* com.hikmethankolay.user_auth_system.repository.*.*(..))")
    public void forRepositoryPackage() {}

    @Pointcut("execution(* com.hikmethankolay.user_auth_system.service.*.*(..))")
    public void forServicePackage() {}

    @Pointcut("forControllerPackage() || forRepositoryPackage() || forServicePackage()")
    public void forAppFlow() {}

    @Before("forAppFlow()")
    public void beforeAppFlow(JoinPoint theJoinPoint) {

        String theMethod = theJoinPoint.getSignature().toShortString();
        myLogger.info("=====> @Before: calling method: " + theMethod);

        Object[] args = theJoinPoint.getArgs();

        for (Object arg : args) {
            myLogger.info("=====> Argument: " + arg);
        }

    }

    @AfterReturning(pointcut = "forAppFlow()",
            returning = "result")
    public void afterAppFlow(JoinPoint theJoinPoint, Object result) {

        String theMethod = theJoinPoint.getSignature().toShortString();
        myLogger.info("=====> @AfterReturning: calling method: " + theMethod);

        Object[] args = theJoinPoint.getArgs();

        myLogger.info("=====> Result: " + result);
    }

    @AfterThrowing(pointcut = "forAppFlow()", throwing = "exception")
    public void afterError(JoinPoint theJoinPoint, Throwable exception) {

        String theMethod = theJoinPoint.getSignature().toShortString();
        myLogger.severe("=====> @AfterThrowing: Exception in method: " + theMethod);

        myLogger.severe("=====> Exception: " + exception.getClass().getName() + " - " + exception.getMessage());

        Object[] args = theJoinPoint.getArgs();
        for (Object arg : args) {
            myLogger.severe("=====> Argument: " + arg);
        }
    }

}
