package com.xml.processor.aspect;

import com.xml.processor.model.AuditLog;
import com.xml.processor.service.AuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Aspect
@Component
public class AuditLogAspect {

    @Autowired
    private AuditLogService auditLogService;

    @Around("@annotation(com.xml.processor.annotation.AuditLog)")
    public Object logAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            createAuditLog(joinPoint, result, exception, startTime);
        }
    }

    private void createAuditLog(ProceedingJoinPoint joinPoint, Object result, Exception exception, long startTime) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        AuditLog auditLog = new AuditLog();
        auditLog.setAction(signature.getName());
        auditLog.setUsername(authentication != null ? authentication.getName() : "anonymous");
        auditLog.setClientId(getClientIdFromRequest(request));
        auditLog.setDetails(getDetailsFromJoinPoint(joinPoint));
        auditLog.setIpAddress(request.getRemoteAddr());
        auditLog.setUserAgent(request.getHeader("User-Agent"));
        auditLog.setRequestMethod(request.getMethod());
        auditLog.setRequestUrl(request.getRequestURI());
        auditLog.setRequestParams(request.getQueryString());
        auditLog.setResponseStatus(exception != null ? 500 : 200);
        auditLog.setErrorMessage(exception != null ? exception.getMessage() : null);
        auditLog.setCreatedAt(LocalDateTime.now());
        auditLog.setExecutionTime(System.currentTimeMillis() - startTime);

        auditLogService.createAuditLog(auditLog);
    }

    private Long getClientIdFromRequest(HttpServletRequest request) {
        // Extract client ID from request attributes or headers
        // This is a placeholder implementation
        String clientIdHeader = request.getHeader("X-Client-ID");
        if (clientIdHeader != null) {
            try {
                return Long.parseLong(clientIdHeader);
            } catch (NumberFormatException e) {
                // Log error or handle invalid client ID
            }
        }
        return null;
    }

    private String getDetailsFromJoinPoint(ProceedingJoinPoint joinPoint) {
        // Extract relevant details from method parameters
        // This is a placeholder implementation
        StringBuilder details = new StringBuilder();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();

        for (int i = 0; i < args.length; i++) {
            if (i > 0) details.append(", ");
            details.append(paramNames[i]).append(": ").append(args[i]);
        }

        return details.toString();
    }
} 