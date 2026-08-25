package com.invoicely.backend.repository;

import com.invoicely.backend.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessRepository extends JpaRepository<Business, UUID> {
    // Spring Data JPA magic: Yeh automatically 'SELECT * FROM businesses WHERE email = ?' ki query bana dega.
    Optional<Business> findByEmail(String email);
}
