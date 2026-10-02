package com.gifts.demo.domain.service.impl;

import com.gifts.demo.common.annotation.DomainService;
import com.gifts.demo.common.annotation.DomainTransactional;
import com.gifts.demo.common.exception.ForbiddenOperationException;
import com.gifts.demo.common.exception.ResourceNotFoundException;
import com.gifts.demo.common.exception.ValidationException;
import com.gifts.demo.domain.model.Event;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.model.Wish;
import com.gifts.demo.domain.repository.EventRepository;
import com.gifts.demo.domain.repository.UserRepository;
import com.gifts.demo.domain.repository.WishRepository;
import com.gifts.demo.domain.service.EventStateService;
import com.gifts.demo.domain.service.GroupPermissionService;
import com.gifts.demo.domain.service.WishService;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@DomainService
@RequiredArgsConstructor
public class WishServiceImpl implements WishService {

    private final WishRepository wishRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final GroupPermissionService groupPermissionService;
    private final EventStateService eventStateService;

    @Override
    public Wish getById(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId
    ) {
        getEvent(groupId, eventId);

        groupPermissionService.requireMembership(
                groupId,
                requesterId
        );

        return getWish(eventId, wishId);
    }

    @Override
    public List<Wish> getByEventId(
            Integer groupId,
            Integer eventId,
            Integer requesterId
    ) {
        getEvent(groupId, eventId);

        groupPermissionService.requireMembership(
                groupId,
                requesterId
        );

        return wishRepository.findByEventId(eventId);
    }

    @Override
    public List<Wish> getByUserIdAndEventId(
            Integer groupId,
            Integer eventId,
            Integer requesterId,
            Integer userId
    ) {
        getEvent(groupId, eventId);

        groupPermissionService.requireMembership(
                groupId,
                requesterId
        );

        groupPermissionService.requireMembership(
                groupId,
                userId
        );

        return wishRepository.findByUserIdAndEventId(
                userId,
                eventId
        );
    }

    @Override
    @DomainTransactional
    public Wish create(
            Integer groupId,
            Integer eventId,
            Integer requesterId,
            Wish wish
    ) {
        validateWish(wish);

        Event event = getEvent(groupId, eventId);

        groupPermissionService.requireMembership(
                groupId,
                requesterId
        );

        eventStateService.requireWishModificationAllowed(event);

        User user = getUser(requesterId);

        wish.setId(null);
        wish.setUser(user);
        wish.setEvent(event);

        return wishRepository.save(wish);
    }

    @Override
    @DomainTransactional
    public Wish update(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId,
            Wish wish
    ) {
        validateWish(wish);

        Event event = getEvent(groupId, eventId);

        groupPermissionService.requireMembership(
                groupId,
                requesterId
        );

        eventStateService.requireWishModificationAllowed(event);

        Wish currentWish = getWish(eventId, wishId);

        requireWishOwner(
                currentWish,
                requesterId
        );

        currentWish.setName(
                wish.getName()
        );
        currentWish.setDescription(
                wish.getDescription()
        );
        currentWish.setPurchaseUrl(
                wish.getPurchaseUrl()
        );
        currentWish.setEstimatedPrice(
                wish.getEstimatedPrice()
        );

        return wishRepository.save(currentWish);
    }

    @Override
    @DomainTransactional
    public void delete(
            Integer groupId,
            Integer eventId,
            Integer wishId,
            Integer requesterId
    ) {
        Event event = getEvent(groupId, eventId);

        groupPermissionService.requireMembership(
                groupId,
                requesterId
        );

        eventStateService.requireWishModificationAllowed(event);

        Wish wish = getWish(eventId, wishId);

        requireWishOwner(wish, requesterId);

        wishRepository.delete(wishId);
    }

    private Event getEvent(
            Integer groupId,
            Integer eventId
    ) {
        return eventRepository
                .findByIdAndGroupId(eventId, groupId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Event " + eventId
                                        + " not found in group "
                                        + groupId
                        )
                );
    }

    private Wish getWish(
            Integer eventId,
            Integer wishId
    ) {
        Wish wish = wishRepository.findById(wishId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Wish with id " + wishId
                        )
                );

        if (wish.getEvent() == null
                || !eventId.equals(
                wish.getEvent().getId()
        )) {
            throw new ResourceNotFoundException(
                    "Wish " + wishId
                            + " not found in event "
                            + eventId
            );
        }

        return wish;
    }

    private User getUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User with id " + userId
                        )
                );
    }

    private void requireWishOwner(
            Wish wish,
            Integer userId
    ) {
        if (!userId.equals(wish.getUser().getId())) {
            throw new ForbiddenOperationException(
                    "User " + userId
                            + " cannot modify wish "
                            + wish.getId()
            );
        }
    }

    private void validateWish(Wish wish) {
        if (wish == null) {
            throw new ValidationException(
                    "Wish cannot be null"
            );
        }

        String name = wish.getName();

        if (name == null || name.isBlank()) {
            throw new ValidationException(
                    "Wish name cannot be empty"
            );
        }

        if (name.length() > 150) {
            throw new ValidationException(
                    "Wish name must not exceed 150 characters"
            );
        }

        String description = wish.getDescription();

        if (description == null || description.isBlank()) {
            throw new ValidationException(
                    "Wish description cannot be empty"
            );
        }

        if (description.length() > 255) {
            throw new ValidationException(
                    "Wish description must not exceed 255 characters"
            );
        }

        String purchaseUrl = wish.getPurchaseUrl();

        if (purchaseUrl != null && purchaseUrl.length() > 2048) {
            throw new ValidationException(
                    "Purchase URL must not exceed 2048 characters"
            );
        }

        BigDecimal estimatedPrice = wish.getEstimatedPrice();

        if (estimatedPrice != null) {
            if (estimatedPrice.signum() < 0) {
                throw new ValidationException(
                        "Estimated price cannot be negative"
                );
            }

            if (estimatedPrice.compareTo(
                    new BigDecimal("99999999.99")
            ) > 0 || estimatedPrice.scale() > 2) {
                throw new ValidationException(
                        "Estimated price must have at most 8 integer digits and 2 decimal places"
                );
            }
        }

        wish.setName(name.strip());
        wish.setDescription(description.strip());
    }
}