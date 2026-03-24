package com.nurlansuleymanli.salonmanager.repository;

import com.nurlansuleymanli.salonmanager.model.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    List<ReservationEntity> findAllByBarberIdAndStartAtBetween(Long barberId, Instant startOfDay, Instant endOfDay);
}
