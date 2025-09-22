package com.bookapi.book_api.controller;

import com.bookapi.book_api.controller.generated.ReservationsApi;
import com.bookapi.book_api.dto.generated.ReservationOutput;
import com.bookapi.book_api.mapper.ReservationMapper;
import com.bookapi.book_api.model.Reservation;
import com.bookapi.book_api.service.ReservationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.stream.Collectors;

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
        // Call the service function
        Reservation cancelledReservation = reservationService.deleteReservationById(bookId, reservationId);
        // TODO: Log the cancelled Reservation once logging is set up
        // Return 204 No Content
        return ResponseEntity.noContent().build();
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
        // Manually construct the Pageable object for now
        // TODO: Refactor this endpoint to align with proper pagination practices.
        // The current openapi.yml contract is flawed:
        // 1. It's missing pagination parameters (e.g., offset, limit). We are using hardcoded defaults.
        // 2. The response should be a structured object (like BookListResponse), not a raw list.
        // 3. The `userId` parameter is currently ignored for non-admin users.
        // This should be addressed when the full security model is implemented.
        final int page = 0;
        final int size = 20;
        Pageable pageable = PageRequest.of(page, size);

        // Get the username from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();

        // Call the service function
        Page<Reservation> reservationPage = reservationService.findReservationsForUser(currentUserName, pageable);

        // Map the Pageable object to a list
        List <ReservationOutput> dtoList = reservationPage.getContent().stream()
                .map(reservationMapper::toReservationOutput)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
}

