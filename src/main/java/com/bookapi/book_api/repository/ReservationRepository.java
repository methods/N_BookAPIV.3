package com.bookapi.book_api.repository;

import com.bookapi.book_api.model.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReservationRepository extends MongoRepository<Reservation, UUID> {
    Page<Reservation> findByUserName(String userName, Pageable pageable);
}
