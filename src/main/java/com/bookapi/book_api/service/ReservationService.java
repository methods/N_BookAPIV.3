package com.bookapi.book_api.service;

import com.bookapi.book_api.model.Reservation;
import com.bookapi.book_api.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation addReservation(UUID bookId, String userName) {
        // Create a new Reservation from the arguments
        Reservation newReservation = new Reservation(
                bookId,
                userName
        );
        // Use the repository's built in methods to save and return the new Reservation
        return reservationRepository.save(newReservation);
    }
}
