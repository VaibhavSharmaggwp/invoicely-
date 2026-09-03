package com.invoicely.backend.security;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // Spring ko batata hai ki yeh configuration class hai
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final RateLimitFilter rateLimitFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
        // 1. CSRF disable karo. (REST APIs jo JWT use karti hain unko CSRF protection ki zaroorat nahi hoti)
                .csrf(csrf -> csrf.disable())

        // 2. Rules set karo: Kaunsi API public hai, kaunsi private?
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health").permitAll() // Health check koi bhi dekh sakta hai
                        .requestMatchers("/api/v1/auth/**").permitAll() // Login/Signup public hona chahiye
                        // Public invoice links ke liye we dont need JWT
                        .requestMatchers("/api/v1/public/**").permitAll()
                        .requestMatchers("/api/v1/webhooks/**").permitAll()
                        .requestMatchers("/error").permitAll() // Error endpoint public to prevent 403 masking error
                        .anyRequest().authenticated() // Baaki saari APIs ke liye Token chahiye
                )
        // 3. Session management ko Stateless karo. (Kyunki hum JWT use kar rahe hain,
        // server ko user ka session yaad rakhne ki zaroorat nahi. Har request mein naya token aayega)
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

        // 4. Rate Limiter pehle, fir custom JwtFilter default filter se pehle khada kar do
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
