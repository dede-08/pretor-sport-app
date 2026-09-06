package com.pretor_sport.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> PROTECTED_PATHS = Set.of(
            "/auth/login",
            "/auth/refresh",
            "/auth/register",
            "/auth/resend-verification"
    );

    private final int maxAttempts;
    private final long windowSeconds;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Long>> attempts = new ConcurrentHashMap<>();

    public RateLimitFilter(
            @Value("${app.security.rate-limit.max-attempts:5}") int maxAttempts,
            @Value("${app.security.rate-limit.window-seconds:60}") long windowSeconds,
            ObjectMapper objectMapper) {
        this.maxAttempts = maxAttempts;
        this.windowSeconds = windowSeconds;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return PROTECTED_PATHS.stream().noneMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String key = resolveKey(request);

        if (!allow(key)) {
            log.warn("Rate limit excedido para {} en {}", key, request.getServletPath());
            writeTooManyRequests(response, request);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveKey(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && !userAgent.isBlank()) {
            return ip + "|" + userAgent.hashCode();
        }
        return ip;
    }

    private boolean allow(String key) {
        long now = System.currentTimeMillis();
        long cutoff = now - windowSeconds * 1000;

        ConcurrentLinkedDeque<Long> deque = attempts.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        synchronized (deque) {
            while (!deque.isEmpty() && deque.peekFirst() < cutoff) {
                deque.pollFirst();
            }
            if (deque.isEmpty()) {
                attempts.remove(key, deque);
            }
            if (deque.size() >= maxAttempts) {
                return false;
            }
            deque.addLast(now);
            return true;
        }
    }

    private void writeTooManyRequests(HttpServletResponse response, HttpServletRequest request) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        body.put("error", "Demasiadas peticiones");
        body.put("message", "Has excedido el límite de intentos. Intenta de nuevo más tarde.");
        body.put("path", request.getRequestURI());

        objectMapper.writeValue(response.getWriter(), body);
    }
}