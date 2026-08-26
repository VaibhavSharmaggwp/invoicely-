package com.invoicely.backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "businesses")
public class Business  implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)  // Secure UUID instead of 1,2,3....
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String phone;
    private String gstNumber;

    @Column(updatable = false)
    private LocalTime createdAt;

    @PrePersist // save hone se just pehle ye function chalega
    protected void onCreate(){
        this.createdAt = LocalTime.from(LocalDateTime.now());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return List.of();
    }

    @Override
    public String getPassword() {
        // Hum Google Auth use karenge, so password ki zaroorat nahi hai.
        return null;
    }

    @Override
    public String getUsername(){
        // Spring Security ke liye 'username' hamara email hai.
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

}
