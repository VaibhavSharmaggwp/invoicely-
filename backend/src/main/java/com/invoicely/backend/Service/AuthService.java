package com.invoicely.backend.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.invoicely.backend.dto.AuthResponseDTO;
import com.invoicely.backend.entity.Business;
import com.invoicely.backend.repository.BusinessRepository;
import com.invoicely.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final BusinessRepository businessRepository;
    private final JwtService jwtService;

    @Value("${app.google.client-id}")
    private String googleClientId;

    public AuthResponseDTO authenticateWithGoogle(String googleIdTokenString){
        try {
            // 1. Google Verifier setup karo
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singleton(googleClientId))
                    .build();
            // 2. Token ko verify karo
            // Note: Development phase mein bina real frontend ke test karna mushkil hota hai.
            GoogleIdToken idToken = verifier.verify(googleIdTokenString);


            if(idToken != null){
                GoogleIdToken.Payload payload= idToken.getPayload();

                // 3. Google se user ka data nikalo
                String email = payload.getEmail();
                String name = (String) payload.get("name");

                // 4. DB mein check karo: User pehle se hai ya naya hai?
                Optional<Business> existingBusiness = businessRepository.findByEmail(email);
                Business business;

                if(existingBusiness.isPresent()){
                    // Login
                    business = existingBusiness.get();
                }else{
                    // Signup (Naya account banao)
                    business = Business.builder()
                            .email(email)
                            .name(name)
                            .build();
                    business = businessRepository.save(business);
                }
                // 5. Hamara custom JWT generate karo
                String jwtToken = jwtService.generateToken(business.getEmail());

                // 6. Response bhej do
                return AuthResponseDTO.builder()
                        .token(jwtToken)
                        .businessId(business.getId())
                        .name(business.getName())
                        .email(business.getEmail())
                        .build();
            } else {
                throw new RuntimeException("Invalid Google ID Token");
            }
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }
}
