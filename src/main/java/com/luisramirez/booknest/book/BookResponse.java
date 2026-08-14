package com.luisramirez.booknest.book;

import com.luisramirez.booknest.author.Author;
import com.luisramirez.booknest.category.Category;
import java.util.Set;
import java.util.stream.Collectors;

public record BookResponse(
    Long id,
    String title,
    String isbn,
    Integer publishedYear,
    Long authorId,
    String authorName,
    Set<String> categories,
    int totalCopies,
    int availableCopies
) {

    public static BookResponse from(Book book) {
        return new BookResponse(
            book.getId(),
            book.getTitle(),
            book.getIsbn(),
            book.getPublishedYear(),
            book.getAuthor().getId(),
            book.getAuthor().getName(),
            book.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.toSet()),
            book.getTotalCopies(),
            book.getAvailableCopies()
        );
    }
}
