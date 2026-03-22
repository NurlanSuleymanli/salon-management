package com.nurlansuleymanli.salonmanager.repository;


import com.nurlansuleymanli.salonmanager.model.dto.response.ServiceResponseDto;
import com.nurlansuleymanli.salonmanager.model.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {


}
