package com.vht.ems.aop.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vht.ems.domain.AuditLog;
import com.vht.ems.repository.AuditLogRepository;
import com.vht.ems.security.SecurityUtils;
import com.vht.ems.service.dto.AlertRuleDTO;
import com.vht.ems.service.dto.CredentialDTO;
import com.vht.ems.service.dto.DeviceDTO;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect that records business audit logs for CRUD operations
 * on Device, AlertRule, and Credential resources.
 * The detail field never contains secrets (encryptedSecret, passwords, SSH keys).
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger LOG = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditAspect(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @AfterReturning(
        pointcut = "execution(* com.vht.ems.web.rest.DeviceResource.createDevice(..)) || " +
            "execution(* com.vht.ems.web.rest.DeviceResource.updateDevice(..)) || " +
            "execution(* com.vht.ems.web.rest.DeviceResource.partialUpdateDevice(..)) || " +
            "execution(* com.vht.ems.web.rest.DeviceResource.deleteDevice(..)) || " +
            "execution(* com.vht.ems.web.rest.AlertRuleResource.createAlertRule(..)) || " +
            "execution(* com.vht.ems.web.rest.AlertRuleResource.updateAlertRule(..)) || " +
            "execution(* com.vht.ems.web.rest.AlertRuleResource.partialUpdateAlertRule(..)) || " +
            "execution(* com.vht.ems.web.rest.AlertRuleResource.deleteAlertRule(..)) || " +
            "execution(* com.vht.ems.web.rest.CredentialResource.createCredential(..)) || " +
            "execution(* com.vht.ems.web.rest.CredentialResource.updateCredential(..)) || " +
            "execution(* com.vht.ems.web.rest.CredentialResource.partialUpdateCredential(..)) || " +
            "execution(* com.vht.ems.web.rest.CredentialResource.deleteCredential(..))",
        returning = "result"
    )
    public void logAction(JoinPoint joinPoint, Object result) {
        try {
            String username = SecurityUtils.getCurrentUserLogin().orElse("system");
            String methodName = joinPoint.getSignature().getName();
            String action = resolveAction(methodName);
            String entityName = resolveEntityName(joinPoint.getSignature().getDeclaringTypeName());
            String entityId = extractEntityId(result, joinPoint);
            String detail = buildDetail(joinPoint, result, entityName);

            AuditLog log = new AuditLog();
            log.setUsername(username);
            log.setAction(action);
            log.setEntityName(entityName);
            log.setEntityId(entityId);
            log.setDetail(detail);
            log.setTimestamp(Instant.now());

            auditLogRepository.save(log);
        } catch (Exception e) {
            LOG.warn("Failed to write audit log for {}: {}", joinPoint.getSignature().getName(), e.getMessage());
        }
    }

    private String resolveAction(String methodName) {
        if (methodName.startsWith("create")) return "CREATE";
        if (methodName.startsWith("update") || methodName.startsWith("partialUpdate")) return "UPDATE";
        if (methodName.startsWith("delete")) return "DELETE";
        return methodName.toUpperCase();
    }

    private String resolveEntityName(String declaringType) {
        if (declaringType.contains("DeviceResource")) return "Device";
        if (declaringType.contains("AlertRuleResource")) return "AlertRule";
        if (declaringType.contains("CredentialResource")) return "Credential";
        return "Unknown";
    }

    private String extractEntityId(Object result, JoinPoint joinPoint) {
        if (result instanceof ResponseEntity<?> re) {
            Object body = re.getBody();
            if (body instanceof DeviceDTO dto) return dto.getId();
            if (body instanceof AlertRuleDTO dto) return dto.getId();
            if (body instanceof CredentialDTO dto) return dto.getId();
        }
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0 && args[0] instanceof String id) {
            return id;
        }
        return null;
    }

    private String buildDetail(JoinPoint joinPoint, Object result, String entityName) {
        try {
            Map<String, Object> detail = new HashMap<>();
            Object[] args = joinPoint.getArgs();
            if (result instanceof ResponseEntity<?> re && re.getBody() != null) {
                Object body = re.getBody();
                detail.put("result", sanitize(body, entityName));
            } else if (args != null && args.length > 0) {
                detail.put("arg", sanitize(args[0], entityName));
            }
            return objectMapper.writeValueAsString(detail);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Object sanitize(Object obj, String entityName) {
        if (obj == null) return null;
        if ("Credential".equals(entityName) && obj instanceof CredentialDTO dto) {
            Map<String, Object> safe = new HashMap<>();
            safe.put("id", dto.getId());
            safe.put("name", dto.getName());
            safe.put("authType", dto.getAuthType());
            safe.put("username", dto.getUsername());
            return safe;
        }
        return obj;
    }
}
