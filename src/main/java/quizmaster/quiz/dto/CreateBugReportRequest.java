package quizmaster.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBugReportRequest {
    @NotBlank(message = "A descrição não pode estar vazia")
    private String description;
    
    private Long userId;
}
