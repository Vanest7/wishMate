package com.gifts.demo.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gifts.demo.controller.mapper.GiftReservationWebMapper;
import com.gifts.demo.controller.webmodel.reservation.GiftReservationResponse;
import com.gifts.demo.domain.model.GiftReservation;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.model.Wish;
import com.gifts.demo.persistence.dao.db.jpa.entity.GiftReservationEntity;
import com.gifts.demo.persistence.dao.db.jpa.mapper.GiftReservationJpaMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GiftReservationMapperTest {

    private static final LocalDateTime RESERVED_AT =
            LocalDateTime.of(2026, 10, 2, 12, 30);

    private final ObjectMapper objectMapper =
            new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldPreserveReservedAtWhenMappingEntityToDomain() {
        GiftReservationEntity entity = new GiftReservationEntity();
        entity.setId(7);
        entity.setReservedAt(RESERVED_AT);

        GiftReservation reservation =
                GiftReservationJpaMapper.INSTANCE
                        .toGiftReservation(entity);

        assertThat(reservation.getId()).isEqualTo(7);
        assertThat(reservation.getReservedAt()).isEqualTo(RESERVED_AT);
    }

    @Test
    void shouldPreserveReservationIdWishIdAndDateInResponse() {
        GiftReservation reservation = createReservation();

        GiftReservationResponse response =
                GiftReservationWebMapper.toResponse(reservation);

        assertThat(response.id()).isEqualTo(7);
        assertThat(response.wishId()).isEqualTo(6);
        assertThat(response.reservedAt()).isEqualTo(RESERVED_AT);
    }

    @Test
    void shouldExposeOnlyAllowedFieldsInReservationResponse() {
        GiftReservation reservation = createReservation();

        GiftReservationResponse response =
                GiftReservationWebMapper.toResponse(reservation);

        JsonNode json = objectMapper.valueToTree(response);

        Set<String> fieldNames = new HashSet<>();
        json.fieldNames().forEachRemaining(fieldNames::add);

        assertThat(fieldNames).containsExactlyInAnyOrder(
                "id",
                "wishId",
                "reservedAt"
        );

        assertThat(json.toString())
                .doesNotContain("private@example.test")
                .doesNotContain("test-password-hash");
    }

    private GiftReservation createReservation() {
        User reserver = User.builder()
                .id(20)
                .userName("Test user")
                .email("private@example.test")
                .passwordHash("test-password-hash")
                .build();

        Wish wish = Wish.builder()
                .id(6)
                .name("Headphones")
                .build();

        return GiftReservation.builder()
                .id(7)
                .user(reserver)
                .wish(wish)
                .reservedAt(RESERVED_AT)
                .build();
    }
}