package com.invoicely.backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "businesses")
public class Business {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)  // Secure UUID instead of 1,2,3....
    private UUID id;

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

}
