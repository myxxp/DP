package com.crabdp.config;
import com.crabdp.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 处理客户端输入错误
    @ExceptionHandler(ClientInputException.class)
    public Result handleClientInputException(ClientInputException e) {
        // 记录错误日志
        log.warn("客户端输入错误：{}", e.getMessage());
        // 返回特定的错误信息
        return Result.fail(e.getMessage());
    }

    // 处理资源未找到错误
    @ExceptionHandler(ResourceNotFoundException.class)
    public Result handleResourceNotFoundException(ResourceNotFoundException e) {
        // 记录错误日志
        log.warn("资源未找到：{}", e.getMessage());
        // 返回特定的错误信息
        return Result.fail( e.getMessage());
    }

    // 处理其他运行时异常
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        // 记录详细错误日志
        log.error("服务器异常：{}", e.getMessage(), e);
        // 返回通用的错误信息，不暴露具体异常信息
        return Result.fail( "服务器内部错误，请稍后重试");
    }
}
