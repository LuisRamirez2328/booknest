package com.luisramirez.booknest.book;

import com.luisramirez.booknest.author.Author;
import com.luisramirez.booknest.category.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    @Query("""
        SELECT DISTINCT b FROM Book b
        JOIN b.author a
        LEFT JOIN b.categories c
        WHERE (:q IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(a.name) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (:categoryId IS NULL OR c.id = :categoryId)
    """)
    Page<Book> search(
        @Param("q") String query,
        @Param("categoryId") Long categoryId,
        Pageable pageable
    );

    long countByAuthor(Author author);

    List<Book> findByCategoriesContaining(Category category);
}
