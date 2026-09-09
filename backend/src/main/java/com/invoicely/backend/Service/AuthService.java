package com.invoicely.backend.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.invoicely.backend.dto.AuthRequestDTO;
import com.invoicely.backend.dto.AuthResponseDTO;
import com.invoicely.backend.entity.Business;
import com.invoicely.backend.repository.BusinessRepository;
import com.invoicely.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final BusinessRepository businessRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.google.client-id}")
    private String googleClientId;

    public AuthResponseDTO register(AuthRequestDTO requestDTO) {
        if (requestDTO.getEmail() == null || requestDTO.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (requestDTO.getPassword() == null || requestDTO.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }

        String cleanEmail = requestDTO.getEmail().trim().toLowerCase();
        Optional<Business> existing = businessRepository.findByEmail(cleanEmail);
        if (existing.isPresent()) {
            throw new RuntimeException("Email is already registered. Please log in.");
        }

        String businessName = requestDTO.getBusinessName() != null && !requestDTO.getBusinessName().isBlank()
                ? requestDTO.getBusinessName().trim()
                : "My Business";

        String phone = requestDTO.getPhone() != null && !requestDTO.getPhone().isBlank()
                ? requestDTO.getPhone().trim()
                : null;

        Business business = Business.builder()
                .email(cleanEmail)
                .name(businessName)
                .phone(phone)
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .build();

        business = businessRepository.save(business);

        String jwtToken = jwtService.generateToken(business.getEmail());

        return AuthResponseDTO.builder()
                .token(jwtToken)
                .businessId(business.getId())
                .name(business.getName())
                .email(business.getEmail())
                .build();
    }

    public AuthResponseDTO login(AuthRequestDTO requestDTO) {
        if (requestDTO.getEmail() == null || requestDTO.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (requestDTO.getPassword() == null || requestDTO.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }

        String cleanEmail = requestDTO.getEmail().trim().toLowerCase();
        Business business = businessRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new RuntimeException("Account not found. Please sign up first."));

        if (business.getPassword() == null || !passwordEncoder.matches(requestDTO.getPassword(), business.getPassword())) {
            throw new RuntimeException("Invalid email or password.");
        }

        String jwtToken = jwtService.generateToken(business.getEmail());

        return AuthResponseDTO.builder()
                .token(jwtToken)
                .businessId(business.getId())
                .name(business.getName())
                .email(business.getEmail())
                .build();
    }

    public AuthResponseDTO authenticateWithGoogle(String googleIdTokenString){
        try {
            if (googleIdTokenString == null || googleIdTokenString.isBlank()) {
                throw new RuntimeException("Google ID Token is required.");
            }

            // Dev mode token support for testing
            if (googleIdTokenString.startsWith("google_dev_") || googleIdTokenString.equalsIgnoreCase("test_token")) {
                String testEmail = "google.test.user@invoicely.com";
                String testName = "Google Test User";
                Business business = businessRepository.findByEmail(testEmail)
                        .orElseGet(() -> businessRepository.save(
                                Business.builder().email(testEmail).name(testName).build()
                        ));

                String jwtToken = jwtService.generateToken(business.getEmail());
                return AuthResponseDTO.builder()
                        .token(jwtToken)
                        .businessId(business.getId())
                        .name(business.getName())
                        .email(business.getEmail())
                        .build();
            }

            // Production Google ID Token Verifier
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singleton(googleClientId))
                    .build();
            GoogleIdToken idToken = verifier.verify(googleIdTokenString);

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                String email = payload.getEmail();
                String name = (String) payload.get("name");

                Optional<Business> existingBusiness = businessRepository.findByEmail(email);
                Business business;

                if (existingBusiness.isPresent()) {
                    business = existingBusiness.get();
                } else {
                    business = Business.builder()
                            .email(email)
                            .name(name != null ? name : "Google User")
                            .build();
                    business = businessRepository.save(business);
                }
                String jwtToken = jwtService.generateToken(business.getEmail());

                return AuthResponseDTO.builder()
                        .token(jwtToken)
                        .businessId(business.getId())
                        .name(business.getName())
                        .email(business.getEmail())
                        .build();
            } else {
                throw new RuntimeException("Invalid or expired Google ID Token");
            }
        } catch (Exception e) {
            String msg = (e.getMessage() != null && !e.getMessage().isBlank()) ? e.getMessage() : "Invalid Token Format";
            throw new RuntimeException("Google Authentication failed: " + msg);
        }
    }
}
