package com.nurlansuleymanli.salonmanager.repository;

import com.nurlansuleymanli.salonmanager.model.entity.BarberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BarberRepository extends JpaRepository<BarberEntity, Long> {

    Page<BarberEntity> findAllByIsActiveTrue(Pageable pageable);

    Optional<BarberEntity> findByUserId(Long userId);
}
