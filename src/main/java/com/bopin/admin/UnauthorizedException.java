package com.bopin.admin;

/** Raised when an access token is missing, expired, or cannot be verified. */
public class UnauthorizedException extends RuntimeException {
  public UnauthorizedException(String message) {
    super(message);
  }
}
