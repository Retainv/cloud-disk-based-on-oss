package top.retain.nd.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.retain.nd.common.CommonResult;
import top.retain.nd.common.CommonResultTool;
import top.retain.nd.common.StatusCode;
import top.retain.nd.exception.SmsCodeException;
import top.retain.nd.exception.UserExistException;
import top.retain.nd.exception.UserNotLoginException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.util.List;

/**
 * @author Retain
 * @date 2021/10/3 16:15
 */
@Slf4j
@RestControllerAdvice
public class ControllerAdvice {
    @Resource
    private HttpServletResponse response;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult validFormError(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder sb = new StringBuilder();
        bindingResult.getFieldErrors().forEach(error -> sb.append(error.getField()).append(error.getDefaultMessage()).append(";"));
        log.error("错误消息：{}",e.getMessage(),e);
        return responseError(422, sb.toString());
    }

    @ExceptionHandler(BindException.class)
    public CommonResult validError(BindException e) {
        List<FieldError> errors = e.getFieldErrors();
        StringBuilder sb = new StringBuilder();
        errors.forEach(error -> sb.append(error.getField()).append(' ').append(error.getDefaultMessage()).append(";"));
        log.error("错误消息：{}",e.getMessage(),e);

        return responseError(422, sb.toString());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public CommonResult requestParamsError(MissingServletRequestParameterException e) {
        log.error("错误消息：{}",e.getMessage(),e);

        return new CommonResult(422, e.getMessage(),null );
    }

    @ExceptionHandler({IllegalArgumentException.class, FileAlreadyExistsException.class})
    public CommonResult paramsError(Exception e) {
        log.error("错误消息：{}",e.getMessage(),e);

        return CommonResultTool.fail(StatusCode.FILE_EXSIT, e.getMessage());
    }


    @ExceptionHandler(AccessDeniedException.class)
    public CommonResult handle(AccessDeniedException e) {
        return responseError(403, e.getMessage());
    }

    @ExceptionHandler({
            FileNotFoundException.class,
            NoSuchFileException.class,
    })
    public CommonResult handle(Exception e) {
        if (log.isDebugEnabled()) {
            log.error("错误消息：{}",e.getMessage(),e);
            e.printStackTrace();
        }
        return responseError(404, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public CommonResult defaultHandle(Exception e) {
        log.error("错误消息：{}",e.getMessage(),e);

        return responseError(500,  e.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public CommonResult handle(DuplicateKeyException e) {
        log.error("错误消息：{}",e.getMessage(),e);

        return responseError(400, e.getMessage());
    }

    @ExceptionHandler({UserNotLoginException.class})
    public CommonResult handle(UserNotLoginException e) {
        log.error("错误消息：{}",e.getMessage(),e);

        return responseError(403, e.getMessage());
    }

    @ExceptionHandler({UserExistException.class, SmsCodeException.class})
    public CommonResult handle(UserExistException e) {
        log.error("错误消息：{}",e.getMessage(),e);
        return responseError(401, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public CommonResult handle(RuntimeException e) {
        log.error("错误消息：{}",e.getMessage(),e);
        return new CommonResult(501, e.getMessage(),null );
    }
    private CommonResult responseError(int code, String message) {

        response.setStatus(code);
        return new CommonResult(code, message, null);
    }

}
