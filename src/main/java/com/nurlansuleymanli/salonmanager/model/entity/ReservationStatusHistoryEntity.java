package com.nurlansuleymanli.salonmanager.model.entity;

import com.nurlansuleymanli.salonmanager.model.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "reservation_status_history", schema = "public")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReservationStatusHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    ReservationEntity reservation;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", nullable = false)
    ReservationStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    ReservationStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    UserEntity changedBy;

    @Column(name = "changed_at", nullable = false)
    Instant changedAt = Instant.now();
}