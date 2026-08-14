package com.luisramirez.booknest.book;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record BookRequest(
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255)
    String title,

    @NotBlank(message = "El ISBN es obligatorio")
    String isbn,

    Integer publishedYear,

    @NotNull(message = "El autor es obligatorio")
    Long authorId,

    Set<Long> categoryIds,

    @Min(value = 1, message = "Debe haber al menos 1 ejemplar")
    int totalCopies
) {
}
