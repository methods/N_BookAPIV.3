package com.bookapi.book_api.mapper;


import com.bookapi.book_api.dto.generated.ReservationListResponse;
import com.bookapi.book_api.dto.generated.ReservationOutput;
import com.bookapi.book_api.model.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReservationMapper {

    public ReservationOutput toReservationOutput(Reservation reservation) {
        if (reservation == null) {
            return null;
        }

        ReservationOutput dto = new ReservationOutput();
        dto.setId(reservation.getId());
        dto.setBookId(reservation.getBookId());
        dto.setUserId(reservation.getUserId());
        dto.setState(reservation.getState());
        dto.setReservedAt(reservation.getReservedAt().atOffset(ZoneOffset.UTC));
        return dto;
    }

    public ReservationListResponse toReservationListResponse(Page<Reservation> reservationPage) {
        // Convert the list of Reservation models to a list of ReservationOutput DTOs
        List<ReservationOutput> reservationOutputs = reservationPage.getContent().stream()
                .map(this::toReservationOutput)
                .collect(Collectors.toList());

        // Create the final response DTO
        ReservationListResponse response = new ReservationListResponse();
        response.setItems(reservationOutputs);
        response.setTotalCount((int) reservationPage.getTotalElements());
        response.setOffset((int) reservationPage.getPageable().getOffset());
        response.setLimit(reservationPage.getPageable().getPageSize());
        return response;
    }
}
