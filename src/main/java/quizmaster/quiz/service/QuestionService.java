package quizmaster.quiz.service;

import quizmaster.quiz.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {
    
    public List<QuestionResponse> getAllQuestions(String category, String difficulty, int page, int size) {
        // TODO: Implement
        return List.of();
    }
    
    public QuestionResponse getQuestion(Long questionId) {
        // TODO: Implement
        return null;
    }
    
    public QuestionResponse createQuestion(CreateQuestionRequest request) {
        // TODO: Implement
        return null;
    }
    
    public QuestionResponse updateQuestion(Long questionId, CreateQuestionRequest request) {
        // TODO: Implement
        return null;
    }
    
    public void deleteQuestion(Long questionId) {
        // TODO: Implement
    }
    
    public List<QuestionResponse> getRandomQuestions(List<String> categories, String difficulty, int count) {
        // TODO: Implement
        return List.of();
    }
    
    public List<String> getAllCategories() {
        // TODO: Implement
        return List.of();
    }
    
    public List<String> getAllDifficulties() {
        // TODO: Implement
        return List.of();
    }
    
    public QuestionStatsResponse getQuestionStats() {
        // TODO: Implement
        return null;
    }
    
    public List<QuestionResponse> createQuestionsBatch(List<CreateQuestionRequest> requests) {
        // TODO: Implement
        return List.of();
    }
}
