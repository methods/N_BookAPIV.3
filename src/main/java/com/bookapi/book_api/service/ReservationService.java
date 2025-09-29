package com.bookapi.book_api.service;

import com.bookapi.book_api.exception.BadResourceException;
import com.bookapi.book_api.exception.ResourceNotFoundException;
import com.bookapi.book_api.model.Reservation;
import com.bookapi.book_api.repository.ReservationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation addReservation(UUID bookId, UUID userId) {
        // Create a new Reservation from the arguments
        Reservation newReservation = new Reservation(
                bookId,
                userId
        );
        // Use the repository's built in methods to save and return the new Reservation
        return reservationRepository.save(newReservation);
    }

    public Reservation findReservationById(UUID bookId, UUID reservationId) {
        // Use the repository's built in methods to return the Reservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id:" + reservationId));

        if (!reservation.getBookId().equals(bookId)) {
            throw new BadResourceException("The reservation with id " + reservationId + " does not belong to the book with id " + bookId);
        }
        // TODO: Add authorization check. A user should only be able to see their own
        //       reservations unless they are an admin. This will be implemented as part
        //       of the security hardening feature.

        return reservation;
    }

    public Reservation deleteReservationById(UUID bookId, UUID reservationId) {
        // Find the reservation to be deleted
        Reservation reservationToDelete = findReservationById(bookId, reservationId);
        // Delete the reservation from the database
        reservationRepository.delete(reservationToDelete);

        return reservationToDelete;
    }

    public Page<Reservation> findReservationsForUser(UUID userId, Pageable pageable) {
        return reservationRepository.findByUserId(userId, pageable);
    }
}
