package com.nurlansuleymanli.salonmanager.model.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.Instant;



@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "salons", schema = "public")
public class SalonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank
    @Size(max = 120)
    @Column(name = "name", nullable = false, length = 120, unique = true)
    String name;

    @NotBlank
    @Column(name = "address" , nullable = false)
    String address;

    @NotBlank
    @Column(name = "contact_phone", nullable = false, length = 20)
    @Pattern(regexp = "^\\+994(10|50|51|55|70|77|99)\\d{7}$")
    String contactPhone;

    @Email
    @NotBlank
    @Column(name = "contact_email", nullable = false)
    String contactEmail;

    @Column(name = "is_active", nullable = false)
    boolean isActive= true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;


    public SalonEntity(String name, String address, String contactPhone, String contactEmail) {
        this.name = name;
        this.address = address;
        this.contactPhone = contactPhone;
        this.contactEmail = normalizeEmail(contactEmail);
    }

    public SalonEntity() {}

    @PrePersist
    @PreUpdate
    void normalize() {this.contactEmail = normalizeEmail(this.contactEmail);}

    private static String normalizeEmail(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }




}
