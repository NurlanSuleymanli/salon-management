package com.nurlansuleymanli.salonmanager.repository;

import com.nurlansuleymanli.salonmanager.model.entity.BarberWorkingHourEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.Optional;

public interface BarberWorkingHourRepository extends JpaRepository<BarberWorkingHourEntity, Long> {

    Optional<BarberWorkingHourEntity> findByBarberIdAndDayOfWeek(Long barberId, DayOfWeek dayOfWeek);

}
