package com.luisramirez.booknest.author;

import com.luisramirez.booknest.exception.ConflictException;
import com.luisramirez.booknest.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public List<AuthorResponse> findAll() {
        return authorRepository.findAll().stream()
            .map(AuthorResponse::from)
            .toList();
    }

    public AuthorResponse findById(Long id) {
        return AuthorResponse.from(getAuthor(id));
    }

    @Transactional
    public AuthorResponse create(AuthorRequest request) {
        if (authorRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("Ya existe un autor con ese nombre");
        }
        Author author = authorRepository.save(new Author(request.name().trim(), request.bio()));
        return AuthorResponse.from(author);
    }

    @Transactional
    public AuthorResponse update(Long id, AuthorRequest request) {
        Author author = getAuthor(id);
        authorRepository.findByNameIgnoreCase(request.name().trim())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new ConflictException("Ya existe un autor con ese nombre");
            });
        author.setName(request.name().trim());
        author.setBio(request.bio());
        authorRepository.save(author);
        return AuthorResponse.from(author);
    }

    @Transactional
    public void delete(Long id) {
        authorRepository.delete(getAuthor(id));
    }

    private Author getAuthor(Long id) {
        return authorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado: " + id));
    }
}
