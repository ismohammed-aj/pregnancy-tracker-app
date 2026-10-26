package com.pfe.mamacareplus.repository;

import com.pfe.mamacareplus.entity.PasswordResetCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCodeEntity, Long> {

    Optional<PasswordResetCodeEntity> findFirstByEmailAndCodeOrderByCreatedAtDesc(String email, String code);

    @Modifying
    @Query("UPDATE PasswordResetCodeEntity c SET c.used = true WHERE c.email = :email AND c.used = false")
    int invalidateUnusedCodesByEmail(@Param("email") String email);
}
