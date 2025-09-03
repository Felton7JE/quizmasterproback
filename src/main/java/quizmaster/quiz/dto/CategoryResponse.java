package quizmaster.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import quizmaster.quiz.models.Category;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String displayName;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static CategoryResponse fromEntity(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getDisplayName(),
            category.getDescription(),
            category.getIsActive(),
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}
