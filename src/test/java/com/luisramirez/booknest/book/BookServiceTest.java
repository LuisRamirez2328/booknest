package com.luisramirez.booknest.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luisramirez.booknest.author.Author;
import com.luisramirez.booknest.author.AuthorRepository;
import com.luisramirez.booknest.category.Category;
import com.luisramirez.booknest.category.CategoryRepository;
import com.luisramirez.booknest.exception.ConflictException;
import com.luisramirez.booknest.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BookService bookService;

    private Author author;
    private Category category;

    @BeforeEach
    void setUp() {
        author = new Author("Gabriel García Márquez", "bio");
        category = new Category("Novela");
    }

    private Book book() {
        Book book = new Book();
        book.setTitle("Cien años de soledad");
        book.setIsbn("978-0307474728");
        book.setPublishedYear(1967);
        book.setAuthor(author);
        book.setCategories(new java.util.HashSet<>(Set.of(category)));
        book.setTotalCopies(5);
        book.setAvailableCopies(5);
        return book;
    }

    @Test
    void findAllMapsSearchResults() {
        Book book = book();
        Page<Book> page = new PageImpl<>(List.of(book));
        when(bookRepository.search(eq("soledad"), eq(null), any(PageRequest.class)))
            .thenReturn(page);

        Page<BookResponse> result = bookService.findAll("soledad", null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Cien años de soledad");
        assertThat(result.getContent().get(0).authorName()).isEqualTo("Gabriel García Márquez");
    }

    @Test
    void findByIdReturnsResponse() {
        Book book = book();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookResponse response = bookService.findById(1L);

        assertThat(response.isbn()).isEqualTo("978-0307474728");
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findById(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createSavesBook() {
        when(bookRepository.existsByIsbn("978-0307474728")).thenReturn(false);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        BookRequest request = new BookRequest(
            "  Cien años de soledad  ", "  978-0307474728  ", 1967, 1L, Set.of(1L), 5
        );

        BookResponse response = bookService.create(request);

        assertThat(response.title()).isEqualTo("Cien años de soledad");
        assertThat(response.availableCopies()).isEqualTo(5);
        assertThat(response.categories()).containsExactly("Novela");
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void createRejectsDuplicateIsbn() {
        when(bookRepository.existsByIsbn("978-0307474728")).thenReturn(true);

        BookRequest request = new BookRequest("Otro libro", "978-0307474728", 2000, 1L, Set.of(), 1);

        assertThatThrownBy(() -> bookService.create(request))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("ISBN");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateAllowsOwnIsbnAndChangesTitle() {
        Book book = book();
        ReflectionTestUtils.setField(book, "id", 1L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.findByIsbn("978-0307474728")).thenReturn(Optional.of(book));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

        BookRequest request = new BookRequest("Nuevo título", "978-0307474728", 1967, 1L, Set.of(), 3);

        BookResponse response = bookService.update(1L, request);

        assertThat(response.title()).isEqualTo("Nuevo título");
        assertThat(book.getAvailableCopies()).isEqualTo(3);
    }

    @Test
    void updateRejectsIsbnOfAnotherBook() {
        Book existing = new Book();
        existing.setIsbn("978-0307474728");
        ReflectionTestUtils.setField(existing, "id", 2L);
        Book book = book();
        ReflectionTestUtils.setField(book, "id", 1L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.findByIsbn("978-0307474728")).thenReturn(Optional.of(existing));

        BookRequest request = new BookRequest("Título", "978-0307474728", 1967, 1L, Set.of(), 3);

        assertThatThrownBy(() -> bookService.update(1L, request))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void deleteRemovesBook() {
        Book book = book();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.delete(1L);

        verify(bookRepository).delete(book);
    }
}
