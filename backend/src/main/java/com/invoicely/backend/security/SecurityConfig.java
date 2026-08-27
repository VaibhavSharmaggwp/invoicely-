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
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
        // 1. CSRF disable karo. (REST APIs jo JWT use karti hain unko CSRF protection ki zaroorat nahi hoti)
                .csrf(csrf -> csrf.disable())

        // 2. Rules set karo: Kaunsi API public hai, kaunsi private?
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health").permitAll() // Health check koi bhi dekh sakta hai
                        .requestMatchers("/api/v1/auth/**").permitAll() // Login/Signup public hona chahiye
                        .requestMatchers("/error").permitAll() // Error endpoint public to prevent 403 masking
                        .anyRequest().authenticated() // Baaki saari APIs ke liye Token chahiye
                )
        // 3. Session management ko Stateless karo. (Kyunki hum JWT use kar rahe hain,
        // server ko user ka session yaad rakhne ki zaroorat nahi. Har request mein naya token aayega)
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

        // 4. Hamara custom 'Security Guard' (JwtFilter) default filter se pehle khada kar do
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
