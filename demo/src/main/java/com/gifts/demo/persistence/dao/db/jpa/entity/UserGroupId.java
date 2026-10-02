package com.gifts.demo.persistence.dao.db.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class UserGroupId implements Serializable {

    private static final long serialVersionUID = 2758786773264508452L;

    @Column(name = "id_grupo", nullable = false)
    private Integer groupId;

    @Column(name = "id_usuario", nullable = false)
    private Integer userId;

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        UserGroupId that = (UserGroupId) object;

        return Objects.equals(groupId, that.groupId)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, userId);
    }
}