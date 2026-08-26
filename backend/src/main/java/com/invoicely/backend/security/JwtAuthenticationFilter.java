package com.invoicely.backend.security;

import com.invoicely.backend.repository.BusinessRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
// OncePerRequestFilter ensure karta hai ki yeh filter har API call par sirf ek baar chale
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final BusinessRepository businessRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException{
        // 1. Request ke header se 'Authorization' nikalo
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Agar header nahi hai ya "Bearer " se start nahi hota,
        // toh aage badhne do (SecurityConfig decide karega block karna hai ya nahi)
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }
        // 3. "Bearer " ke baad ka actual token nikal lo (substring 7)
        jwt = authHeader.substring(7);

        // 4. Token se email nikalo
        userEmail = jwtService.extractEmail(jwt);

        // 5. Agar email mila aur abhi tak user authenticate nahi hua hai context mein
        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
            // DB se user find karo
            UserDetails userDetails = businessRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 6. Agar token valid hai, toh Spring ko bata do ki "Yeh user legit hai"
            if(jwtService.isTokenValid(jwt, userDetails.getUsername())){
                // Security token banao
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Spring ke context mein save kar do, taaki controllers ko pata chale kaun logged in hai
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // 7. Agle filter ya controller ke paas request bhej do
        filterChain.doFilter(request, response);
    }

}
