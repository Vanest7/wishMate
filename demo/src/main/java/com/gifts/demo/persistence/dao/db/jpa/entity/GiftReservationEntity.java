package com.gifts.demo.persistence.dao.db.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "reservas_regalos")
public class GiftReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_reserva", nullable = false)
    private UserEntity user;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "id_deseo",
            nullable = false,
            unique = true
    )
    private WishEntity wish;

    @Column(
            name = "fecha_reserva",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime reservedAt;
}