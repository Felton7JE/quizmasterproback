package quizmaster.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import quizmaster.quiz.enums.CategoryAssignmentMode;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DistributeCategoriesRequest {
    private Long hostId;
    private CategoryAssignmentMode mode;
}
