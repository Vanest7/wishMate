package com.gifts.demo.persistence.repository.impl;

import com.gifts.demo.common.exception.ResourceAlreadyExistsException;
import com.gifts.demo.domain.model.GiftReservation;
import com.gifts.demo.domain.repository.GiftReservationRepository;
import com.gifts.demo.persistence.dao.db.GiftReservationDaoDb;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GiftReservationRepositoryImpl
        implements GiftReservationRepository {

    private static final String WISH_UNIQUE_CONSTRAINT =
            "uk_regalos_deseo";

    private final GiftReservationDaoDb giftReservationDaoDb;

    @Override
    public Optional<GiftReservation> findById(Integer id) {
        return giftReservationDaoDb.findById(id);
    }

    @Override
    public Optional<GiftReservation> findByWishId(
            Integer wishId
    ) {
        return giftReservationDaoDb.findByWishId(wishId);
    }

    @Override
    public Optional<GiftReservation> findByWishIdAndUserId(
            Integer wishId,
            Integer userId
    ) {
        return giftReservationDaoDb.findByWishIdAndUserId(
                wishId,
                userId
        );
    }

    @Override
    public List<GiftReservation> findByUserId(Integer userId) {
        return giftReservationDaoDb.findByUserId(userId);
    }

    @Override
    public boolean existsByWishId(Integer wishId) {
        return giftReservationDaoDb.existsByWishId(wishId);
    }

    @Override
    public GiftReservation save(
            GiftReservation giftReservation
    ) {
        try {
            return giftReservationDaoDb.save(
                    giftReservation
            );
        } catch (DataIntegrityViolationException exception) {
            if (isWishAlreadyReserved(exception)) {
                throw new ResourceAlreadyExistsException(
                        "Wish "
                                + giftReservation.getWish().getId()
                                + " is already reserved"
                );
            }

            throw exception;
        }
    }

    @Override
    public void delete(Integer id) {
        giftReservationDaoDb.delete(id);
    }

    private boolean isWishAlreadyReserved(
            Throwable throwable
    ) {
        Throwable current = throwable;

        while (current != null) {
            if (current instanceof ConstraintViolationException constraintException
                    && WISH_UNIQUE_CONSTRAINT.equalsIgnoreCase(
                    constraintException.getConstraintName()
            )) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}