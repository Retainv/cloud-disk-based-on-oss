package top.retain.nd.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

@Slf4j
public class LogFilter extends OncePerRequestFilter implements Ordered {
    // put filter at the end of all other filters to make sure we are processing after all others
    private int order = Ordered.LOWEST_PRECEDENCE - 8;
    public static final String SPLIT_STRING_M = "=";
    public static final String SPLIT_STRING_DOT = ", ";

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper wrapperRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrapperResponse = new ContentCachingResponseWrapper(response);
        String urlParams = getRequestParams(request);
        filterChain.doFilter(wrapperRequest, wrapperResponse);

        String requestBodyStr = getRequestBody(wrapperRequest);
        log.info("**USER:" + request.getRemoteUser());
        log.info("**URL : " + request.getRequestURL().toString());
        log.info("**HTTP_METHOD : " + request.getMethod());
        log.info("**IP : " + request.getRemoteAddr());
        log.info("**params[{}] | request body:{}", urlParams, requestBodyStr);

        String responseBodyStr = getResponseBody(wrapperResponse);
        log.info("**response body:{}", responseBodyStr);
        wrapperResponse.copyBodyToResponse();
    }

    /**
     * 打印请求参数
     *
     * @param request
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                String payload;
                try {
                    payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException e) {
                    payload = "[unknown]";
                }
                return payload.replaceAll(" ", "");
            }
        }
        return "";
    }

    /**
     * 打印返回参数
     *
     * @param response
     */
    private String getResponseBody(ContentCachingResponseWrapper response) {
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response,
                ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                String payload;
                try {
                    payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException e) {
                    payload = "[unknown]";
                }
                return payload;
            }
        }
        return "";
    }

    /**
     * 获取请求地址上的参数
     *
     * @param request
     * @return
     */
    public static String getRequestParams(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        Enumeration<String> enu = request.getParameterNames();
        //获取请求参数
        while (enu.hasMoreElements()) {
            String name = enu.nextElement();
            sb.append(name + SPLIT_STRING_M).append(request.getParameter(name));
            if (enu.hasMoreElements()) {
                sb.append(SPLIT_STRING_DOT);
            }
        }
        return sb.toString();
    }
}
