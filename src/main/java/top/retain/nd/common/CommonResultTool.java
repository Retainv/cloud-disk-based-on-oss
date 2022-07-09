package top.retain.nd.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author waxijiang
 */
public class CommonResultTool {

    public static CommonResult success(){
        return new CommonResult(StatusCode.SUCCESS.getCode(), StatusCode.SUCCESS.getMsg());
    }

    public static CommonResult success(String msg, Object data){
        return new CommonResult(StatusCode.SUCCESS.getCode(), msg, data);
    }

    public static CommonResult success(Object data){
        return new CommonResult(StatusCode.SUCCESS.getCode(), StatusCode.SUCCESS.getMsg(), data);
    }

    public static CommonResult fail(StatusCode statusCode){
        return new CommonResult(statusCode.getCode(), statusCode.getMsg());
    }
    public static CommonResult fail(AuthenticationException exception){
        return new CommonResult(411, exception.getMessage());
    }
    public static CommonResult fail(StatusCode statusCode, String msg){
        return new CommonResult(statusCode.getCode(), msg, null);
    }

    public static void toJson(Integer code, String msg, int total, int current, Object data, HttpServletResponse response) {

        ObjectMapper mapper = new ObjectMapper();
        PrintWriter writer;
        try {
            response.flushBuffer();
            writer = response.getWriter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            CommonResult commonResult = new CommonResult().setCode(code).setMessage(msg).setData(data).setTotal(total).setCurrentPage(current);
            String value = mapper.writeValueAsString(commonResult);
            writer.write(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            writer.close();
        }
    }

}
