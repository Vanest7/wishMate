package com.gifts.demo.mapper;

import com.gifts.demo.domain.model.Event;
import com.gifts.demo.domain.model.EventStatus;
import com.gifts.demo.domain.model.Group;
import com.gifts.demo.domain.model.GroupMember;
import com.gifts.demo.domain.model.GroupRole;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.model.Wish;
import com.gifts.demo.persistence.dao.db.jpa.entity.EventEntity;
import com.gifts.demo.persistence.dao.db.jpa.entity.GroupEntity;
import com.gifts.demo.persistence.dao.db.jpa.entity.UserEntity;
import com.gifts.demo.persistence.dao.db.jpa.entity.UserGroupEntity;
import com.gifts.demo.persistence.dao.db.jpa.entity.WishEntity;
import com.gifts.demo.persistence.dao.db.jpa.mapper.EventJpaMapper;
import com.gifts.demo.persistence.dao.db.jpa.mapper.GroupJpaMapper;
import com.gifts.demo.persistence.dao.db.jpa.mapper.UserGroupJpaMapper;
import com.gifts.demo.persistence.dao.db.jpa.mapper.UserJpaMapper;
import com.gifts.demo.persistence.dao.db.jpa.mapper.WishJpaMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class JpaMappersTest {

    @Test
    void shouldMapEmailToMailAndPreserveUserFieldsInBothDirections() {
        User original = createUser();

        UserEntity entity =
                UserJpaMapper.INSTANCE.toUserEntity(original);

        assertThat(entity.getId()).isEqualTo(20);
        assertThat(entity.getUserName()).isEqualTo("Test user");
        assertThat(entity.getHandle()).isEqualTo("test.user");
        assertThat(entity.getMail()).isEqualTo("user@example.test");
        assertThat(entity.getPasswordHash()).isEqualTo("test-hash");

        User restored = UserJpaMapper.INSTANCE.toUser(entity);

        assertThat(restored)
                .usingRecursiveComparison()
                .isEqualTo(original);
    }

    @Test
    void shouldPreserveGroupNameAndCreatorInBothDirections() {
        Group original = createGroup();

        GroupEntity entity =
                GroupJpaMapper.INSTANCE.toGroupEntity(original);

        assertThat(entity.getId()).isEqualTo(1);
        assertThat(entity.getName()).isEqualTo("Family");
        assertThat(entity.getCreator().getId()).isEqualTo(20);
        assertThat(entity.getCreator().getMail())
                .isEqualTo("user@example.test");

        Group restored = GroupJpaMapper.INSTANCE.toGroup(entity);

        assertThat(restored)
                .usingRecursiveComparison()
                .isEqualTo(original);
    }

    @Test
    void shouldPreserveEventDatesStateAndRelationshipsInBothDirections() {
        Event original = createEvent();

        EventEntity entity =
                EventJpaMapper.INSTANCE.toEventEntity(original);

        assertThat(entity.getId()).isEqualTo(4);
        assertThat(entity.getGroup().getId()).isEqualTo(1);
        assertThat(entity.getCreator().getId()).isEqualTo(20);
        assertThat(entity.getStatus()).isEqualTo(EventStatus.ACTIVE);
        assertThat(entity.getStartDate())
                .isEqualTo(LocalDate.of(2026, 10, 10));
        assertThat(entity.getEndDate())
                .isEqualTo(LocalDate.of(2026, 10, 11));
        assertThat(entity.getCreatedAt())
                .isEqualTo(original.getCreatedAt());
        assertThat(entity.getUpdatedAt())
                .isEqualTo(original.getUpdatedAt());

        Event restored = EventJpaMapper.INSTANCE.toEvent(entity);

        assertThat(restored)
                .usingRecursiveComparison()
                .isEqualTo(original);
    }

    @Test
    void shouldPreserveWishPriceAndRelationshipsInBothDirections() {
        Wish original = Wish.builder()
                .id(6)
                .user(createUser())
                .event(createEvent())
                .name("Headphones")
                .description("Wireless headphones")
                .purchaseUrl("https://example.test/headphones")
                .estimatedPrice(new BigDecimal("59.90"))
                .build();

        WishEntity entity =
                WishJpaMapper.INSTANCE.toWishEntity(original);

        assertThat(entity.getId()).isEqualTo(6);
        assertThat(entity.getUser().getId()).isEqualTo(20);
        assertThat(entity.getEvent().getId()).isEqualTo(4);
        assertThat(entity.getName()).isEqualTo("Headphones");
        assertThat(entity.getDescription())
                .isEqualTo("Wireless headphones");
        assertThat(entity.getPurchaseUrl())
                .isEqualTo("https://example.test/headphones");
        assertThat(entity.getEstimatedPrice())
                .isEqualByComparingTo("59.90");

        Wish restored = WishJpaMapper.INSTANCE.toWish(entity);

        assertThat(restored)
                .usingRecursiveComparison()
                .isEqualTo(original);
    }

    @Test
    void shouldPreserveMembershipRoleDateAndRelationships() {
        GroupMember original = GroupMember.builder()
                .group(createGroup())
                .user(createUser())
                .role(GroupRole.ADMIN)
                .joinedAt(LocalDateTime.of(2026, 10, 2, 10, 0))
                .build();

        UserGroupEntity entity =
                UserGroupJpaMapper.INSTANCE
                        .toUserGroupEntity(original);

        assertThat(entity.getGroup().getId()).isEqualTo(1);
        assertThat(entity.getUser().getId()).isEqualTo(20);
        assertThat(entity.getRole()).isEqualTo(GroupRole.ADMIN);
        assertThat(entity.getJoinedAt()).isEqualTo(original.getJoinedAt());

        GroupMember restored =
                UserGroupJpaMapper.INSTANCE.toGroupMember(entity);

        assertThat(restored)
                .usingRecursiveComparison()
                .isEqualTo(original);
    }

    private User createUser() {
        return User.builder()
                .id(20)
                .userName("Test user")
                .handle("test.user")
                .email("user@example.test")
                .passwordHash("test-hash")
                .build();
    }

    private Group createGroup() {
        return Group.builder()
                .id(1)
                .name("Family")
                .creator(createUser())
                .build();
    }

    private Event createEvent() {
        return Event.builder()
                .id(4)
                .group(createGroup())
                .creator(createUser())
                .name("Birthday")
                .description("Family celebration")
                .status(EventStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 10, 10))
                .endDate(LocalDate.of(2026, 10, 11))
                .createdAt(LocalDateTime.of(2026, 10, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 10, 2, 11, 0))
                .build();
    }
}