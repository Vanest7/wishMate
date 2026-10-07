package com.gifts.demo.persistence.dao.db.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "usuarios",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_usuarios_username",
                columnNames = "username"
        )
)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String userName;

    @Column(name = "username", length = 30)
    private String handle;

    @Column(name = "mail", nullable = false, unique = true, length = 150)
    private String mail;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
}