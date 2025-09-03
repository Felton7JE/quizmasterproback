package quizmaster.quiz.config.componets;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import quizmaster.quiz.service.CategoryService;

@Component
@RequiredArgsConstructor
@Profile("!test") // Não executar em testes
public class CategoryInitializer implements CommandLineRunner {
    
    private final CategoryService categoryService;
    
    @Override
    public void run(String... args) throws Exception {
        categoryService.initializeDefaultCategories();
    }
}
