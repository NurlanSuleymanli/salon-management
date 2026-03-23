package com.nurlansuleymanli.salonmanager.repository;


import com.nurlansuleymanli.salonmanager.model.dto.response.ServiceResponseDto;
import com.nurlansuleymanli.salonmanager.model.entity.ServiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {


    Optional<ServiceEntity> findByName(String name);

    Page<ServiceEntity> findAllByIsActiveTrue(Pageable pageable);
}
