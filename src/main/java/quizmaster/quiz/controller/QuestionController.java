package quizmaster.quiz.controller;

import quizmaster.quiz.dto.*;
import quizmaster.quiz.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuestionController {
    
    private final QuestionService questionService;
    
    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getAllQuestions(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<QuestionResponse> questions = questionService.getAllQuestions(category, difficulty, page, size);
        return ResponseEntity.ok(questions);
    }
    
    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> getQuestion(@PathVariable Long questionId) {
        QuestionResponse question = questionService.getQuestion(questionId);
        return ResponseEntity.ok(question);
    }
    
    @PostMapping
    public ResponseEntity<QuestionResponse> createQuestion(@Valid @RequestBody CreateQuestionRequest request) {
        QuestionResponse question = questionService.createQuestion(request);
        return ResponseEntity.ok(question);
    }
    
    @PutMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody CreateQuestionRequest request) {
        QuestionResponse question = questionService.updateQuestion(questionId, request);
        return ResponseEntity.ok(question);
    }
    
    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        questionService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/random")
    public ResponseEntity<List<QuestionResponse>> getRandomQuestions(
            @RequestParam List<String> categories,
            @RequestParam String difficulty,
            @RequestParam int count) {
        List<QuestionResponse> questions = questionService.getRandomQuestions(categories, difficulty, count);
        return ResponseEntity.ok(questions);
    }
    
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = questionService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/difficulties")
    public ResponseEntity<List<String>> getDifficulties() {
        List<String> difficulties = questionService.getAllDifficulties();
        return ResponseEntity.ok(difficulties);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<QuestionStatsResponse> getQuestionStats() {
        QuestionStatsResponse stats = questionService.getQuestionStats();
        return ResponseEntity.ok(stats);
    }
    
    @PostMapping("/batch")
    public ResponseEntity<List<QuestionResponse>> createQuestionsBatch(@Valid @RequestBody List<CreateQuestionRequest> requests) {
        List<QuestionResponse> questions = questionService.createQuestionsBatch(requests);
        return ResponseEntity.ok(questions);
    }
}
