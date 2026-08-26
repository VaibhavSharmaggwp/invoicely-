package com.invoicely.backend.controller;

import com.invoicely.backend.Service.AuthService;
import com.invoicely.backend.dto.AuthRequestDTO;
import com.invoicely.backend.dto.AuthResponseDTO;
import com.invoicely.backend.entity.Business;
import com.invoicely.backend.repository.BusinessRepository;
import com.invoicely.backend.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth") // SecurityConfig permitAll()
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final BusinessRepository businessRepository;
    private final JwtService jwtService;

    @PostMapping("/google")
    public ResponseEntity<AuthResponseDTO> googleLogin(@Valid @RequestBody AuthRequestDTO requestDTO){
        // Android yahan Google ka token bhejega
        AuthResponseDTO response = authService.authenticateWithGoogle(requestDTO.getGoogleIdToken());
        return ResponseEntity.ok(response);
    }

    // Dev Testing ke liye temporary endpoint (Google bypass)
    @PostMapping("/dev-token")
    public ResponseEntity<AuthResponseDTO> getDevToken(@RequestParam String email){
        // 1. DB check karo, nahi hai toh dummy bana do taaki error na aaye
        Business business = businessRepository.findByEmail(email)
                .orElseGet(() -> businessRepository.save(
                        Business.builder().email(email).name("Test User").build()
                ));

        // 2. Custom JWT generate karo
        String jwtToken = jwtService.generateToken(business.getEmail());

        return ResponseEntity.ok(AuthResponseDTO.builder()
                .token(jwtToken)
                .businessId(business.getId())
                .name(business.getName())
                .email(business.getEmail())
                .build());
    }
}
