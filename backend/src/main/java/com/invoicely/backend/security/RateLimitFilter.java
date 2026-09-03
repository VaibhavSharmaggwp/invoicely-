package com.invoicely.backend.security;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component

public class RateLimitFilter extends OncePerRequestFilter {
    // Tracks buckets by IP address.
    // (In a multi-server setup, this map is replaced by your Redis connection!)
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Defines the rules: 20 requests allowed per minute
    private Bucket createBucket(){
        Bandwidth limit = Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws SecurityException, IOException, ServletException {
        // 1. Identify the user by their IP address
        String clientIp = request.getRemoteAddr();
        // 2. Fetch their specific bucket (or create one if it's their first time)
        Bucket bucket = buckets.computeIfAbsent(clientIp, k -> createBucket());

        // 3. Try to consume 1 token for this API call
        if(bucket.tryConsume(1)){
            // Success! Let the request proceed to Spring Security/Controllers
            filterChain.doFilter(request, response);
        }else{
            // Blocked! Bucket is empty. Return HTTP 429 Too Many Requests
            System.out.println(" RATE LIMIT TRIGGERED: Blocked IP " + clientIp);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests. Please try again in a minute.");
        }
    }
}
