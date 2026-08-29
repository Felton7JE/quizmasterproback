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
import quizmaster.quiz.models.StoreItem;
import quizmaster.quiz.models.Title;
import quizmaster.quiz.enums.ItemType;
import quizmaster.quiz.enums.TitleConditionType;
import quizmaster.quiz.repository.StoreItemRepository;
import quizmaster.quiz.repository.TitleRepository;
import java.time.LocalDateTime;

import quizmaster.quiz.models.Season;
import quizmaster.quiz.models.SeasonReward;
import quizmaster.quiz.repository.SeasonRepository;
import quizmaster.quiz.repository.SeasonRewardRepository;
import quizmaster.quiz.enums.RewardType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import java.io.InputStream;


@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryEntityRepository categoryRepo;
    private final QuestionRepository questionRepo;
    private final UserRepository userRepo;
    private final StoreItemRepository storeItemRepo;
    private final TitleRepository titleRepo;
    private final SeasonRepository seasonRepo;
    private final SeasonRewardRepository seasonRewardRepo;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) {
        initUsers();
        initCategories();
        initQuestions();
        initStoreItems();
        initTitles();
        initSeasons();
    }

    private void initUsers() {
        List<String> userNames = List.of("luz1", "luz2", "luz3", "luz4");
        for (String name : userNames) {
            var existingUser = userRepo.findFirstByUsernameAndActiveTrue(name);
            if (existingUser.isEmpty()) {
                User u = new User();
                u.setUsername(name);
                u.setEmail(name + "@test.com");
                u.setCreatedAt(LocalDateTime.now());
                u.setCoins(5000); // 💰 Moedas para testar a loja!
                u.setCrystals(100); // 🔮 Cristais mágicos para IA e loja!
                u.setEnergy(100);
                userRepo.save(u);
            } else {
                User u = existingUser.get();
                if (u.getCrystals() == null || u.getCrystals() < 50) {
                    u.setCrystals(100);
                    userRepo.save(u);
                }
            }
        }
    }

    private void initCategories() {
        if (categoryRepo.count() > 0) {
            // Se já existirem, garantimos apenas que as novas disciplinas (BIOLOGY, PHYSICS, CHEMISTRY) sejam adicionadas
            List<Category> all = categoryRepo.findAll();
            Set<String> existing = all.stream().map(Category::getName).collect(Collectors.toSet());
            List<Category> newCats = new ArrayList<>();
            if (!existing.contains("BIOLOGY")) newCats.add(new Category("BIOLOGY", "Biologia", "Questões de biologia"));
            if (!existing.contains("PHYSICS")) newCats.add(new Category("PHYSICS", "Física", "Questões de física"));
            if (!existing.contains("CHEMISTRY")) newCats.add(new Category("CHEMISTRY", "Química", "Questões de química"));
            if (!newCats.isEmpty()) {
                categoryRepo.saveAll(newCats);
            }
            return;
        }
        
        List<Category> categories = List.of(
                new Category("MATH", "Matemática", "Questões de matemática"),
                new Category("PORTUGUESE", "Português", "Questões de língua portuguesa"),
                new Category("HISTORY", "História", "Questões de história"),
                new Category("GEOGRAPHY", "Geografia", "Questões de geografia"),
                new Category("SCIENCE", "Ciências", "Questões de ciências"),
                new Category("ENGLISH", "Inglês", "Questões de inglês"),
                new Category("MIXED", "Misto", "Questões variadas"),
                new Category("POP_CULTURE", "Cultura Pop", "Cinema, TV e Cultura Pop"),
                new Category("BIOLOGY", "Biologia", "Questões de biologia"),
                new Category("PHYSICS", "Física", "Questões de física"),
                new Category("CHEMISTRY", "Química", "Questões de química")
        );
        categoryRepo.saveAll(categories);
    }

    private void initQuestions() {
        // ATENÇÃO: Código temporário para limpar as questões antigas e inserir as novas dos JSONs revisados!
        System.out.println("TRUNCATING OLD QUESTION TABLES...");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");
        jdbcTemplate.execute("TRUNCATE TABLE user_question_history;");
        jdbcTemplate.execute("TRUNCATE TABLE game_question;");
        jdbcTemplate.execute("TRUNCATE TABLE game_category_question;");
        jdbcTemplate.execute("TRUNCATE TABLE answer;");
        jdbcTemplate.execute("TRUNCATE TABLE questions;");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");
        System.out.println("OLD QUESTIONS TRUNCATED!");

        if (questionRepo.count() > 2000) return;

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

        // Gerar 10 perguntas adicionais de cada dificuldade para cada categoria (para testes)
        for (Category category : categoryRepo.findAll()) {
            for (Difficulty diff : Difficulty.values()) {
                for (int i = 1; i <= 10; i++) {
                    Question q = new Question();
                    q.setCategory(category);
                    q.setDifficulty(diff);
                    q.setQuestionText("Pergunta de teste (" + diff.name() + ") de " + category.getName() + " #" + i);
                    q.setOptions(List.of("Opção Correta", "Opção Errada 1", "Opção Errada 2", "Opção Errada 3"));
                    q.setCorrectAnswer(0);
                    q.setExplanation("Esta é uma pergunta gerada automaticamente para testes.");
                    int pts = diff == Difficulty.EASY ? 100 : (diff == Difficulty.MEDIUM ? 150 : 200);
                    q.setPoints(pts);
                    questionRepo.save(q);
                }
            }
        }

        System.out.println("Loading JSON questions...");
        ObjectMapper mapper = new ObjectMapper();
        loadQuestionsFromJson("questoes/portugues_revisado.json", "PORTUGUESE", catMap, mapper);
        loadQuestionsFromJson("questoes/matematica_revisado.json", "MATH", catMap, mapper);
        loadQuestionsFromJson("questoes/historia_revisado.json", "HISTORY", catMap, mapper);
        loadQuestionsFromJson("questoes/geografia_revisado.json", "GEOGRAPHY", catMap, mapper);
        loadQuestionsFromJson("questoes/ingles_revisado.json", "ENGLISH", catMap, mapper);
        loadQuestionsFromJson("questoes/cultura_pop_revisado.json", "POP_CULTURE", catMap, mapper);
        loadQuestionsFromJson("questoes/biologia_revisado.json", "BIOLOGY", catMap, mapper);
        loadQuestionsFromJson("questoes/fisica_revisado.json", "PHYSICS", catMap, mapper);
        loadQuestionsFromJson("questoes/quimica_revisado.json", "CHEMISTRY", catMap, mapper);
    }

    private void loadQuestionsFromJson(String filePath, String categoryName, Map<String, Category> catMap, ObjectMapper mapper) {
        Category category = catMap.get(categoryName);
        if (category == null) return;

        try (InputStream is = new ClassPathResource(filePath).getInputStream()) {
            JsonNode root = mapper.readTree(is);
            if (root.isArray()) {
                List<Question> questionsToSave = new java.util.ArrayList<>();
                int index = 1;
                for (JsonNode node : root) {
                    Question q = new Question();
                    q.setCategory(category);
                    q.setQuestionText(node.get("pergunta").asText());
                    
                    List<String> options = new java.util.ArrayList<>();
                    for (JsonNode opt : node.get("opcoes")) {
                        options.add(opt.asText());
                    }
                    q.setOptions(options);
                    q.setCorrectAnswer(node.get("resposta_correta").asInt());
                    q.setExplanation("");
                    
                    Difficulty diff;
                    int pts;
                    if (index <= 165) {
                        diff = Difficulty.EASY;
                        pts = 100;
                    } else if (index <= 335) {
                        diff = Difficulty.MEDIUM;
                        pts = 150;
                    } else {
                        diff = Difficulty.HARD;
                        pts = 200;
                    }
                    q.setDifficulty(diff);
                    q.setPoints(pts);
                    
                    questionsToSave.add(q);
                    index++;
                }
                questionRepo.saveAll(questionsToSave);
                System.out.println("Loaded " + questionsToSave.size() + " questions for " + categoryName);
            }
        } catch (Exception e) {
            System.err.println("Could not load questions from " + filePath + ": " + e.getMessage());
        }
    }

    private void initStoreItems() {
        List<StoreItem> items = new java.util.ArrayList<>();

        // ── BANNERS ──────────────────────────────────────────────────────────
        // Incomuns (500 moedas)
        items.add(createStoreItem("Céu Limpo",       "Um dia perfeito para aprender", 500, ItemType.BANNER, "images/banners/banner_ceu.jpg", "Incomum"));
        items.add(createStoreItem("Sala de Aula",    "Onde o conhecimento começa",    500, ItemType.BANNER, "images/banners/banner_quadro_sala.png", "Incomum"));
        items.add(createStoreItem("Quiz Clássico",   "Simples e direto ao ponto",     500, ItemType.BANNER, "images/banners/banner_quiz.png", "Incomum"));

        // Raros (800 moedas)
        items.add(createStoreItem("Floresta Densa",  "A sabedoria escondida nas árvores", 800, ItemType.BANNER, "images/banners/banner_floresta.png", "Raro"));
        items.add(createStoreItem("Castelo Medieval", "Fortaleza do conhecimento",        800, ItemType.BANNER, "images/banners/banner_castelo.png", "Raro"));
        items.add(createStoreItem("Mundo 8-Bits",    "Nostalgia pura em pixeis",          800, ItemType.BANNER, "images/banners/banner_pixel.png", "Raro"));
        items.add(createStoreItem("Noite 8-Bits",    "Acalma os teus olhos enquanto jogas",800, ItemType.BANNER, "images/banners/banner_pixel_noturno.png", "Raro"));
        items.add(createStoreItem("Terminal Hacker", "A invadir a base de dados do Quiz", 800, ItemType.BANNER, "images/banners/banner_hacker.png", "Raro"));

        // Épicos (1200 moedas)
        items.add(createStoreItem("Chamas Ardentes", "Aquece a competição",               1200, ItemType.BANNER, "images/banners/banner_fogo.jpg", "Épico"));
        items.add(createStoreItem("Oceano Profundo", "Mergulha num mar de perguntas",     1200, ItemType.BANNER, "images/banners/banner_agua.jpg", "Épico"));
        items.add(createStoreItem("Gelo Eterno",     "Mente fria e calculista",           1200, ItemType.BANNER, "images/banners/banner_gelo.jpg", "Épico"));
        items.add(createStoreItem("Luzes de Néon",   "O teu perfil a brilhar na escuridão",1200, ItemType.BANNER, "images/banners/banner_neon.png", "Épico"));
        items.add(createStoreItem("Apocalipse (Dia)", "O fim do mundo começou de dia",    1200, ItemType.BANNER, "images/banners/banner_apocalipse_zumbi_dia.png", "Épico"));

        // Lendários (1800 moedas)
        items.add(createStoreItem("Apocalipse (Tóxico)","Tudo o que restou foi a radiação",1800, ItemType.BANNER, "images/banners/banner_apocalipse_zumbi_grean.png", "Lendário"));
        items.add(createStoreItem("Apocalipse (Noite)","A escuridão esconde os piores medos",1800, ItemType.BANNER, "images/banners/banner_apocalipse_zumbi_night.png", "Lendário"));
        items.add(createStoreItem("Galáxia",         "O universo inteiro num só banner",  1800, ItemType.BANNER, "images/banners/banner_galaxia.jpg", "Lendário"));
        items.add(createStoreItem("The Best",        "Um título para os verdadeiros campeões",1800, ItemType.BANNER, "images/banners/banner_the_best.png", "Lendário"));

        // Supremos (3500 moedas)
        items.add(createStoreItem("Aura Divina",     "Energia suprema emana de ti",       3500, ItemType.BANNER, "images/banners/banner_aura.png", "Supremo"));
        items.add(createStoreItem("Multiverso",      "Controlas o espaço e o tempo",      3500, ItemType.BANNER, "images/banners/banner_multiverso.png", "Supremo"));
        items.add(createStoreItem("Banner Supremo",  "O topo da cadeia alimentar do Quiz",3500, ItemType.BANNER, "images/banners/banner_supremo.png", "Supremo"));

        // ── FRASES PROVOCATIVAS & VITÓRIA ────────────────────────────────────
        // Comuns (150 - 200 moedas)
        items.add(createStoreItem("Frase: Foi fácil demais!",       "Provoca os adversários",            200, ItemType.TEXT_PHRASE, "Foi fácil demais!", "Comum"));
        items.add(createStoreItem("Frase: Mais sorte na próxima!",  "Provoca os adversários",            200, ItemType.TEXT_PHRASE, "Mais sorte na próxima!", "Comum"));
        items.add(createStoreItem("Frase: Boa jogada! 👏",          "Reconhece a jogada do oponente",    150, ItemType.TEXT_PHRASE, "Boa jogada! 👏", "Comum"));
        items.add(createStoreItem("Frase: Não desistas! 💪",        "Motivação durante a partida",      150, ItemType.TEXT_PHRASE, "Não desistas! 💪", "Comum"));
        items.add(createStoreItem("Frase: Muito fácil! 😎",         "Mostra a tua confiança",           180, ItemType.TEXT_PHRASE, "Muito fácil! 😎", "Comum"));
        items.add(createStoreItem("Frase: Tás pronto? 🔥",          "Aquece o duelo de perguntas",      180, ItemType.TEXT_PHRASE, "Tás pronto? 🔥", "Comum"));
        items.add(createStoreItem("Frase: Boa sorte! 🍀",           "Deseja sorte ao adversário",        150, ItemType.TEXT_PHRASE, "Boa sorte! 🍀", "Comum"));
        items.add(createStoreItem("Frase: Ops... 😂",               "Quando alguém erra feio",          150, ItemType.TEXT_PHRASE, "Ops... 😂", "Comum"));

        // Incomuns (220 - 280 moedas)
        items.add(createStoreItem("Frase: GG EZ",                   "Clássico dos gamers",               250, ItemType.TEXT_PHRASE, "GG EZ", "Incomum"));
        items.add(createStoreItem("Frase: Essa foi por pouco! 😱",  "Para momentos de quase erro",       220, ItemType.TEXT_PHRASE, "Essa foi por pouco! 😱", "Incomum"));
        items.add(createStoreItem("Frase: Tás pronto pro show? 🔥", "Mostra que vieste para vencer",     240, ItemType.TEXT_PHRASE, "Tás pronto pro show? 🔥", "Incomum"));
        items.add(createStoreItem("Frase: Errar faz parte! 😉",     "Consola o adversário com estilo",   220, ItemType.TEXT_PHRASE, "Errar faz parte! 😉", "Incomum"));
        items.add(createStoreItem("Frase: Segura essa resposta! ⚡", "Dispara sabedoria a alta velocidade",260, ItemType.TEXT_PHRASE, "Segura essa resposta! ⚡", "Incomum"));
        items.add(createStoreItem("Frase: O jogo só acaba no fim! ⏳", "Reviravoltas até ao último segundo", 250, ItemType.TEXT_PHRASE, "O jogo só acaba no fim! ⏳", "Incomum"));

        // Raros (300 - 380 moedas)
        items.add(createStoreItem("Frase: Sou imparável!",          "Para os campeões",                  300, ItemType.TEXT_PHRASE, "Sou imparável!", "Raro"));
        items.add(createStoreItem("Frase: Tenta acompanhar o ritmo! ⚡", "Para os mais rápidos",          300, ItemType.TEXT_PHRASE, "Tenta acompanhar o ritmo! ⚡", "Raro"));
        items.add(createStoreItem("Frase: A ler a tua mente! 🔮",   "Prevê cada movimento do adversário",320, ItemType.TEXT_PHRASE, "A ler a tua mente! 🔮", "Raro"));
        items.add(createStoreItem("Frase: Velocidade da Luz! ⚡",    "Resposta instantânea e certeira",   340, ItemType.TEXT_PHRASE, "Velocidade da Luz! ⚡", "Raro"));
        items.add(createStoreItem("Frase: Acertar é de mestre! 🧠", "Conhecimento e sabedoria refinada", 360, ItemType.TEXT_PHRASE, "Acertar é de mestre! 🧠", "Raro"));
        items.add(createStoreItem("Frase: Calculado ao milímetro! 📐", "Estratégia pura e matemática",  350, ItemType.TEXT_PHRASE, "Calculado ao milímetro! 📐", "Raro"));
        items.add(createStoreItem("Frase: A minha intuição nunca falha! ✨", "Sexto sentido ativado",   320, ItemType.TEXT_PHRASE, "A minha intuição nunca falha! ✨", "Raro"));

        // Épicos (400 - 600 moedas)
        items.add(createStoreItem("Frase: Sou o Novo Campeão! 👑",  "Exclusivo de Missão ou Loja",       400, ItemType.TEXT_PHRASE, "Sou o Novo Campeão! 👑", "Épico"));
        items.add(createStoreItem("Frase: O trono é meu! 🏆",       "Para os líderes do ranking",        400, ItemType.TEXT_PHRASE, "O trono é meu! 🏆", "Épico"));
        items.add(createStoreItem("Frase: Respeita o Mestre! 🎩",   "Lição de inteligência em direto",   450, ItemType.TEXT_PHRASE, "Respeita o Mestre! 🎩", "Épico"));
        items.add(createStoreItem("Frase: 100% de Precisão! 🎯",    "Sem margem para dúvidas",           480, ItemType.TEXT_PHRASE, "100% de Precisão! 🎯", "Épico"));
        items.add(createStoreItem("Frase: Génio em Ação! 🧪",       "Ciência e conhecimento no topo",    500, ItemType.TEXT_PHRASE, "Génio em Ação! 🧪", "Épico"));
        items.add(createStoreItem("Frase: Mente de Titânio! 🛡️",     "Inabalável contra qualquer pressão", 450, ItemType.TEXT_PHRASE, "Mente de Titânio! 🛡️", "Épico"));

        // Lendários & Supremos (750 - 1500 moedas)
        items.add(createStoreItem("Frase: Impossível de Derrotar! 🌟", "Apenas para os invictos",        750, ItemType.TEXT_PHRASE, "Impossível de Derrotar! 🌟", "Lendário"));
        items.add(createStoreItem("Frase: Domínio Absoluto! 👑",    "Controle total sobre o tabuleiro",  900, ItemType.TEXT_PHRASE, "Domínio Absoluto! 👑", "Lendário"));
        items.add(createStoreItem("Frase: Lenda Viva do Quiz! 🌌",  "Conhecimento de outra dimensão",   1500, ItemType.TEXT_PHRASE, "Lenda Viva do Quiz! 🌌", "Supremo"));

        // ── EMOJIS & REAÇÕES RÁPIDAS ──────────────────────────────────────────
        // Comuns (150 - 200 moedas)
        items.add(createStoreItem("Emoji: Fogo Lendário 🔥",        "Mostra que estás quente no jogo",   200, ItemType.EMOTE, "🔥", "Comum"));
        items.add(createStoreItem("Emoji: Óculos de Mestre 😎",     "Estilo e confiança total",          200, ItemType.EMOTE, "😎", "Comum"));
        items.add(createStoreItem("Emoji: Palmas de Respeito 👏",   "Reconhecimento da boa jogada",      200, ItemType.EMOTE, "👏", "Comum"));
        items.add(createStoreItem("Emoji: Rindo Demais 😂",         "Gargalhada contagiante",            150, ItemType.EMOTE, "😂", "Comum"));
        items.add(createStoreItem("Emoji: Chocado 😱",              "Surpresa inacreditável",            150, ItemType.EMOTE, "😱", "Comum"));
        items.add(createStoreItem("Emoji: Força Total 💪",          "Determinação inabalável",           180, ItemType.EMOTE, "💪", "Comum"));
        items.add(createStoreItem("Emoji: Trevo da Sorte 🍀",       "Abençoado pelos deuses do Quiz",    180, ItemType.EMOTE, "🍀", "Comum"));
        items.add(createStoreItem("Emoji: Piscadela 😉",            "Cumplicidade e diversão",           150, ItemType.EMOTE, "😉", "Comum"));

        // Incomuns (200 - 280 moedas)
        items.add(createStoreItem("Emoji: Caveira de Ouro 💀",      "Quando o adversário foi de base",   250, ItemType.EMOTE, "💀", "Incomum"));
        items.add(createStoreItem("Emoji: Frio Calculista 🧊",      "Sangue frio para responder",        220, ItemType.EMOTE, "🧊", "Incomum"));
        items.add(createStoreItem("Emoji: Olhos de Foco 👀",        "De olho em cada jogada",            200, ItemType.EMOTE, "👀", "Incomum"));
        items.add(createStoreItem("Emoji: Troféu de Ouro 🏆",       "O símbolo dos vencedores",          260, ItemType.EMOTE, "🏆", "Incomum"));
        items.add(createStoreItem("Emoji: Silêncio Absoluto 🤫",    "Foco total na pergunta",            220, ItemType.EMOTE, "🤫", "Incomum"));
        items.add(createStoreItem("Emoji: Alvo Certeiro 🎯",        "Na mosca sem hesitar",              250, ItemType.EMOTE, "🎯", "Incomum"));

        // Raros (300 - 380 moedas)
        items.add(createStoreItem("Emoji: Raio Veloz ⚡",           "Velocidade eletrizante",            300, ItemType.EMOTE, "⚡", "Raro"));
        items.add(createStoreItem("Emoji: Cérebro Supremo 🧠",      "Pura inteligência em ação",         350, ItemType.EMOTE, "🧠", "Raro"));
        items.add(createStoreItem("Emoji: Foguete Cósmico 🚀",      "Rumo ao topo do ranking",           300, ItemType.EMOTE, "🚀", "Raro"));
        items.add(createStoreItem("Emoji: Mente a Explodir 🤯",     "Perguntas que desafiam a mente",    320, ItemType.EMOTE, "🤯", "Raro"));
        items.add(createStoreItem("Emoji: Mágico do Quiz 🎩",       "Truques de conhecimento",           350, ItemType.EMOTE, "🎩", "Raro"));
        items.add(createStoreItem("Emoji: Bola de Cristal 🔮",      "Previsões infalíveis",              350, ItemType.EMOTE, "🔮", "Raro"));
        items.add(createStoreItem("Emoji: Estrela Brilhante ⭐",    "Brilho de uma estrela",             380, ItemType.EMOTE, "⭐", "Raro"));
        items.add(createStoreItem("Emoji: Tubarão dos Quizzes 🦈",  "Predador implacável nas partidas",  380, ItemType.EMOTE, "🦈", "Raro"));

        // Épicos (400 - 600 moedas)
        items.add(createStoreItem("Emoji: Coroa da Vitória 👑",     "Digno de um verdadeiro mestre",     400, ItemType.EMOTE, "👑", "Épico"));
        items.add(createStoreItem("Emoji: Leão Majestoso 🦁",       "O rei da selva do saber",           450, ItemType.EMOTE, "🦁", "Épico"));
        items.add(createStoreItem("Emoji: Anjo Sábio 😇",           "Respostas iluminadas",              450, ItemType.EMOTE, "😇", "Épico"));
        items.add(createStoreItem("Emoji: Robô Inteligente 🤖",     "Processador de alto rendimento",    500, ItemType.EMOTE, "🤖", "Épico"));
        items.add(createStoreItem("Emoji: Sol Radiante ☀️",         "Ilumina a sala com respostas",      450, ItemType.EMOTE, "☀️", "Épico"));

        // Lendários & Supremos (750 - 1500 moedas)
        items.add(createStoreItem("Emoji: Diamante Brilhante 💎",   "Precioso e imbatível",              750, ItemType.EMOTE, "💎", "Lendário"));
        items.add(createStoreItem("Emoji: Dragão Místico 🐉",       "Poder ancestral e indomável",       900, ItemType.EMOTE, "🐉", "Lendário"));
        items.add(createStoreItem("Emoji: Fénix Imortal 🦅",        "Ressurge sempre com mais força",   1000, ItemType.EMOTE, "🦅", "Lendário"));
        items.add(createStoreItem("Emoji: Galáxia Suprema 🌌",      "Conexão com todo o cosmos",        1500, ItemType.EMOTE, "🌌", "Supremo"));

        // ── AVATARES REAIS ───────────────────────────────────────────────────
        
        // Avatares Básicos (Gratuitos ou muito baratos)
        items.add(createStoreItem("O Angolano",             "Representante da sabedoria de Angola", 100, ItemType.AVATAR, "images/avatars/avatar_angolano.png", "Básico"));
        items.add(createStoreItem("A Angolana",             "Representante da inteligência de Angola", 100, ItemType.AVATAR, "images/avatars/avatar_angolana_f.png", "Básico"));
        items.add(createStoreItem("O Moçambicano",          "Representante da sabedoria de Moçambique", 100, ItemType.AVATAR, "images/avatars/avatar_mocambicano.png", "Básico"));
        items.add(createStoreItem("A Moçambicana",          "Representante da inteligência de Moçambique", 100, ItemType.AVATAR, "images/avatars/avatar_mocambicana_f.png", "Básico"));
        items.add(createStoreItem("O Brasileiro",           "Representante da sabedoria do Brasil", 100, ItemType.AVATAR, "images/avatars/avatar_brasileiro.png", "Básico"));
        items.add(createStoreItem("A Brasileira",           "Representante da inteligência do Brasil", 100, ItemType.AVATAR, "images/avatars/avatar_brasileira_f.png", "Básico"));
        items.add(createStoreItem("Raposa Normal",          "Tudo começa com curiosidade",        150, ItemType.AVATAR, "images/avatars/avatar_raposa_normal.png", "Básico"));

        // Personalidades & Poses (Comuns - 200-400 moedas)
        items.add(createStoreItem("Soldado de Honra",       "Pronto para a batalha de perguntas", 200, ItemType.AVATAR, "images/avatars/avatar_soldado.png", "Comum"));
        items.add(createStoreItem("O Juiz Supremo",         "A verdade acima de tudo",            250, ItemType.AVATAR, "images/avatars/avatar_juiz_rigoroso.png", "Comum"));
        items.add(createStoreItem("O Filósofo",             "Questiona até as perguntas",         250, ItemType.AVATAR, "images/avatars/avatar_filosofo.png", "Comum"));
        items.add(createStoreItem("Raposa Detetive",        "Investiga os mistérios locais",      350, ItemType.AVATAR, "images/avatars/avatar_rapousa_detetive.png", "Comum"));

        // Personagens Pixel (Incomuns - 400-600 moedas)
        items.add(createStoreItem("Pixel: O Estudante",     "A saber mais do que parece",         400, ItemType.AVATAR, "images/avatars/avatar_pixel_estudante.png", "Incomum"));
        items.add(createStoreItem("Pixel: A Estudiosa",     "Primeira da turma, sempre",          400, ItemType.AVATAR, "images/avatars/avatar_pixel_estudante_f.png", "Incomum"));
        items.add(createStoreItem("Pixel: O Construtor",    "Cada erro é só mais código",         400, ItemType.AVATAR, "images/avatars/avatar_pixel_engenheiro.png", "Incomum"));
        items.add(createStoreItem("Pixel: O Herói 8-Bit",   "Lenda nos 32 pixels de altura",      450, ItemType.AVATAR, "images/avatars/avatar_pixel_heroi.png", "Incomum"));
        items.add(createStoreItem("Pixel: O Dev",           "Código é o seu superpoder",          450, ItemType.AVATAR, "images/avatars/avatar_pixel_it.png", "Incomum"));
        items.add(createStoreItem("Pixel: O Doutor",        "Diagnóstico: muita inteligência",    450, ItemType.AVATAR, "images/avatars/avatar_pixel_medico.png", "Incomum"));
        items.add(createStoreItem("Pixel: O Mestre",        "Sabe a resposta antes da pergunta",  450, ItemType.AVATAR, "images/avatars/avatar_pixel_professor.png", "Incomum"));
        items.add(createStoreItem("Pixel: O Vilão",         "O lado sombrio do saber",            500, ItemType.AVATAR, "images/avatars/avatar_pixel_vilao.png", "Incomum"));
        items.add(createStoreItem("Raposa Exploradora",     "Caiu no mundo Pixel a explorar",     550, ItemType.AVATAR, "images/avatars/avatar_raposa_full_pixel_art.png", "Incomum"));
        items.add(createStoreItem("IA Consciente",          "Processamento a 100%. Erro não encontrado.", 550, ItemType.AVATAR, "images/avatars/avatar_ia.png", "Incomum"));

        // Personagens Especiais (Raros - 600-900 moedas)
        items.add(createStoreItem("O Herói Retro",          "Nasceu para ser épico",              600, ItemType.AVATAR, "images/avatars/avatar_heroi_retro.png", "Raro"));
        items.add(createStoreItem("A Heroína Retro",         "A rainha dos jogos antigos",         600, ItemType.AVATAR, "images/avatars/avatar_heroina_retro.png", "Raro"));
        items.add(createStoreItem("O Vampiro Eterno",        "Imortal, elegante e letal",          650, ItemType.AVATAR, "images/avatars/avatar_vampiro.png", "Raro"));
        items.add(createStoreItem("O Zumbi Insaciável",      "Devora respostas, não cérebros",     650, ItemType.AVATAR, "images/avatars/avatar_zumbi.png", "Raro"));
        items.add(createStoreItem("Caçador do Paranormal",   "O impossível é a sua especialidade", 700, ItemType.AVATAR, "images/avatars/avatar_detetive_sobrenatural.png", "Raro"));
        items.add(createStoreItem("O Ninja das Sombras",     "Invisível. Rápido. Certeiro.",       700, ItemType.AVATAR, "images/avatars/avatar_ninja_sombrio_refinado.png", "Raro"));
        items.add(createStoreItem("Raposa Shinobi",          "A astúcia como única arma",          700, ItemType.AVATAR, "images/avatars/avatar_rapousa_ninja.png", "Raro"));
        items.add(createStoreItem("Urso do Conhecimento",    "Grande, poderoso e sábio",           700, ItemType.AVATAR, "images/avatars/avatar_urso_fixe.png", "Raro"));
        items.add(createStoreItem("A Génio Numérica",        "Vê equações onde os outros veem caos", 750, ItemType.AVATAR, "images/avatars/avatar_genia_matematica.png", "Raro"));
        items.add(createStoreItem("A Coruja Omnisciente",    "Vê na escuridão, sabe tudo",         750, ItemType.AVATAR, "images/avatars/avatar_coruja_sabia.png", "Raro"));
        items.add(createStoreItem("IA Dominante",            "A máquina que aprendeu tudo",        800, ItemType.AVATAR, "images/avatars/avatar_ia_dominante.png", "Raro"));
        items.add(createStoreItem("O Vigilante Encapuzado",  "Protege o quiz das respostas erradas", 800, ItemType.AVATAR, "images/avatars/avatar_super_heroi_encapuzado.png", "Raro"));
        items.add(createStoreItem("A Musa do Pop",           "Estrela do palco e da loja",         850, ItemType.AVATAR, "images/avatars/avatar_musa_do_pop.png", "Raro"));

        // Épicos com Auras (1000-1500 moedas)
        items.add(createStoreItem("Senhor das Águas",        "O oceano obedece à sua vontade",    1000, ItemType.AVATAR, "images/avatars/avatar_aura_agua.png", "Épico"));
        items.add(createStoreItem("Mestre do Vento",         "Livre como o ar, ágil como o pensamento", 1000, ItemType.AVATAR, "images/avatars/avatar_aura_ar.png", "Épico"));
        items.add(createStoreItem("Filho das Chamas",        "Ardente, imparável, lendário",      1000, ItemType.AVATAR, "images/avatars/avatar_aura_fogo.png", "Épico"));
        items.add(createStoreItem("Ser de Partículas",        "Feito de energia pura do cosmos",   1000, ItemType.AVATAR, "images/avatars/avatar_aura_particulas.png", "Épico"));
        items.add(createStoreItem("Guardião da Terra",       "A força da natureza em pessoa",     1000, ItemType.AVATAR, "images/avatars/avatar_aura_terra.png", "Épico"));
        items.add(createStoreItem("O Homem da Aura",         "A energia dele é contagiante",      1200, ItemType.AVATAR, "images/avatars/avatar_homem_aura.png", "Épico"));
        items.add(createStoreItem("O Invisível Cósmico",     "Além do visível, além da percepção",1200, ItemType.AVATAR, "images/avatars/avatar_invisivel_aura.png", "Épico"));
        items.add(createStoreItem("O Cérebro Iluminado",     "A mente mais brilhante da loja",    1200, ItemType.AVATAR, "images/avatars/avatar_cerebro_brilhante.png", "Épico"));
        items.add(createStoreItem("Raposa da Sorte",         "A própria fortuna escolheu-te",     1250, ItemType.AVATAR, "images/avatars/avatar_raposa_transicao_sorte.png", "Épico"));

        // Lendários (1500-2000 moedas)
        items.add(createStoreItem("I'm Genius",              "QI acima de qualquer medição",      1500, ItemType.AVATAR, "images/avatars/avatar_im_genius.png", "Lendário"));
        items.add(createStoreItem("I'm King",                "O trono pertence a quem sabe",      1500, ItemType.AVATAR, "images/avatars/avatar_im_king.png", "Lendário"));
        items.add(createStoreItem("I'm Legend",              "Uma lenda não se apaga",            1800, ItemType.AVATAR, "images/avatars/avatar_im_legend.png", "Lendário"));
        items.add(createStoreItem("I'm Rich",                "Rico em conhecimento e moedas",     1800, ItemType.AVATAR, "images/avatars/avatar_im_rich.png", "Lendário"));
        items.add(createStoreItem("I'm Sorry",               "Humildade épica. Classe rara.",     1500, ItemType.AVATAR, "images/avatars/avatar_im_sorry.png", "Lendário"));
        items.add(createStoreItem("No One Beats Me",         "Imbatível. Palavra final.",         2000, ItemType.AVATAR, "images/avatars/avatar_no_one_beats_me.png", "Lendário"));
        items.add(createStoreItem("Try Me",                  "Desafia-me. Tens coragem?",         2000, ItemType.AVATAR, "images/avatars/avatar_try_me.png", "Lendário"));
        
        // Supremos (A Elite Máxima - 3000+ moedas)
        items.add(createStoreItem("Guardiã do Multiverso",   "Protege todas as realidades",       3000, ItemType.AVATAR, "images/avatars/avatar_guardiao_do_multiverso.png", "Supremo"));
        items.add(createStoreItem("Guardião do Tempo",       "O relógio do universo",             3000, ItemType.AVATAR, "images/avatars/avatar_guardiao_do_tempo.png", "Supremo"));
        items.add(createStoreItem("Mestre do Quiz",          "O criador. O único. O lendário.",   3500, ItemType.AVATAR, "images/avatars/avatar_mestre_do_quiz.png", "Supremo"));


        // ── MOLDURAS ──────────────────────────────────────────────────────────
        items.add(createStoreItem("Moldura de Fogo",        "Chamas ardentes ao redor do seu avatar", 800, ItemType.PROFILE_FRAME, "images/frames/moldura_fogo_v2.png", "Raro"));
        items.add(createStoreItem("Moldura de Gelo",        "Aura congelante e impenetrável",         800, ItemType.PROFILE_FRAME, "images/frames/moldura_gelo_v2.png", "Raro"));
        items.add(createStoreItem("Moldura de Terra",       "Força e estabilidade da natureza",       1000, ItemType.PROFILE_FRAME, "images/frames/moldura_terra_v2.png", "Épico"));
        items.add(createStoreItem("Moldura de Ar",          "Ventos rápidos e cortantes",             1000, ItemType.PROFILE_FRAME, "images/frames/moldura_ar_v2.png", "Épico"));
        items.add(createStoreItem("Moldura de Energia",     "Pura energia cósmica pulsante",          1500, ItemType.PROFILE_FRAME, "images/frames/moldura_energia_v2.png", "Lendário"));

        // ── EXTRAS CONSUMÍVEIS ───────────────────────────────────────────────
        items.add(createStoreItem("Recarga de Energia",     "Restaura a energia para 100",   150, ItemType.ENERGY_REFILL, "energy_refill", "Comum"));
        items.add(createStoreItem("Boost de XP (1h)",       "Dobra o XP ganho por 1 hora",   500, ItemType.XP_BOOST,     "xp_boost_1h", "Incomum"));

        for (StoreItem item : items) {
            if (storeItemRepo.findFirstByName(item.getName()).isEmpty()) {
                storeItemRepo.save(item);
            }
        }
    }

    private StoreItem createStoreItem(String name, String desc, int price, ItemType type, String value, String rarity) {
        StoreItem item = new StoreItem();
        item.setName(name);
        item.setDescription(desc);
        item.setPrice(price);
        item.setType(type);
        item.setValue(value);
        item.setRarity(rarity);
        return item;
    }

    private void initTitles() {
        if (titleRepo.count() > 0) return;

        List<Title> titles = List.of(
            createTitle("Iniciante", "Jogue a sua primeira partida", TitleConditionType.GAMES_PLAYED, 1),
            createTitle("Veterano", "Jogue 100 partidas", TitleConditionType.GAMES_PLAYED, 100),
            createTitle("Rei da Trívia", "Vença 50 partidas", TitleConditionType.WINS, 50),
            createTitle("Mestre de Nível 10", "Alcance o nível 10", TitleConditionType.LEVEL, 10)
        );
        titleRepo.saveAll(titles);
    }

    private Title createTitle(String name, String desc, TitleConditionType type, int val) {
        Title t = new Title();
        t.setName(name);
        t.setDescription(desc);
        t.setConditionType(type);
        t.setConditionValue(val);
        return t;
    }

    private void initSeasons() {
        if (seasonRepo.count() > 0) return;

        Category popCultureCat = categoryRepo.findAll().stream()
            .filter(c -> c.getName().equals("POP_CULTURE"))
            .findFirst()
            .orElse(null);

        if (popCultureCat == null) return;

        Season season = new Season();
        season.setName("Passe de Batalha: Cinema, TV e Cultura Pop!");
        season.setDescription("Mostre que você sabe tudo sobre filmes, séries e música.");
        season.setStartDate(LocalDateTime.now());
        season.setEndDate(LocalDateTime.now().plusDays(90)); // 90 days season
        season.setActive(true);
        season.setExclusiveCategoryId(popCultureCat.getId());
        
        season = seasonRepo.save(season);

        List<SeasonReward> rewards = new java.util.ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            SeasonReward reward = new SeasonReward();
            reward.setSeason(season);
            reward.setLevelRequired(i);
            
            boolean isBoss = (i % 5 == 0);
            reward.setIsBossLevel(isBoss);
            if (isBoss) {
                reward.setBossName("Chefão Nível " + i);
            }

            reward.setFreeRewardType(RewardType.COIN);
            reward.setFreeRewardValue(String.valueOf(i * 10)); // 10, 20, 30...

            reward.setPremiumRewardType(RewardType.XP);
            reward.setPremiumRewardValue(String.valueOf(i * 20));

            rewards.add(reward);
        }
        seasonRewardRepo.saveAll(rewards);
        System.out.println("Season 'Cultura Pop' created with 30 levels.");
    }
}
