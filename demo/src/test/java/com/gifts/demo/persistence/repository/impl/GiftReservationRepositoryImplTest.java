package com.gifts.demo.persistence.repository.impl;

import com.gifts.demo.common.exception.ResourceAlreadyExistsException;
import com.gifts.demo.domain.model.GiftReservation;
import com.gifts.demo.domain.model.Wish;
import com.gifts.demo.persistence.dao.db.GiftReservationDaoDb;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiftReservationRepositoryImplTest {

    @Mock
    private GiftReservationDaoDb giftReservationDaoDb;

    private GiftReservationRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new GiftReservationRepositoryImpl(
                giftReservationDaoDb
        );
    }

    @Test
    void shouldReturnSavedReservationIncludingGeneratedDate() {
        GiftReservation input = createReservation();

        LocalDateTime reservedAt =
                LocalDateTime.of(2026, 10, 2, 12, 30);

        GiftReservation saved = GiftReservation.builder()
                .id(7)
                .wish(input.getWish())
                .reservedAt(reservedAt)
                .build();

        when(giftReservationDaoDb.save(input)).thenReturn(saved);

        GiftReservation result = repository.save(input);

        assertThat(result).isSameAs(saved);
        assertThat(result.getId()).isEqualTo(7);
        assertThat(result.getReservedAt()).isEqualTo(reservedAt);

        verify(giftReservationDaoDb).save(input);
    }

    @Test
    void shouldTranslateDuplicateWishConstraintToBusinessConflict() {
        GiftReservation input = createReservation();

        ConstraintViolationException constraintException =
                new ConstraintViolationException(
                        "Duplicate wish reservation",
                        new SQLException(
                                "Duplicate entry",
                                "23000",
                                1062
                        ),
                        "uk_regalos_deseo"
                );

        DataIntegrityViolationException persistenceException =
                new DataIntegrityViolationException(
                        "Could not save reservation",
                        constraintException
                );

        when(giftReservationDaoDb.save(input))
                .thenThrow(persistenceException);

        assertThatThrownBy(() -> repository.save(input))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("6");
    }

    @Test
    void shouldPreserveViolationsOfOtherConstraints() {
        GiftReservation input = createReservation();

        ConstraintViolationException constraintException =
                new ConstraintViolationException(
                        "Another constraint failed",
                        new SQLException(
                                "Integrity constraint violation",
                                "23000"
                        ),
                        "unrelated_test_constraint"
                );

        DataIntegrityViolationException persistenceException =
                new DataIntegrityViolationException(
                        "Could not save reservation",
                        constraintException
                );

        when(giftReservationDaoDb.save(input))
                .thenThrow(persistenceException);

        assertThatThrownBy(() -> repository.save(input))
                .isSameAs(persistenceException);
    }

    @Test
    void shouldPreserveIntegrityErrorsWithoutKnownConstraint() {
        GiftReservation input = createReservation();

        DataIntegrityViolationException persistenceException =
                new DataIntegrityViolationException(
                        "Unclassified integrity error"
                );

        when(giftReservationDaoDb.save(input))
                .thenThrow(persistenceException);

        assertThatThrownBy(() -> repository.save(input))
                .isSameAs(persistenceException);
    }

    private GiftReservation createReservation() {
        return GiftReservation.builder()
                .wish(Wish.builder().id(6).build())
                .build();
    }
}