package com.nurlansuleymanli.salonmanager.repository;

import com.nurlansuleymanli.salonmanager.model.entity.SalonEntity;
import io.jsonwebtoken.security.Jwks;
import org.apache.el.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalonRepository extends JpaRepository<SalonEntity , Long> {


    Page<SalonEntity> findAllByIsActiveTrue(Pageable pageable);

    Optional<SalonEntity> findByName(String name);
}
