package com.luisramirez.booknest.loan;

import com.luisramirez.booknest.book.Book;
import java.time.LocalDate;

public record LoanResponse(
    Long id,
    Long bookId,
    String bookTitle,
    String userEmail,
    LocalDate loanDate,
    LocalDate dueDate,
    LocalDate returnDate,
    boolean active
) {

    public static LoanResponse from(Loan loan) {
        Book book = loan.getBook();
        return new LoanResponse(
            loan.getId(),
            book.getId(),
            book.getTitle(),
            loan.getUser().getEmail(),
            loan.getLoanDate(),
            loan.getDueDate(),
            loan.getReturnDate(),
            loan.getReturnDate() == null
        );
    }
}
