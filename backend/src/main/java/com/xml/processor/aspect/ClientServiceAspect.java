package com.xml.processor.aspect;

import com.xml.processor.model.AuditLog;
import com.xml.processor.model.Client;
import com.xml.processor.repository.AuditLogRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ClientServiceAspect {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @AfterReturning(
        pointcut = "execution(* com.xml.processor.service.impl.ClientServiceImpl.saveClient(..))",
        returning = "result")
    public void logClientSave(JoinPoint joinPoint, Client result) {
        Client client = (Client) joinPoint.getArgs()[0];
        String action = client.getId() == null ? "CREATE" : "UPDATE";
        
        AuditLog log = new AuditLog();
        log.setUsername(getCurrentUsername());
        log.setAction(action);
        log.setEntityType("CLIENT");
        log.setEntityId(result.getId());
        log.setDetails("Client " + action.toLowerCase() + "d: " + result.getName());
        
        auditLogRepository.save(log);
    }
    
    @AfterReturning(
        pointcut = "execution(* com.xml.processor.service.impl.ClientServiceImpl.deleteClient(..))")
    public void logClientDelete(JoinPoint joinPoint) {
        Long clientId = (Long) joinPoint.getArgs()[0];
        
        AuditLog log = new AuditLog();
        log.setUsername(getCurrentUsername());
        log.setAction("DELETE");
        log.setEntityType("CLIENT");
        log.setEntityId(clientId);
        log.setDetails("Client deleted with ID: " + clientId);
        
        auditLogRepository.save(log);
    }
    
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
} 