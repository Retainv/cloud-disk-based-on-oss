package top.retain.nd.filter;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import top.retain.nd.common.CommonResultTool;
import top.retain.nd.common.StatusCode;
import top.retain.nd.config.security.TokenAuthenticationHelper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * @author waxijiang
 * jwt 对请求的验证
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        try {
            // 对用 token 获取到的用户进行校验
            Authentication authentication = TokenAuthenticationHelper.getAuthentication(httpServletRequest);
            // 凭证有则放行
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(httpServletRequest, httpServletResponse);


        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException |
                SignatureException | IllegalArgumentException e) {
            // 错误时
            httpServletResponse.setContentType("application/json; charset=UTF-8");
            PrintWriter out = httpServletResponse.getWriter();
            out.write(new ObjectMapper().writeValueAsString(CommonResultTool.fail(StatusCode.UN_AUTHORIZED, "Token " +
                    "expired，登陆已过期")));
            out.flush();
            out.close();
//            throw new RuntimeException(e);
//            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired，登陆已过期");
        }
    }
}
