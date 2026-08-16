package quizmaster.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySummaryResponse {
    private String topic;
    private String title;
    
    @Builder.Default
    private List<String> summaryBullets = new ArrayList<>();
    
    @Builder.Default
    private List<FlashcardDto> flashcards = new ArrayList<>();
}
