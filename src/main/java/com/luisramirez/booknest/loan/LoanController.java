package com.luisramirez.booknest.loan;

import com.luisramirez.booknest.security.AppUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public LoanResponse borrow(
        @AuthenticationPrincipal AppUserDetails principal,
        @Valid @RequestBody LoanRequest request
    ) {
        return loanService.borrow(principal, request);
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public LoanResponse returnBook(
        @AuthenticationPrincipal AppUserDetails principal,
        @PathVariable Long id
    ) {
        return loanService.returnBook(principal, id);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<LoanResponse> myLoans(@AuthenticationPrincipal AppUserDetails principal) {
        return loanService.myLoans(principal);
    }
}
