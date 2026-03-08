package com.nurlansuleymanli.salonmanager.model.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "salon_working_hours", schema = "public",
        uniqueConstraints = @UniqueConstraint(columnNames = {"salon_id", "day_of_week"}))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SalonWorkingHourEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salon_id", nullable = false)
    SalonEntity salonEntity;

    @Column(name = "start_time")
    LocalTime startTime;

    @Column(name = "end_time")
    LocalTime endTime;

    @Column(name = "is_closed", nullable = false)
    boolean isClosed;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    DayOfWeek dayOfWeek;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;


    @PrePersist
    @PreUpdate
    private void validate() {
        if (isClosed) {
            startTime = null;
            endTime = null;
            return;
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Open day must have startTime and endTime");
        }
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
    }

}
