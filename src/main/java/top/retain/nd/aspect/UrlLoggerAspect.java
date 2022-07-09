package top.retain.nd.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @author Retain
 * @date 2021/12/10 10:36
 * 已弃用,使用LogFilter
 */
@Component
@Aspect
@Slf4j
@Deprecated
public class UrlLoggerAspect {
//    @Pointcut("execution(* top.retain.nd.controller.*.*(..))")
    public void pt(){};

//    @Before("pt()") //在切入点的方法run之前要干的
    public void logBeforeController(JoinPoint joinPoint){
        // 这个RequestContextHolder是Springmvc提供来获得请求的东西
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
        // 记录下请求内容
        log.info("**USER:" + request.getRemoteUser());
        log.info("**URL : " + request.getRequestURL().toString());
        log.info("**HTTP_METHOD : " + request.getMethod());
        log.info("**IP : " + request.getRemoteAddr());
        log.info("**THE ARGS OF THE CONTROLLER : " + Arrays.toString(joinPoint.getArgs()));
        //下面这个getSignature().getDeclaringTypeName()是获取包+类名的   然后后面的joinPoint.getSignature.getName()获取了方法名
        log.info("**CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
    }
}
