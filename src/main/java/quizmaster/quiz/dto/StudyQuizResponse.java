package quizmaster.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyQuizResponse {
    private String id;
    private String title;
    private String description;
    private String sourceType; // PDF, TEXT, TOPIC
    private String sourceFileName;
    private LocalDateTime createdAt;
    private Integer questionCount;
    private Integer crystalsCost;
    
    @Builder.Default
    private List<StudyQuestionDto> questions = new ArrayList<>();
    
    @Builder.Default
    private List<FlashcardDto> flashcards = new ArrayList<>();
    
    @Builder.Default
    private List<String> summaryBullets = new ArrayList<>();
    
    private Integer bestScore;
    private Boolean isShared;
    private String shareCode;
}
