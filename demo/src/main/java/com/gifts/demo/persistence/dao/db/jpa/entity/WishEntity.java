package com.gifts.demo.persistence.dao.db.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "deseos")
public class WishEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evento")
    private EventEntity event;

    @Column(name = "descripcion", nullable = false, length = 255)
    private String description;

    @Column(name = "url_compra", length = 2048)
    private String purchaseUrl;

    @Column(name = "precio_estimado", precision = 10, scale = 2)
    private BigDecimal estimatedPrice;
}