package com.luisramirez.booknest.loan;

import jakarta.validation.constraints.NotNull;

public record LoanRequest(
    @NotNull(message = "El libro es obligatorio")
    Long bookId
) {
}
