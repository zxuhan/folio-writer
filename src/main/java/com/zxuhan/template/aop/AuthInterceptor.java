package com.zxuhan.template.aop;

import com.zxuhan.template.annotation.AuthCheck;
import com.zxuhan.template.exception.BusinessException;
import com.zxuhan.template.exception.ErrorCode;
import com.zxuhan.template.model.entity.User;
import com.zxuhan.template.model.enums.UserRoleEnum;
import com.zxuhan.template.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Authorization check AOP.
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * Execute the authorization check.
     *
     * @param joinPoint the join point
     * @param authCheck the authorization check annotation
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // Get the currently logged-in user
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        // No role required — allow through
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        // The following code requires a specific role to pass
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        // No role assigned — deny access
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // Admin role required but current user is not admin — deny access
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // Authorization passed — allow through
        return joinPoint.proceed();
    }
}
