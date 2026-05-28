package com.rental.user.aspect;

import com.example.rentalcommon.annotation.OperationLog;
import com.example.rentalcommon.security.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Pointcut("@annotation(com.example.rentalcommon.annotation.OperationLog)")
    public void logPoint() {}

    @Around("logPoint() && @annotation(op)")
    public Object handleLog(ProceedingJoinPoint joinPoint, OperationLog op) throws Throwable {
        long start = System.currentTimeMillis();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = auth != null && auth.getPrincipal() instanceof LoginUser lu ? lu : null;

        try {
            Object result = joinPoint.proceed();
            long time = System.currentTimeMillis() - start;

            log.info("[操作日志] 用户: {}, 角色: {}, 动作: {}, 耗时: {}ms",
                    loginUser != null ? loginUser.getUsername() : "匿名",
                    loginUser != null ? loginUser.getRole() : "未知",
                    op.value(), time);
            return result;
        } catch (Throwable e) {
            log.error("[操作异常] 用户: {}, 动作: {}, 错误: {}",
                    loginUser != null ? loginUser.getUsername() : "匿名", op.value(), e.getMessage());
            throw e;
        }
    }
}
