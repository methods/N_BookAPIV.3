package com.bookapi.book_api.mapper;


import com.bookapi.book_api.dto.generated.ReservationOutput;
import com.bookapi.book_api.model.Reservation;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@Component
public class ReservationMapper {

    public ReservationOutput toReservationOutput(Reservation reservation) {
        if (reservation == null) {
            return null;
        }

        ReservationOutput dto = new ReservationOutput();
        dto.setId(reservation.getId());
        dto.setBookId(reservation.getBookId());
        dto.setUserName(reservation.getUserName());
        dto.setState(reservation.getState());
        dto.setReservedAt(reservation.getReservedAt().atOffset(ZoneOffset.UTC));
        return dto;
    }
}
