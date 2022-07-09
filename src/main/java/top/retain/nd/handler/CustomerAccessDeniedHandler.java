package top.retain.nd.handler;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import top.retain.nd.common.CommonResultTool;
import top.retain.nd.common.StatusCode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Retain
 */
public class CustomerAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
                httpServletResponse.setContentType("application/json; charset=UTF-8");
                PrintWriter out = httpServletResponse.getWriter();
                out.write(new ObjectMapper().writeValueAsString(CommonResultTool.fail(StatusCode.NO_PERMISSION)));
                out.flush();
                out.close();
    }
}
