package com.nurlansuleymanli.salonmanager.repository;

import com.nurlansuleymanli.salonmanager.model.entity.TokenBlacklistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklistEntity, Long> {
    
    boolean existsByToken(String token);
}
