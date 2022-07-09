package top.retain.nd.util;

import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import top.retain.nd.exception.UserNotLoginException;

import javax.servlet.http.HttpSession;

/**
 * @author Retain
 * @date 2021/10/3 14:54
 */
@Component
public class SpringSecurityUtil {
    public static String currentUser(HttpSession session) {
        SecurityContextImpl securityContext = (SecurityContextImpl) session.getAttribute("SPRING_SECURITY_CONTEXT");
        if (securityContext == null) {
            throw new UserNotLoginException("请先登录！");
        }
        return String.valueOf(securityContext.getAuthentication().getPrincipal());
    }
}
