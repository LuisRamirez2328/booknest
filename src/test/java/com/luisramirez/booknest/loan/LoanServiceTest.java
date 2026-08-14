package com.luisramirez.booknest.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luisramirez.booknest.book.Book;
import com.luisramirez.booknest.book.BookRepository;
import com.luisramirez.booknest.exception.ConflictException;
import com.luisramirez.booknest.exception.ResourceNotFoundException;
import com.luisramirez.booknest.security.AppUserDetails;
import com.luisramirez.booknest.user.Role;
import com.luisramirez.booknest.user.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private LoanService loanService;

    private User user;
    private AppUserDetails principal;
    private Book book;

    @BeforeEach
    void setUp() {
        user = new User("Ana Pérez", "ana@example.com", "hash", Role.USER);
        principal = new AppUserDetails(user);
        book = new Book();
        ReflectionTestUtils.setField(book, "id", 1L);
        book.setTitle("Cien años de soledad");
        book.setAvailableCopies(2);
    }

    @Test
    void borrowDecrementsAvailableCopies() {
        when(loanRepository.findActiveByUser(user)).thenReturn(List.of());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        LoanResponse response = loanService.borrow(principal, new LoanRequest(1L));

        assertThat(response.bookTitle()).isEqualTo("Cien años de soledad");
        assertThat(response.active()).isTrue();
        assertThat(book.getAvailableCopies()).isEqualTo(1);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void borrowRejectsWhenAlreadyBorrowed() {
        Loan existing = new Loan(user, book, null, null);
        when(loanRepository.findActiveByUser(user)).thenReturn(List.of(existing));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> loanService.borrow(principal, new LoanRequest(1L)))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("prestado");

        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void borrowRejectsWhenNoCopiesAvailable() {
        book.setAvailableCopies(0);
        when(loanRepository.findActiveByUser(user)).thenReturn(List.of());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> loanService.borrow(principal, new LoanRequest(1L)))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("ejemplares");
    }

    @Test
    void borrowThrowsWhenBookMissing() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.borrow(principal, new LoanRequest(99L)))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void returnBookRestoresCopy() {
        Loan loan = new Loan(user, book, null, null);
        when(loanRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(loan));

        LoanResponse response = loanService.returnBook(principal, 1L);

        assertThat(response.active()).isFalse();
        assertThat(loan.getReturnDate()).isNotNull();
        assertThat(book.getAvailableCopies()).isEqualTo(3);
    }

    @Test
    void returnBookRejectsWhenAlreadyReturned() {
        Loan loan = new Loan(user, book, null, null);
        loan.setReturnDate(java.time.LocalDate.now());
        when(loanRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.returnBook(principal, 1L))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("devuelto");
    }

    @Test
    void returnBookThrowsWhenLoanNotOwned() {
        when(loanRepository.findByIdAndUser(eq(1L), eq(user))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.returnBook(principal, 1L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void myLoansReturnsOnlyOwnLoans() {
        Loan loan = new Loan(user, book, java.time.LocalDate.now(), java.time.LocalDate.now().plusDays(14));
        when(loanRepository.findByUserOrderByLoanDateDesc(user)).thenReturn(List.of(loan));

        List<LoanResponse> loans = loanService.myLoans(principal);

        assertThat(loans).hasSize(1);
        assertThat(loans.get(0).userEmail()).isEqualTo("ana@example.com");
    }
}
