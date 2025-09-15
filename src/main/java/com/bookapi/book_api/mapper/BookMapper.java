package com.bookapi.book_api.mapper;


import com.bookapi.book_api.dto.generated.BookListResponse;
import com.bookapi.book_api.dto.generated.BookOutput;
import com.bookapi.book_api.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookMapper {

    public BookOutput toBookOutput(Book book) {
        if (book == null) {
            return null;
        }

        BookOutput dto = new BookOutput();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setSynopsis(book.getSynopsis());
        return dto;
    }

    public BookListResponse toBookListResponse(Page<Book> bookPage) {
        // Convert the list of Book models to a list of BookOutput DTOs
        List<BookOutput> bookOutputs = bookPage.getContent().stream()
                .map(this::toBookOutput)
                .collect(Collectors.toList());

        // Create the final response DTO
        BookListResponse response = new BookListResponse();
        response.setItems(bookOutputs);
        response.setTotalCount((int) bookPage.getTotalElements());
        response.setOffset((int) bookPage.getPageable().getOffset());
        response.setLimit(bookPage.getPageable().getPageSize());
        return response;
    }
}

