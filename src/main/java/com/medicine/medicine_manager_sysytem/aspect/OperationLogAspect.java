package com.medicine.medicine_manager_sysytem.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.medicine_manager_sysytem.entity.OperationLog;
import com.medicine.medicine_manager_sysytem.entity.User;
import com.medicine.medicine_manager_sysytem.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(100)
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@within(org.springframework.web.bind.annotation.RestController) && !@within(com.medicine.medicine_manager_sysytem.controller.OperationLogController)")
    public Object recordOperationLog(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        OperationLog operationLog = new OperationLog();
        operationLog.setStatus(1); // 默认成功

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                operationLog.setModule(getModuleName(request.getRequestURI()));
                operationLog.setOperation(getOperationName(joinPoint));
                operationLog.setMethod(request.getMethod() + " " + request.getRequestURI());
                
                String ip = getClientIp(request);
                operationLog.setIpAddress(ip);
                
                log.info("📝 捕获操作: 用户={}, IP={}, 方法={}", 
                        SecurityContextHolder.getContext().getAuthentication().getName(), ip, operationLog.getMethod());

                try {
                    operationLog.setParams(objectMapper.writeValueAsString(joinPoint.getArgs()));
                } catch (Exception e) {
                    operationLog.setParams("参数序列化失败");
                }
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                operationLog.setUserId(user.getId());
                operationLog.setUsername(user.getUsername());
            }

            Object result = joinPoint.proceed();
            
            long timeTaken = System.currentTimeMillis() - startTime;
            operationLog.setTimeTaken(timeTaken);
            
            try {
                operationLog.setResult(objectMapper.writeValueAsString(result));
            } catch (Exception e) {
                operationLog.setResult("结果序列化失败");
            }

            return result;
        } catch (Throwable e) {
            operationLog.setStatus(0);
            operationLog.setErrorMsg(e.getMessage());
            operationLog.setTimeTaken(System.currentTimeMillis() - startTime);
            log.error("❌ 操作执行失败: {}", e.getMessage());
            throw e;
        } finally {
            operationLog.setCreateTime(LocalDateTime.now());
            try {
                operationLogMapper.insert(operationLog);
                log.debug("✅ 日志已保存: status={}, ip={}, time={}", 
                        operationLog.getStatus(), operationLog.getIpAddress(), operationLog.getTimeTaken());
            } catch (Exception e) {
                log.error("❌ 保存操作日志失败", e);
            }
        }
    }

    private String getModuleName(String uri) {
        if (uri.contains("/auth/")) return "用户认证";
        if (uri.contains("/users/")) return "用户管理";
        if (uri.contains("/roles/")) return "角色管理";
        if (uri.contains("/medicines/")) return "药品管理";
        if (uri.contains("/suppliers/")) return "供应商管理";
        if (uri.contains("/customers/")) return "客户管理";
        if (uri.contains("/purchase-orders/")) return "采购管理";
        if (uri.contains("/sales-orders/")) return "销售管理";
        if (uri.contains("/inventory/")) return "库存管理";
        if (uri.contains("/quality/")) return "质量管理";
        if (uri.contains("/finance/")) return "财务管理";
        return "其他模块";
    }

    private String getOperationName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String name = method.getName();
        
        if (name.startsWith("create") || name.startsWith("add")) return "新增";
        if (name.startsWith("update") || name.startsWith("edit")) return "修改";
        if (name.startsWith("delete") || name.startsWith("remove")) return "删除";
        if (name.startsWith("get") || name.startsWith("list") || name.startsWith("query")) return "查询";
        if (name.contains("approve")) return "审批";
        if (name.contains("reject")) return "拒绝";
        if (name.contains("cancel")) return "取消";
        if (name.contains("login")) return "登录";
        if (name.contains("logout")) return "登出";
        
        return name;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "127.0.0.1";
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
