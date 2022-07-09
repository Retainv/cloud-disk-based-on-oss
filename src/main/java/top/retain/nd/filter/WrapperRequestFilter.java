package top.retain.nd.filter;

import lombok.extern.slf4j.Slf4j;
import top.retain.nd.util.RequestWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@Slf4j
public class WrapperRequestFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 如果是请求过滤
        if (!servletRequest.getInputStream().isFinished() && !servletRequest.getContentType().contains("multipart" +
                "/form" +
                "-data;")) {
            ServletRequest requestWrapper = null;
            requestWrapper = new RequestWrapper((HttpServletRequest) servletRequest);
            log.info("包装request");
            filterChain.doFilter(requestWrapper, servletResponse);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}
