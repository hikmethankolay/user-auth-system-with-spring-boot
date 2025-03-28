/**
 * @file AuthLogger.java
 * @brief Aspect class for logging method calls in the application.
 *
 * This class uses Spring AOP to log method calls before execution, 
 * after successful execution, and upon exceptions.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.aspect
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.aspect;

import com.hikmethankolay.user_auth_system.dto.LoginRequestDTO;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * @class AuthLogger
 * @brief Aspect for logging application flow.
 *
 * This aspect logs method invocations within controller, repository, and service packages.
 */
@Aspect
@Component
public class AuthLogger {

    /** Logger instance for logging method calls. */
    private final Logger myLogger = Logger.getLogger(getClass().getName());

    /**
     * @brief Pointcut for controller package.
     */
    @Pointcut("execution(* com.hikmethankolay.user_auth_system.controller.*.*(..))")
    public void forControllerPackage() {}

    /**
     * @brief Pointcut for repository package.
     */
    @Pointcut("execution(* com.hikmethankolay.user_auth_system.repository.*.*(..))")
    public void forRepositoryPackage() {}

    /**
     * @brief Pointcut for service package.
     */
    @Pointcut("execution(* com.hikmethankolay.user_auth_system.service.*.*(..))")
    public void forServicePackage() {}

    /**
     * @brief Combined pointcut for application flow.
     */
    @Pointcut("forControllerPackage() || forRepositoryPackage() || forServicePackage()")
    public void forAppFlow() {}

    /**
     * @brief Pointcut definition for authentication controller methods.
     */
    @Pointcut("execution(* com.hikmethankolay.user_auth_system.controller.AuthController.*(..))")
    public void authMethods() {}

    /**
     * @brief Logs method call before execution.
     * @param theJoinPoint The join point representing the method execution.
     */
    @Before("forAppFlow()")
    public void beforeAppFlow(JoinPoint theJoinPoint) {
        String theMethod = theJoinPoint.getSignature().toShortString();
        myLogger.info("=====> @Before: calling method: " + theMethod);

        Object[] args = theJoinPoint.getArgs();
        for (Object arg : args) {
            myLogger.info("=====> Argument: " + arg);
        }
    }

    /**
     * @brief Logs method call after successful execution.
     * @param theJoinPoint The join point representing the method execution.
     * @param result The result returned by the method.
     */
    @AfterReturning(pointcut = "forAppFlow()", returning = "result")
    public void afterAppFlow(JoinPoint theJoinPoint, Object result) {
        String theMethod = theJoinPoint.getSignature().toShortString();
        myLogger.info("=====> @AfterReturning: calling method: " + theMethod);
        myLogger.info("=====> Result: " + result);
    }

    /**
     * @brief Logs method call when an exception occurs.
     * @param theJoinPoint The join point representing the method execution.
     * @param exception The exception thrown by the method.
     */
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

    /**
     * @brief Logs authentication attempts after method execution.
     *
     * This advice captures login attempts and their results, logging whether the
     * authentication was successful and which user attempted to authenticate.
     *
     * @param joinPoint The join point representing the intercepted method.
     * @param loginRequest The login request containing user credentials.
     * @param result The result of the authentication attempt.
     */
    @AfterReturning(pointcut = "authMethods() && args(loginRequest,..)", returning = "result")
    public void logLoginAttempt(JoinPoint joinPoint, LoginRequestDTO loginRequest, Object result) {
        String method = joinPoint.getSignature().toShortString();
        boolean success = result != null && !(result instanceof ResponseEntity) ||
                result instanceof ResponseEntity && ((ResponseEntity<?>) result).getStatusCode().is2xxSuccessful();

        myLogger.info("Authentication attempt: method=" + method +
                ", username=" + loginRequest.identifier() +
                ", success=" + success);
    }
}