package quizmaster.quiz.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quizmaster.quiz.dto.CategoryResponse;
import quizmaster.quiz.models.Category;
import quizmaster.quiz.repository.CategoryEntityRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    
    private final CategoryEntityRepository categoryRepository;
    
    /**
     * Get all active categories
     */
    @Transactional(readOnly = true)
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayName();
    }
    
    /**
     * Get all categories (including inactive)
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayName();
    }
    
    /**
     * Get all active categories as DTO
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategoriesAsDto() {
        return getAllActiveCategories().stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get category by ID
     */
    @Transactional(readOnly = true)
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }
    
    /**
     * Get category by name
     */
    @Transactional(readOnly = true)
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByNameIgnoreCase(name);
    }
    
    /**
     * Create new category
     */
    public Category createCategory(String name, String displayName, String description) {
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Category with name '" + name + "' already exists");
        }
        
        Category category = new Category(name.toUpperCase(), displayName, description);
        return categoryRepository.save(category);
    }
    
    /**
     * Update category
     */
    public Category updateCategory(Long id, String displayName, String description, Boolean isActive) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        
        if (displayName != null) {
            category.setDisplayName(displayName);
        }
        if (description != null) {
            category.setDescription(description);
        }
        if (isActive != null) {
            category.setIsActive(isActive);
        }
        
        return categoryRepository.save(category);
    }
    
    /**
     * Delete category (soft delete - set as inactive)
     */
    public void deactivateCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        
        category.setIsActive(false);
        categoryRepository.save(category);
    }
    
    /**
     * Activate category
     */
    public void activateCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        
        category.setIsActive(true);
        categoryRepository.save(category);
    }
    
    /**
     * Initialize default categories from enum
     */
    @Transactional
    public void initializeDefaultCategories() {
        // Get existing enum values
        quizmaster.quiz.enums.Category[] enumCategories = quizmaster.quiz.enums.Category.values();
        
        for (quizmaster.quiz.enums.Category enumCategory : enumCategories) {
            if (!categoryRepository.existsByNameIgnoreCase(enumCategory.name())) {
                String displayName = formatDisplayName(enumCategory.name());
                Category category = new Category(enumCategory.name(), displayName);
                categoryRepository.save(category);
            }
        }
    }
    
    /**
     * Format enum name to display name
     */
    private String formatDisplayName(String enumName) {
        return switch (enumName) {
            case "MATH" -> "Matemática";
            case "PORTUGUESE" -> "Português";
            case "HISTORY" -> "História";
            case "GEOGRAPHY" -> "Geografia";
            case "SCIENCE" -> "Ciências";
            case "ENGLISH" -> "Inglês";
            case "MIXED" -> "Misto";
            default -> enumName.charAt(0) + enumName.substring(1).toLowerCase();
        };
    }
    
    /**
     * Check if category exists by name
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }
}
