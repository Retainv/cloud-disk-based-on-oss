package top.retain.nd.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;
import top.retain.nd.common.CommonResult;
import top.retain.nd.common.CommonResultTool;
import top.retain.nd.common.StatusCode;
import top.retain.nd.config.security.SmsCodeAuthenticationToken;
import top.retain.nd.config.security.TokenAuthenticationHelper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Retain
 * @date 2021/10/19 15:42
 */
public class SmsCodeAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    /**
     * form表单中手机号码的字段name
     */
    public static final String SPRING_SECURITY_FORM_MOBILE_KEY = "phoneNumber";

    private String mobileParameter = SPRING_SECURITY_FORM_MOBILE_KEY;
    /**
     * 是否仅 POST 方式
     */
    private boolean postOnly = true;

    public SmsCodeAuthenticationFilter() {
        // 短信登录的请求 post 方式的 /sms/login
        super(new AntPathRequestMatcher("/sms/login", "POST"));
    }
    public SmsCodeAuthenticationFilter(String defaultFilterProcessesUrl, AuthenticationManager authenticationManager) {
        super(new AntPathRequestMatcher(defaultFilterProcessesUrl));
        // 为 AbstractAuthenticationProcessingFilter 中的属性赋值
        setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (postOnly && !"POST".equals(request.getMethod())) {
            throw new AuthenticationServiceException(
                    "Authentication method not supported: " + request.getMethod());
        }

        String mobile = request.getParameter(mobileParameter);
        String code = request.getParameter("code");

        if (mobile == null) {
            mobile = "";
        }

        mobile = mobile.trim();

        SmsCodeAuthenticationToken authRequest = new SmsCodeAuthenticationToken(mobile, code);

        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);
    }


    protected void setDetails(HttpServletRequest request, SmsCodeAuthenticationToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }

    public String getMobileParameter() {
        return mobileParameter;
    }

    public void setMobileParameter(String mobileParameter) {
        Assert.hasText(mobileParameter, "Mobile parameter must not be empty or null");
        this.mobileParameter = mobileParameter;
    }

    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        TokenAuthenticationHelper.addAuthentication(request, response, authResult);
    }

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
            // 验证码错误
        } else if (failed instanceof BadCredentialsException) {
            commonResult = CommonResultTool.fail(StatusCode.CODE_WRONG);
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
