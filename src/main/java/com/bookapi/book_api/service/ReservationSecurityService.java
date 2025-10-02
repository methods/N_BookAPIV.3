package com.bookapi.book_api.service;


import com.bookapi.book_api.model.CustomOAuth2User;
import com.bookapi.book_api.model.Reservation;
import com.bookapi.book_api.repository.ReservationRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service("reservationSecurityService")
public class ReservationSecurityService {

    private final ReservationRepository reservationRepository;

    public ReservationSecurityService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public boolean isReservationOwner(Authentication authentication, UUID reservationId) {
        // Get the database ID of the current logged in user
        CustomOAuth2User currentUser = (CustomOAuth2User) authentication.getPrincipal();
        UUID currentUserId = currentUser.getLocalUser().getId();

        // Find the reservation in the database
        Optional<Reservation> currentReservationOptional = reservationRepository.findById(reservationId);
        // Check there is a Reservation in the retrieved Optional
        if (currentReservationOptional.isEmpty()) {
            return false;
        }
        // Retrieve the reservation from the Optional
        Reservation currentReservation = currentReservationOptional.get();
        UUID reservationOwner = currentReservation.getUserId();

        // Check if the userId matches that of the reservation
        return reservationOwner == currentUserId;
    }
}
