package com.luisramirez.booknest.config;

import com.luisramirez.booknest.author.Author;
import com.luisramirez.booknest.author.AuthorRepository;
import com.luisramirez.booknest.book.Book;
import com.luisramirez.booknest.book.BookRepository;
import com.luisramirez.booknest.category.Category;
import com.luisramirez.booknest.category.CategoryRepository;
import com.luisramirez.booknest.user.Role;
import com.luisramirez.booknest.user.User;
import com.luisramirez.booknest.user.UserRepository;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(
        UserRepository userRepository,
        AuthorRepository authorRepository,
        CategoryRepository categoryRepository,
        BookRepository bookRepository,
        PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new User(
                    "Admin",
                    "admin@booknest.dev",
                    passwordEncoder.encode("admin123"),
                    Role.ADMIN
                ));
                userRepository.save(new User(
                    "Lector Demo",
                    "user@booknest.dev",
                    passwordEncoder.encode("user123"),
                    Role.USER
                ));
            }

            if (authorRepository.count() == 0) {
                Author gabriel = authorRepository.save(new Author(
                    "Gabriel García Márquez",
                    "Escritor, novelista, cuentista, guionista y periodista colombiano."
                ));
                Author isabel = authorRepository.save(new Author(
                    "Isabel Allende",
                    "Escritora chilena, una de las autoras vivas más leídas en lengua española."
                ));
                Author yuval = authorRepository.save(new Author(
                    "Yuval Noah Harari",
                    "Historiador y escritor israelí, autor de Sapiens."
                ));

                Category novela = categoryRepository.save(new Category("Novela"));
                Category historia = categoryRepository.save(new Category("Historia"));
                Category ciencia = categoryRepository.save(new Category("Ciencia"));
                Category fantasia = categoryRepository.save(new Category("Fantasía"));

                bookRepository.save(book("Cien años de soledad", "978-0307474728", 1967, gabriel,
                    Set.of(novela, fantasia), 5));
                bookRepository.save(book("El amor en los tiempos del cólera", "978-0307387264", 1985, gabriel,
                    Set.of(novela), 4));
                bookRepository.save(book("La casa de los espíritus", "978-0525433480", 1982, isabel,
                    Set.of(novela, fantasia), 3));
                bookRepository.save(book("Paula", "978-0060916765", 1994, isabel,
                    Set.of(novela), 2));
                bookRepository.save(book("Sapiens: De animales a dioses", "978-8499926223", 2011, yuval,
                    Set.of(historia, ciencia), 6));
                bookRepository.save(book("Homo Deus: Breve historia del mañana", "978-8499928739", 2015, yuval,
                    Set.of(historia, ciencia), 4));
            }
        };
    }

    private Book book(String title, String isbn, int year, Author author, Set<Category> categories, int copies) {
        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setPublishedYear(year);
        book.setAuthor(author);
        book.setCategories(categories);
        book.setTotalCopies(copies);
        book.setAvailableCopies(copies);
        return book;
    }
}
