package top.retain.nd.filter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.util.HtmlUtils;
import top.retain.nd.common.CommonResult;
import top.retain.nd.common.CommonResultTool;
import top.retain.nd.common.StatusCode;
import top.retain.nd.config.security.TokenAuthenticationHelper;
import top.retain.nd.entity.User;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * JWT 登陆过滤器
 */
public class JwtLoginFilter extends AbstractAuthenticationProcessingFilter {

    /**
     * @param defaultFilterProcessesUrl 配置要过滤的地址，即登陆地址
     * @param authenticationManager     认证管理器，校验身份时会用到
     */
    public JwtLoginFilter(String defaultFilterProcessesUrl, AuthenticationManager authenticationManager) {
        super(new AntPathRequestMatcher(defaultFilterProcessesUrl));
        // 为 AbstractAuthenticationProcessingFilter 中的属性赋值
        setAuthenticationManager(authenticationManager);
    }


    /**
     * 提取用户账号密码进行验证
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException, IOException, ServletException {
        try {
            // 获取 User 对象
            // readValue 第一个参数 输入流，第二个参数 要转换的对象
            User user = new ObjectMapper().readValue(httpServletRequest.getInputStream(), User.class);
            // 对 html 标签进行转义，防止 XSS 攻击
            String username = user.getUsername();
            username = HtmlUtils.htmlEscape(username);
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    username,
                    user.getPassword()
            );
            // 进行登陆验证
            return getAuthenticationManager().authenticate(token);
        } catch (JsonParseException e) {
            throw new InsufficientAuthenticationException("认证信息不足");
        }
    }

    /**
     * 登陆成功回调
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        // 登陆成功
        TokenAuthenticationHelper.addAuthentication(request, response, authResult);
    }

    /**
     * 登陆失败回调
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        // 默认错误
        CommonResult commonResult = CommonResultTool.fail(StatusCode.INTERNAL_SERVER_ERROR);
        // 用户名错误
        if (failed instanceof UsernameNotFoundException) {
            commonResult = CommonResultTool.fail(StatusCode.UNKNOWN_USERNAME);
            // 用户已被删除
        } else if (failed instanceof DisabledException) {
            commonResult = CommonResultTool.fail(StatusCode.USER_DISABLED);
            // 用户密码错误
        } else if (failed instanceof BadCredentialsException) {
            commonResult = CommonResultTool.fail(StatusCode.WRONG_USERNAME_OR_PASSWORD);
            // 用户已被禁用(锁定)
        } else if (failed instanceof LockedException) {
            commonResult = CommonResultTool.fail(StatusCode.USER_LOCKED);
        } else if (failed instanceof InsufficientAuthenticationException) {
            commonResult = CommonResultTool.fail(StatusCode.SYNTAX_ERROR, failed.getLocalizedMessage());
        }

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.write(new ObjectMapper().writeValueAsString(commonResult));
        out.flush();
        out.close();
    }
}
