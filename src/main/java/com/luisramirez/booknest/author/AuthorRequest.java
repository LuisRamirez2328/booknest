package com.luisramirez.booknest.author;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255)
    String name,

    @Size(max = 1000)
    String bio
) {
}
