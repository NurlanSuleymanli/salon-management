package com.nurlansuleymanli.salonmanager.repository;

import com.nurlansuleymanli.salonmanager.model.entity.ReservationEntity;
import com.nurlansuleymanli.salonmanager.model.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    List<ReservationEntity> findAllByBarberIdAndStartAtBetween(Long barberId, Instant startOfDay, Instant endOfDay);

    List<ReservationEntity> findAllByCustomerIdOrderByStartAtDesc(Long customerId);

    @Query("SELECT r FROM ReservationEntity r WHERE r.barber.id = :barberId AND r.startAt >= :from ORDER BY r.startAt ASC")
    List<ReservationEntity> findBarberScheduleFrom(@Param("barberId") Long barberId, @Param("from") Instant from);

    @Query("SELECT r FROM ReservationEntity r WHERE r.barber.id = :barberId AND r.startAt < :endAt AND r.endAt > :startAt AND r.status NOT IN :excludedStatuses")
    List<ReservationEntity> findConflictingReservations(@Param("barberId") Long barberId, @Param("startAt") Instant startAt, @Param("endAt") Instant endAt, @Param("excludedStatuses") List<ReservationStatus> excludedStatuses);

    Page<ReservationEntity> findAll(Pageable pageable);
}
