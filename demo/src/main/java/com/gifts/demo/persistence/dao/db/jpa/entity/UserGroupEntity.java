package com.gifts.demo.persistence.dao.db.jpa.entity;

import com.gifts.demo.domain.model.GroupRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "grupo_usuarios")
public class UserGroupEntity {

    @EmbeddedId
    private UserGroupId id;

    @MapsId("groupId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_grupo", nullable = false)
    private GroupEntity group;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private GroupRole role = GroupRole.MEMBER;

    @Column(name = "fecha_union", nullable = false)
    private LocalDateTime joinedAt;
}