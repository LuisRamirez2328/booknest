package com.luisramirez.booknest.category;

import com.luisramirez.booknest.exception.ConflictException;
import com.luisramirez.booknest.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
            .map(CategoryResponse::from)
            .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("Ya existe una categoría con ese nombre");
        }
        Category category = categoryRepository.save(new Category(request.name().trim()));
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + id));
        categoryRepository.delete(category);
    }
}
