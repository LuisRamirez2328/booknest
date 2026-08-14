package com.luisramirez.booknest.book;

import com.luisramirez.booknest.author.Author;
import com.luisramirez.booknest.author.AuthorRepository;
import com.luisramirez.booknest.category.Category;
import com.luisramirez.booknest.category.CategoryRepository;
import com.luisramirez.booknest.exception.ConflictException;
import com.luisramirez.booknest.exception.ResourceNotFoundException;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    public BookService(
        BookRepository bookRepository,
        AuthorRepository authorRepository,
        CategoryRepository categoryRepository
    ) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
    }

    public Page<BookResponse> findAll(String query, Long categoryId, Pageable pageable) {
        String normalized = query == null || query.isBlank() ? null : query.trim();
        return bookRepository.search(normalized, categoryId, pageable)
            .map(BookResponse::from);
    }

    public BookResponse findById(Long id) {
        return BookResponse.from(getBook(id));
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        if (bookRepository.existsByIsbn(request.isbn().trim())) {
            throw new ConflictException("Ya existe un libro con ese ISBN");
        }
        Book book = new Book();
        applyRequest(book, request);
        bookRepository.save(book);
        return BookResponse.from(book);
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = getBook(id);

        bookRepository.findByIsbn(request.isbn().trim())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new ConflictException("Ya existe un libro con ese ISBN");
            });

        applyRequest(book, request);
        bookRepository.save(book);
        return BookResponse.from(book);
    }

    @Transactional
    public void delete(Long id) {
        Book book = getBook(id);
        bookRepository.delete(book);
    }

    private Book getBook(Long id) {
        return bookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado: " + id));
    }

    private void applyRequest(Book book, BookRequest request) {
        Author author = authorRepository.findById(request.authorId())
            .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado: " + request.authorId()));

        Set<Category> categories = request.categoryIds() == null ? Set.of()
            : request.categoryIds().stream()
                .map(categoryId -> categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + categoryId)))
                .collect(Collectors.toSet());

        book.setTitle(request.title().trim());
        book.setIsbn(request.isbn().trim());
        book.setPublishedYear(request.publishedYear());
        book.setAuthor(author);
        book.setCategories(categories);
        book.setTotalCopies(request.totalCopies());
        book.setAvailableCopies(request.totalCopies());
    }
}
