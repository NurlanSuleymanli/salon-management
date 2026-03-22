package com.nurlansuleymanli.salonmanager.repository;

import com.nurlansuleymanli.salonmanager.model.entity.SalonEntity;
import io.jsonwebtoken.security.Jwks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalonRepository extends JpaRepository<SalonEntity , Long> {


}
