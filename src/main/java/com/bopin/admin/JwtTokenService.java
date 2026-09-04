package com.bopin.admin;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Stateless JWT access-token service.
 *
 * The token carries only the authenticated subject and role. Business data
 * remains in MySQL; this class is responsible for signing and validating the
 * time-bounded access credential sent in the Authorization header.
 */
@Service
public class JwtTokenService {
  private final SecretKey signingKey;
  private final long expirationMs;
  private final String issuer;

  public JwtTokenService(
      @Value("${security.jwt.secret}") String secret,
      @Value("${security.jwt.expiration-ms:2592000000}") long expirationMs,
      @Value("${security.jwt.issuer:bopin-admin-server}") String issuer) {
    if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
      throw new IllegalStateException("security.jwt.secret must contain at least 32 bytes");
    }
    if (expirationMs <= 0) throw new IllegalStateException("security.jwt.expiration-ms must be positive");
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationMs = expirationMs;
    this.issuer = issuer;
  }

  public String issue(String userId, String role) {
    Instant issuedAt = Instant.now();
    return Jwts.builder()
      .id(UUID.randomUUID().toString())
      .issuer(issuer)
      .subject(userId)
      .claim("role", role)
      .issuedAt(Date.from(issuedAt))
      .expiration(Date.from(issuedAt.plusMillis(expirationMs)))
      .signWith(signingKey)
      .compact();
  }

  /** Access-token lifetime in seconds, suitable for an OAuth-style response. */
  public long expiresInSeconds() {
    return expirationMs / 1000L;
  }

  /** Returns the user id for a valid token, or null for an invalid/expired token. */
  public String subject(String token) {
    Claims claims = parse(token);
    return claims == null ? null : claims.getSubject();
  }

  public String role(String token) {
    Claims claims = parse(token);
    return claims == null ? null : claims.get("role", String.class);
  }

  private Claims parse(String token) {
    if (token == null || token.isBlank()) return null;
    try {
      return Jwts.parser()
        .verifyWith(signingKey)
        .requireIssuer(issuer)
        .build()
        .parseSignedClaims(token)
        .getPayload();
    } catch (JwtException | IllegalArgumentException error) {
      return null;
    }
  }
}
