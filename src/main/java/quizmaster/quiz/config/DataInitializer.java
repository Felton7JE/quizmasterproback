package quizmaster.quiz.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import quizmaster.quiz.enums.Difficulty;
import quizmaster.quiz.models.Category;
import quizmaster.quiz.models.Question;
import quizmaster.quiz.repository.CategoryEntityRepository;
import quizmaster.quiz.repository.QuestionRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryEntityRepository categoryRepo;
    private final QuestionRepository questionRepo;

    @Override
    @Transactional
    public void run(String... args) {
        initCategories();
        initQuestions();
    }

    private void initCategories() {
        if (categoryRepo.count() > 0) return;
        List<Category> categories = List.of(
                new Category("MATH", "Matemática", "Questões de matemática"),
                new Category("PORTUGUESE", "Português", "Questões de língua portuguesa"),
                new Category("HISTORY", "História", "Questões de história"),
                new Category("GEOGRAPHY", "Geografia", "Questões de geografia"),
                new Category("SCIENCE", "Ciências", "Questões de ciências"),
                new Category("ENGLISH", "Inglês", "Questões de inglês"),
                new Category("MIXED", "Misto", "Questões variadas")
        );
        categoryRepo.saveAll(categories);
    }

    private void initQuestions() {
        if (questionRepo.count() > 0) return;

        // Mapear categorias pelo nome para fácil acesso
        Map<String, Category> catMap = categoryRepo.findAll().stream()
                .collect(Collectors.toMap(Category::getName, c -> c));

        // Lista de perguntas exemplo (categoryName, text, options, correctIndex, explanation, difficulty, points)
        List<Object[]> seeds = List.of(
                new Object[]{"MATH", "Quanto é 7 + 5?", List.of("10", "11", "12", "13"), 2, "7 + 5 = 12", Difficulty.EASY, 100},
                new Object[]{"MATH", "Qual é o resultado de 9 x 6?", List.of("42", "54", "56", "48"), 1, "9 * 6 = 54", Difficulty.EASY, 120},
                new Object[]{"GEOGRAPHY", "Capital da França?", List.of("Paris", "Lyon", "Marselha", "Nice"), 0, "Paris é a capital", Difficulty.EASY, 100},
                new Object[]{"HISTORY", "Ano da independência do Brasil?", List.of("1808", "1822", "1889", "1815"), 1, "7 de setembro de 1822", Difficulty.EASY, 120},
                new Object[]{"SCIENCE", "Planeta conhecido como Planeta Vermelho?", List.of("Vênus", "Marte", "Júpiter", "Saturno"), 1, "Marte tem coloração avermelhada", Difficulty.EASY, 110},
                new Object[]{"PORTUGUESE", "Plural de 'cão'?", List.of("cães", "cãos", "cões", "caes"), 0, "Cão -> cães", Difficulty.MEDIUM, 150},
                new Object[]{"ENGLISH", "Tradução de 'house'?", List.of("casa", "cavalo", "cachorro", "carro"), 0, "House = casa", Difficulty.EASY, 90},
                new Object[]{"MATH", "Raiz quadrada de 81?", List.of("7", "9", "8", "6"), 1, "√81 = 9", Difficulty.MEDIUM, 160},
                new Object[]{"SCIENCE", "Principal gás que respiramos?", List.of("Oxigênio", "Nitrogênio", "Gás Carbônico", "Hélio"), 1, "Nitrogênio ~78%", Difficulty.MEDIUM, 170},
                new Object[]{"GEOGRAPHY", "Maior país em área?", List.of("Canadá", "China", "Rússia", "EUA"), 2, "Rússia é o maior", Difficulty.MEDIUM, 180}
        );

        for (Object[] s : seeds) {
            String catName = (String) s[0];
            Category category = catMap.get(catName);
            if (category == null) continue; // segurança

            Question q = new Question();
            q.setCategory(category);
            q.setQuestionText((String) s[1]);
            //noinspection unchecked
            q.setOptions((List<String>) s[2]);
            q.setCorrectAnswer((Integer) s[3]);
            q.setExplanation((String) s[4]);
            q.setDifficulty((Difficulty) s[5]);
            q.setPoints((Integer) s[6]);
            questionRepo.save(q);
        }
    }
}
