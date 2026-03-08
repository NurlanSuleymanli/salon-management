package com.nurlansuleymanli.salonmanager.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "barber_working_hours", schema = "public",
        uniqueConstraints = @UniqueConstraint(columnNames = {"barber_id", "day_of_week"}))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BarberWorkingHourEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "barber_id", nullable = false)
    BarberEntity barber;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    LocalTime endTime;

    @Column(name = "break_start_time")
    LocalTime breakStartTime;

    @Column(name = "break_end_time")
    LocalTime breakEndTime;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @PrePersist
    @PreUpdate
    private void validate() {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
        boolean breakStartNull = (breakStartTime == null);
        boolean breakEndNull = (breakEndTime == null);
        if (breakStartNull != breakEndNull) {
            throw new IllegalArgumentException("breakStartTime and breakEndTime must be both null or both non-null");
        }
        if (breakStartTime != null) {
            if (!breakStartTime.isBefore(breakEndTime)) {
                throw new IllegalArgumentException("breakStartTime must be before breakEndTime");
            }
            if (breakStartTime.isBefore(startTime) || breakEndTime.isAfter(endTime)) {
                throw new IllegalArgumentException("break must be within working hours");
            }
        }
    }
}