package com.feedback.feedback_management.util;

import io.jsonwebtoken.Claims;

public class ServiceUtils {
    public static Claims extractClaimsFromToken(String token, JwtUtil jwtUtil) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid or missing token");
        }
        return jwtUtil.parseToken(token.replace("Bearer ", ""));
    }

    public static Long extractUserId(Claims claims) {
        return Long.parseLong(claims.get("id").toString());
    }

    public static String extractUserRole(Claims claims) {
        return claims.get("role").toString();
    }
}
