package com.luisramirez.booknest.loan;

import com.luisramirez.booknest.book.Book;
import com.luisramirez.booknest.book.BookRepository;
import com.luisramirez.booknest.exception.ConflictException;
import com.luisramirez.booknest.exception.ResourceNotFoundException;
import com.luisramirez.booknest.security.AppUserDetails;
import com.luisramirez.booknest.user.User;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;

    private static final int LOAN_DAYS = 14;

    public LoanService(LoanRepository loanRepository, BookRepository bookRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public LoanResponse borrow(AppUserDetails principal, LoanRequest request) {
        User user = principal.user();
        Book book = bookRepository.findById(request.bookId())
            .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado: " + request.bookId()));

        boolean alreadyBorrowed = loanRepository.findActiveByUser(user).stream()
            .anyMatch(loan -> loan.getBook().getId().equals(book.getId()));
        if (alreadyBorrowed) {
            throw new ConflictException("Ya tienes este libro prestado");
        }

        if (book.getAvailableCopies() <= 0) {
            throw new ConflictException("No hay ejemplares disponibles de este libro");
        }

        LocalDate today = LocalDate.now();
        Loan loan = new Loan(user, book, today, today.plusDays(LOAN_DAYS));
        loanRepository.save(loan);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        return LoanResponse.from(loan);
    }

    @Transactional
    public LoanResponse returnBook(AppUserDetails principal, Long loanId) {
        User user = principal.user();
        Loan loan = loanRepository.findByIdAndUser(loanId, user)
            .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado: " + loanId));

        if (loan.getReturnDate() != null) {
            throw new ConflictException("Este libro ya fue devuelto");
        }

        loan.setReturnDate(LocalDate.now());
        loanRepository.save(loan);

        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        return LoanResponse.from(loan);
    }

    public List<LoanResponse> myLoans(AppUserDetails principal) {
        return loanRepository.findByUserOrderByLoanDateDesc(principal.user()).stream()
            .map(LoanResponse::from)
            .toList();
    }
}
