package com.bopin.admin;

public record ApiResult<T>(int code, String message, T data) {
  public static <T> ApiResult<T> ok(T data) {
    return new ApiResult<>(0, "ok", data);
  }
}
