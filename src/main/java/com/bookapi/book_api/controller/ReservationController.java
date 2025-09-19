package com.bookapi.book_api.controller;

import com.bookapi.book_api.controller.generated.ReservationsApi;
import com.bookapi.book_api.dto.generated.ReservationOutput;
import com.bookapi.book_api.mapper.ReservationMapper;
import com.bookapi.book_api.model.Reservation;
import com.bookapi.book_api.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
public class ReservationController implements ReservationsApi {

    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;

    public ReservationController (ReservationService reservationService, ReservationMapper reservationMapper) {
        this.reservationService = reservationService;
        this.reservationMapper = reservationMapper;
    }

    @Override
    public ResponseEntity<ReservationOutput> createReservation(
            UUID bookId
            ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String currentUser = authentication.getName();

        // Call the ReservationService method for creating a reservation
        Reservation createdReservation = reservationService.addReservation(bookId, currentUser);
        // Build the URI to be returned with the 201 response
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdReservation.getId())
                .toUri();
        // Use the mapper to convert it to a public DTO
        ReservationOutput reservationOutput = reservationMapper.toReservationOutput(createdReservation);
        // Return the DTO with a 201 Created Status
        return ResponseEntity.created(location).body(reservationOutput);
    }

    @Override
    public ResponseEntity<ReservationOutput> cancelReservationById(UUID bookId, UUID reservationId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<ReservationOutput> getReservationById(UUID bookId, UUID reservationId) {
        // Call the service function
        Reservation userReservation = reservationService.findReservationById(bookId, reservationId);

        // Map the reservation to a public DTO
        ReservationOutput reservationOutput = reservationMapper.toReservationOutput(userReservation);
        // Return the DTO with a 200 OK Status
        return ResponseEntity.ok(reservationOutput);
    }

    @Override
    public ResponseEntity<List<ReservationOutput>> listReservations(UUID userId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}

