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
import quizmaster.quiz.repository.UserRepository;
import quizmaster.quiz.models.User;
import java.time.LocalDateTime;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryEntityRepository categoryRepo;
    private final QuestionRepository questionRepo;
    private final UserRepository userRepo;

    @Override
    @Transactional
    public void run(String... args) {
        initUsers();
        initCategories();
        initQuestions();
    }

    private void initUsers() {
        List<String> userNames = List.of("luz1", "luz2", "luz3", "luz4");
        for (String name : userNames) {
            if (!userRepo.findByUsername(name).isPresent()) {
                User u = new User();
                u.setUsername(name);
                u.setEmail(name + "@test.com");
                u.setCreatedAt(LocalDateTime.now());
                userRepo.save(u);
            }
        }
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
                // Matemática
                new Object[]{"MATH", "Quanto é 7 + 5?", List.of("10", "11", "12", "13"), 2, "7 + 5 = 12", Difficulty.EASY, 100},
                new Object[]{"MATH", "Qual é o resultado de 9 x 6?", List.of("42", "54", "56", "48"), 1, "9 * 6 = 54", Difficulty.EASY, 120},
                new Object[]{"MATH", "Raiz quadrada de 81?", List.of("7", "9", "8", "6"), 1, "√81 = 9", Difficulty.MEDIUM, 160},
                new Object[]{"MATH", "Quanto é 15% de 200?", List.of("20", "25", "30", "35"), 2, "10% é 20, 5% é 10. Total 30", Difficulty.MEDIUM, 150},
                new Object[]{"MATH", "Quantos graus tem um círculo completo?", List.of("180", "270", "360", "90"), 2, "Um círculo tem 360 graus", Difficulty.EASY, 100},
                
                // Geografia
                new Object[]{"GEOGRAPHY", "Capital da França?", List.of("Paris", "Lyon", "Marselha", "Nice"), 0, "Paris é a capital", Difficulty.EASY, 100},
                new Object[]{"GEOGRAPHY", "Maior país em área?", List.of("Canadá", "China", "Rússia", "EUA"), 2, "Rússia é o maior", Difficulty.MEDIUM, 180},
                new Object[]{"GEOGRAPHY", "Qual é o rio mais longo do mundo?", List.of("Amazonas", "Nilo", "Yangtzé", "Mississípi"), 0, "O Amazonas é considerado o mais longo (e mais caudaloso)", Difficulty.HARD, 200},
                new Object[]{"GEOGRAPHY", "Onde fica o deserto do Saara?", List.of("Ásia", "América do Sul", "África", "Austrália"), 2, "Fica no norte de África", Difficulty.EASY, 110},
                new Object[]{"GEOGRAPHY", "Qual país tem o formato de uma bota?", List.of("Espanha", "Grécia", "Itália", "Portugal"), 2, "A Península Itálica parece uma bota", Difficulty.EASY, 100},
                
                // História
                new Object[]{"HISTORY", "Ano da independência do Brasil?", List.of("1808", "1822", "1889", "1815"), 1, "7 de setembro de 1822", Difficulty.EASY, 120},
                new Object[]{"HISTORY", "Quem descobriu o caminho marítimo para a Índia?", List.of("Cristóvão Colombo", "Pedro Álvares Cabral", "Vasco da Gama", "Bartolomeu Dias"), 2, "Vasco da Gama em 1498", Difficulty.MEDIUM, 160},
                new Object[]{"HISTORY", "Em que ano começou a Primeira Guerra Mundial?", List.of("1914", "1918", "1939", "1945"), 0, "Começou em 1914 e acabou em 1918", Difficulty.HARD, 200},
                new Object[]{"HISTORY", "Quem foi o primeiro presidente de Angola?", List.of("José Eduardo dos Santos", "Agostinho Neto", "Jonas Savimbi", "Holden Roberto"), 1, "António Agostinho Neto (1975)", Difficulty.MEDIUM, 150},
                
                // Ciências
                new Object[]{"SCIENCE", "Planeta conhecido como Planeta Vermelho?", List.of("Vênus", "Marte", "Júpiter", "Saturno"), 1, "Marte tem coloração avermelhada devido ao óxido de ferro", Difficulty.EASY, 110},
                new Object[]{"SCIENCE", "Principal gás que respiramos?", List.of("Oxigênio", "Nitrogênio", "Gás Carbônico", "Hélio"), 1, "A atmosfera é composta por ~78% de Nitrogênio", Difficulty.MEDIUM, 170},
                new Object[]{"SCIENCE", "Qual é a fórmula química da água?", List.of("CO2", "H2O", "O2", "NaCl"), 1, "Dois átomos de hidrogênio e um de oxigênio (H2O)", Difficulty.EASY, 90},
                new Object[]{"SCIENCE", "Qual o maior órgão do corpo humano?", List.of("Coração", "Cérebro", "Fígado", "Pele"), 3, "A pele é o maior órgão do corpo humano", Difficulty.MEDIUM, 150},
                
                // Português
                new Object[]{"PORTUGUESE", "Plural de 'cão'?", List.of("cães", "cãos", "cões", "caes"), 0, "Cão -> cães", Difficulty.MEDIUM, 150},
                new Object[]{"PORTUGUESE", "Sinônimo de 'alegre'?", List.of("Triste", "Contente", "Cansado", "Zangado"), 1, "Alegre e contente são sinônimos", Difficulty.EASY, 100},
                new Object[]{"PORTUGUESE", "Qual é o antônimo de 'frio'?", List.of("Gelado", "Morno", "Quente", "Fresco"), 2, "O oposto de frio é quente", Difficulty.EASY, 90},
                
                // Inglês
                new Object[]{"ENGLISH", "Tradução de 'house'?", List.of("casa", "cavalo", "cachorro", "carro"), 0, "House = casa", Difficulty.EASY, 90},
                new Object[]{"ENGLISH", "Como se diz 'Obrigado' em inglês?", List.of("Please", "Sorry", "Hello", "Thank you"), 3, "Thank you significa obrigado", Difficulty.EASY, 80},
                new Object[]{"ENGLISH", "O que significa 'Apple'?", List.of("Laranja", "Banana", "Maçã", "Pera"), 2, "Apple é maçã", Difficulty.EASY, 90},
                
                // Mais Questões
                new Object[]{"MIXED", "Quem pintou a Mona Lisa?", List.of("Van Gogh", "Picasso", "Leonardo da Vinci", "Michelangelo"), 2, "Foi pintada por Leonardo da Vinci", Difficulty.MEDIUM, 150},
                new Object[]{"MIXED", "Qual é o menor país do mundo?", List.of("Mónaco", "Nauru", "Tuvalu", "Vaticano"), 3, "O Vaticano é o menor país", Difficulty.HARD, 200},
                new Object[]{"MIXED", "Qual é o metal cujo símbolo químico é Au?", List.of("Prata", "Ouro", "Cobre", "Alumínio"), 1, "Au vem do latim 'Aurum', que significa ouro", Difficulty.EASY, 100},
                new Object[]{"MATH", "Qual é o próximo número na sequência: 2, 4, 8, 16, ...?", List.of("24", "30", "32", "36"), 2, "A sequência é multiplicada por 2", Difficulty.MEDIUM, 150},
                new Object[]{"SCIENCE", "Qual é a velocidade aproximada da luz?", List.of("300.000 km/s", "150.000 km/s", "1.000.000 km/s", "500.000 km/s"), 0, "Aproximadamente 300.000 km/s no vácuo", Difficulty.HARD, 200}
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
