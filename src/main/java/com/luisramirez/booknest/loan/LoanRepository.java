package com.luisramirez.booknest.loan;

import com.luisramirez.booknest.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUserOrderByLoanDateDesc(User user);

    @Query("SELECT l FROM Loan l WHERE l.user = :user AND l.returnDate IS NULL")
    List<Loan> findActiveByUser(@Param("user") User user);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.book.id = :bookId AND l.returnDate IS NULL")
    long countActiveLoansForBook(@Param("bookId") Long bookId);

    Optional<Loan> findByIdAndUser(Long id, User user);
}
