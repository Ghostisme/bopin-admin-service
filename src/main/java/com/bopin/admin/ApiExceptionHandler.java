package com.bopin.admin;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(BusinessException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResult<Map<String, Object>> handleBusiness(BusinessException error) {
    return new ApiResult<>(400, error.getMessage(), Map.of());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiResult<Map<String, Object>> handleUnexpected(Exception error) {
    return new ApiResult<>(500, "服务暂时不可用", Map.of("error", error.getClass().getSimpleName()));
  }
}
