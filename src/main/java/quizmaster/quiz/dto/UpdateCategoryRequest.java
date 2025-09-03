package quizmaster.quiz.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateCategoryRequest {
    private String displayName;
    private String description;
    private Boolean isActive;
}
