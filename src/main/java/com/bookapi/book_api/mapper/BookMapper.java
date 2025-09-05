package com.bookapi.book_api.mapper;


import com.bookapi.book_api.dto.generated.BookOutput;
import com.bookapi.book_api.model.Book;
import org.springframework.stereotype.Component;

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
}
