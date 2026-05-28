package com.example.back.controller;

import com.example.back.dto.Result;
import jakarta.validation.ConstraintViolationException;
import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：将常见异常转换为统一的 {@link Result} 响应结构。
 *
 * <p>注意：部分异常（例如客户端主动断连）不应当再尝试写回响应体，否则会导致二次异常/日志刷屏。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理客户端断连/取消请求异常。
     *
     * <p>客户端断连/取消请求属于“连接生命周期”问题，通常不是业务异常。
     * 这里不返回 Result，避免在连接已关闭时继续写响应导致更多异常。</p>
     *
     * @param e 异常实例
     */
    @ExceptionHandler({AsyncRequestNotUsableException.class, ClientAbortException.class})
    public void handleClientAbort(Exception e) {
        // 客户端断连/取消请求：属于“连接生命周期”问题，通常不是业务异常。
        // 这里不返回 Result，避免在连接已关闭时继续写响应导致更多异常。
        log.debug("Client aborted connection: {}", e.getMessage());
    }

    /**
     * 处理非法参数异常。
     *
     * @param e 异常实例
     * @return 统一的错误响应结构
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        String msg = e.getMessage() == null || e.getMessage().trim().isEmpty() ? "参数错误" : e.getMessage();
        return Result.error(msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return Result.error("请求参数格式错误");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String msg = "参数校验失败";
        if (e.getBindingResult() != null && e.getBindingResult().getFieldError() != null) {
            String m = e.getBindingResult().getFieldError().getDefaultMessage();
            if (m != null && !m.trim().isEmpty()) msg = m;
        }
        return Result.error(msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String msg = e.getMessage() == null || e.getMessage().trim().isEmpty() ? "参数校验失败" : e.getMessage();
        return Result.error(msg);
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        String msg = e.getMessage() == null || e.getMessage().trim().isEmpty() ? "操作失败" : e.getMessage();
        log.warn("RuntimeException: {}", msg);
        return Result.error(msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        String msg = e.getMessage() == null || e.getMessage().trim().isEmpty() ? "服务器错误" : e.getMessage();
        log.error("Unhandled exception", e);
        return Result.error("服务器错误: " + msg);
    }
}
