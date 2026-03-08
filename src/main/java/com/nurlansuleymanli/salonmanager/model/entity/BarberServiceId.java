package com.nurlansuleymanli.salonmanager.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class BarberServiceId implements Serializable {
    @Column(name = "barber_id")
    Long barberId;

    @Column(name = "service_id")
    Long serviceId;
}
