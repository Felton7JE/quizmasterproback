package quizmaster.quiz.config.componets;

import lombok.RequiredArgsConstructor;
import quizmaster.quiz.models.Category;
import quizmaster.quiz.enums.Difficulty;
import quizmaster.quiz.models.Question;
import quizmaster.quiz.repository.QuestionRepository;
import quizmaster.quiz.service.CategoryService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    
    private final QuestionRepository questionRepository;
    private final CategoryService categoryService;
    
    @Override
    public void run(String... args) throws Exception {
        if (questionRepository.count() == 0) {
            loadSampleQuestions();
        }
    }
    
    private void loadSampleQuestions() {
        // Buscar categorias do banco
        Category mathCategory = categoryService.getCategoryByName("MATH").orElse(null);
        Category portugueseCategory = categoryService.getCategoryByName("PORTUGUESE").orElse(null);
        
        if (mathCategory != null) {
            // Matemática - Fácil
            Question mathEasy1 = new Question();
            mathEasy1.setQuestionText("Quanto é 2 + 2?");
            mathEasy1.setOptions(Arrays.asList("3", "4", "5", "6"));
            mathEasy1.setCorrectAnswer(1);
            mathEasy1.setCategory(mathCategory);
            mathEasy1.setDifficulty(Difficulty.EASY);
            mathEasy1.setExplanation("2 + 2 = 4");
            mathEasy1.setPoints(100);
            questionRepository.save(mathEasy1);
        }
        
        if (portugueseCategory != null) {
            // Português - Fácil
            Question portugueseEasy1 = new Question();
            portugueseEasy1.setQuestionText("Qual é o plural de 'casa'?");
            portugueseEasy1.setOptions(Arrays.asList("casas", "casa", "casões", "casaes"));
            portugueseEasy1.setCorrectAnswer(0);
            portugueseEasy1.setCategory(portugueseCategory);
            portugueseEasy1.setDifficulty(Difficulty.EASY);
            portugueseEasy1.setExplanation("O plural de casa é casas");
            portugueseEasy1.setPoints(100);
            questionRepository.save(portugueseEasy1);
        }
        
        // Adicionar mais perguntas conforme necessário...
    }
}