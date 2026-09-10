package quizmaster.quiz.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import quizmaster.quiz.models.Category;
import quizmaster.quiz.repository.CategoryEntityRepository;
import quizmaster.quiz.repository.QuestionRepository;
import quizmaster.quiz.repository.UserRepository;
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

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;


@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryEntityRepository categoryRepo;
    private final UserRepository userRepo;
    private final StoreItemRepository storeItemRepo;
    private final TitleRepository titleRepo;
    private final SeasonRepository seasonRepo;
    private final SeasonRewardRepository seasonRewardRepo;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) {
        initStoreItems();
        initTitles();
        // initSeasons(); // Desativado para que as seasons sejam inseridas e gerenciadas diretamente via SQL (MySQL)
    }

    private void initStoreItems() {
        List<StoreItem> items = new java.util.ArrayList<>();

        // ── BANNERS ──────────────────────────────────────────────────────────
        // Incomuns (500 moedas)
        items.add(createStoreItem("Céu Limpo",       "Um dia perfeito para aprender", 500, ItemType.BANNER, "images/banners/banner_ceu.jpg", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Sala de Aula",    "Onde o conhecimento começa",    500, ItemType.BANNER, "images/banners/banner_quadro_sala.png", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Quiz Clássico",   "Simples e direto ao ponto",     500, ItemType.BANNER, "images/banners/banner_quiz.png", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));

        // Raros (800 moedas)
        items.add(createStoreItem("Floresta Densa",  "A sabedoria escondida nas árvores", 800, ItemType.BANNER, "images/banners/banner_floresta.png", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Castelo Medieval", "Fortaleza do conhecimento",        800, ItemType.BANNER, "images/banners/banner_castelo.png", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Mundo 8-Bits",    "Nostalgia pura em pixeis",          800, ItemType.BANNER, "images/banners/banner_pixel.png", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Noite 8-Bits",    "Acalma os teus olhos enquanto jogas",800, ItemType.BANNER, "images/banners/banner_pixel_noturno.png", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Terminal Hacker", "A invadir a base de dados do Quiz", 800, ItemType.BANNER, "images/banners/banner_hacker.png", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));

        // Épicos (1200 moedas)
        items.add(createStoreItem("Chamas Ardentes", "Aquece a competição",               1200, ItemType.BANNER, "images/banners/banner_fogo.jpg", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Oceano Profundo", "Mergulha num mar de perguntas",     1200, ItemType.BANNER, "images/banners/banner_agua.jpg", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Gelo Eterno",     "Mente fria e calculista",           1200, ItemType.BANNER, "images/banners/banner_gelo.jpg", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Luzes de Néon",   "O teu perfil a brilhar na escuridão",100, ItemType.BANNER, "images/banners/banner_neon.png", "Épico", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Apocalipse (Dia)", "O fim do mundo começou de dia",    1200, ItemType.BANNER, "images/banners/banner_apocalipse_zumbi_dia.png", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));

        // Lendários (1800 moedas)
        items.add(createStoreItem("Apocalipse (Tóxico)","Tudo o que restou foi a radiação",1800, ItemType.BANNER, "images/banners/banner_apocalipse_zumbi_grean.png", "Lendário", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Apocalipse (Noite)","A escuridão esconde os piores medos",1800, ItemType.BANNER, "images/banners/banner_apocalipse_zumbi_night.png", "Lendário", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Galáxia",         "O universo inteiro num só banner",  1800, ItemType.BANNER, "images/banners/banner_galaxia.jpg", "Lendário", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("The Best",        "Um título para os verdadeiros campeões",150, ItemType.BANNER, "images/banners/banner_the_best.png", "Lendário", quizmaster.quiz.enums.CurrencyType.CRYSTALS));

        // Supremos (3500 moedas)
        items.add(createStoreItem("Aura Divina",     "Energia suprema emana de ti",       200, ItemType.BANNER, "images/banners/banner_aura.png", "Supremo", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Multiverso",      "Controlas o espaço e o tempo",      3500, ItemType.BANNER, "images/banners/banner_multiverso.png", "Supremo", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Banner Supremo",  "O topo da cadeia alimentar do Quiz",200, ItemType.BANNER, "images/banners/banner_supremo.png", "Supremo", quizmaster.quiz.enums.CurrencyType.CRYSTALS));

        // ── FRASES PROVOCATIVAS & VITÓRIA ────────────────────────────────────
        // Comuns (150 - 200 moedas)
        items.add(createStoreItem("Frase: Foi fácil demais!",       "Provoca os adversários",            200, ItemType.TEXT_PHRASE, "Foi fácil demais!", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: Mais sorte na próxima!",  "Provoca os adversários",            200, ItemType.TEXT_PHRASE, "Mais sorte na próxima!", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: Boa jogada! 👏",          "Reconhece a jogada do oponente",    150, ItemType.TEXT_PHRASE, "Boa jogada! 👏", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: Não desistas! 💪",        "Motivação durante a partida",      150, ItemType.TEXT_PHRASE, "Não desistas! 💪", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: Muito fácil! 😎",         "Mostra a tua confiança",           180, ItemType.TEXT_PHRASE, "Muito fácil! 😎", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: Tás pronto? 🔥",          "Aquece o duelo de perguntas",      180, ItemType.TEXT_PHRASE, "Tás pronto? 🔥", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: Boa sorte! 🍀",           "Deseja sorte ao adversário",        150, ItemType.TEXT_PHRASE, "Boa sorte! 🍀", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: Ops... 😂",               "Quando alguém erra feio",          150, ItemType.TEXT_PHRASE, "Ops... 😂", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));

        // Incomuns (220 - 280 moedas)
        items.add(createStoreItem("Frase: GG EZ",                   "Clássico dos gamers",               250, ItemType.TEXT_PHRASE, "GG EZ", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: Essa foi por pouco! 😱",  "Para momentos de quase erro",       220, ItemType.TEXT_PHRASE, "Essa foi por pouco! 😱", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: Tás pronto pro show? 🔥", "Mostra que vieste para vencer",     240, ItemType.TEXT_PHRASE, "Tás pronto pro show? 🔥", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: Errar faz parte! 😉",     "Consola o adversário com estilo",   220, ItemType.TEXT_PHRASE, "Errar faz parte! 😉", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: Segura essa resposta! ⚡", "Dispara sabedoria a alta velocidade",260, ItemType.TEXT_PHRASE, "Segura essa resposta! ⚡", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: O jogo só acaba no fim! ⏳", "Reviravoltas até ao último segundo", 250, ItemType.TEXT_PHRASE, "O jogo só acaba no fim! ⏳", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));

        // Raros (300 - 380 moedas)
        items.add(createStoreItem("Frase: Sou imparável!",          "Para os campeões",                  300, ItemType.TEXT_PHRASE, "Sou imparável!", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: Tenta acompanhar o ritmo! ⚡", "Para os mais rápidos",          300, ItemType.TEXT_PHRASE, "Tenta acompanhar o ritmo! ⚡", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: A ler a tua mente! 🔮",   "Prevê cada movimento do adversário",320, ItemType.TEXT_PHRASE, "A ler a tua mente! 🔮", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: Velocidade da Luz! ⚡",    "Resposta instantânea e certeira",   340, ItemType.TEXT_PHRASE, "Velocidade da Luz! ⚡", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: Acertar é de mestre! 🧠", "Conhecimento e sabedoria refinada", 360, ItemType.TEXT_PHRASE, "Acertar é de mestre! 🧠", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: Calculado ao milímetro! 📐", "Estratégia pura e matemática",  350, ItemType.TEXT_PHRASE, "Calculado ao milímetro! 📐", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: A minha intuição nunca falha! ✨", "Sexto sentido ativado",   320, ItemType.TEXT_PHRASE, "A minha intuição nunca falha! ✨", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));

        // Épicos (400 - 600 moedas)
        items.add(createStoreItem("Frase: Sou o Novo Campeão! 👑",  "Exclusivo de Missão ou Loja",       400, ItemType.TEXT_PHRASE, "Sou o Novo Campeão! 👑", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: O trono é meu! 🏆",       "Para os líderes do ranking",        400, ItemType.TEXT_PHRASE, "O trono é meu! 🏆", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: Respeita o Mestre! 🎩",   "Lição de inteligência em direto",   450, ItemType.TEXT_PHRASE, "Respeita o Mestre! 🎩", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: 100% de Precisão! 🎯",    "Sem margem para dúvidas",           480, ItemType.TEXT_PHRASE, "100% de Precisão! 🎯", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: Génio em Ação! 🧪",       "Ciência e conhecimento no topo",    500, ItemType.TEXT_PHRASE, "Génio em Ação! 🧪", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: Mente de Titânio! 🛡️",     "Inabalável contra qualquer pressão", 450, ItemType.TEXT_PHRASE, "Mente de Titânio! 🛡️", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));

        // Lendários & Supremos (Cristais)
        items.add(createStoreItem("Frase: Impossível de Derrotar! 🌟", "Apenas para os invictos",        750, ItemType.TEXT_PHRASE, "Impossível de Derrotar! 🌟", "Lendário", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Frase: Domínio Absoluto! 👑",    "Controle total sobre o tabuleiro",  900, ItemType.TEXT_PHRASE, "Domínio Absoluto! 👑", "Lendário", quizmaster.quiz.enums.CurrencyType.COINS));
        
        // Novas frases em Cristais solicitadas pelo usuário:
        items.add(createStoreItem("Frase: Sou o Novo Campeão! 👑",  "Exclusivo Supremo",                 50, ItemType.TEXT_PHRASE, "Sou o Novo Campeão! 👑", "Supremo", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Frase: Lenda Viva do Quiz! 🌌",  "Conhecimento de outra dimensão",    100, ItemType.TEXT_PHRASE, "Lenda Viva do Quiz! 🌌", "Supremo", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Frase: Eu sou o supremo! ✨",    "Avisa a todos quem manda aqui",     80, ItemType.TEXT_PHRASE, "Eu sou o supremo! ✨", "Supremo", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Frase: Farmando aura! ✨",       "A acumular respeito na partida",    50, ItemType.TEXT_PHRASE, "Farmando aura! ✨", "Lendário", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Frase: Eu sou cheese! 🧀",       "Sabor inconfundível da vitória",    50, ItemType.TEXT_PHRASE, "Eu sou cheese! 🧀", "Lendário", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Frase: É só isso? 😂",           "Quando o nível está muito baixo",   40, ItemType.TEXT_PHRASE, "É só isso? 😂", "Lendário", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Frase: Meu pequenote! 🤏",       "Superioridade esmagadora",          60, ItemType.TEXT_PHRASE, "Meu pequenote! 🤏", "Supremo", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Frase: Você não me vence! 🛡️",   "A barreira impenetrável",           60, ItemType.TEXT_PHRASE, "Você não me vence! 🛡️", "Supremo", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Frase: Me solta! 😤",             "Imparável e furioso",               50, ItemType.TEXT_PHRASE, "Me solta! 😤", "Lendário", quizmaster.quiz.enums.CurrencyType.CRYSTALS));

        // ── EMOJIS & REAÇÕES RÁPIDAS ──────────────────────────────────────────
        // Comuns (150 - 200 moedas)
        items.add(createStoreItem("Emoji: Fogo Lendário 🔥",        "Mostra que estás quente no jogo",   200, ItemType.EMOTE, "🔥", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Emoji: Óculos de Mestre 😎",     "Estilo e confiança total",          200, ItemType.EMOTE, "😎", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Emoji: Palmas de Respeito 👏",   "Reconhecimento da boa jogada",      200, ItemType.EMOTE, "👏", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Emoji: Rindo Demais 😂",         "Gargalhada contagiante",            150, ItemType.EMOTE, "😂", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Emoji: Chocado 😱",              "Surpresa inacreditável",            150, ItemType.EMOTE, "😱", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Emoji: Força Total 💪",          "Determinação inabalável",           180, ItemType.EMOTE, "💪", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Emoji: Trevo da Sorte 🍀",       "Abençoado pelos deuses do Quiz",    180, ItemType.EMOTE, "🍀", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Emoji: Piscadela 😉",            "Cumplicidade e diversão",           150, ItemType.EMOTE, "😉", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));

        // Incomuns (200 - 280 moedas)
        items.add(createStoreItem("Emoji: Caveira de Ouro 💀",      "Quando o adversário foi de base",   250, ItemType.EMOTE, "💀", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Emoji: Frio Calculista 🧊",      "Sangue frio para responder",        220, ItemType.EMOTE, "🧊", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Emoji: Olhos de Foco 👀",        "De olho em cada jogada",            200, ItemType.EMOTE, "👀", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Emoji: Troféu de Ouro 🏆",       "O símbolo dos vencedores",          260, ItemType.EMOTE, "🏆", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Emoji: Silêncio Absoluto 🤫",    "Foco total na pergunta",            220, ItemType.EMOTE, "🤫", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Emoji: Alvo Certeiro 🎯",        "Na mosca sem hesitar",              250, ItemType.EMOTE, "🎯", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));

        // Raros (300 - 380 moedas)
        items.add(createStoreItem("Emoji: Raio Veloz ⚡",           "Velocidade eletrizante",            300, ItemType.EMOTE, "⚡", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Emoji: Foguete Cósmico 🚀",      "Rumo ao topo do ranking",           300, ItemType.EMOTE, "🚀", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Emoji: Mente a Explodir 🤯",     "Perguntas que desafiam a mente",    320, ItemType.EMOTE, "🤯", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Emoji: Mágico do Quiz 🎩",       "Truques de conhecimento",           350, ItemType.EMOTE, "🎩", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Emoji: Estrela Brilhante ⭐",    "Brilho de uma estrela",             380, ItemType.EMOTE, "⭐", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Emoji: Tubarão dos Quizzes 🦈",  "Predador implacável nas partidas",  380, ItemType.EMOTE, "🦈", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));

        // Épicos (400 - 600 moedas)
        items.add(createStoreItem("Emoji: Leão Majestoso 🦁",       "O rei da selva do saber",           450, ItemType.EMOTE, "🦁", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Emoji: Anjo Sábio 😇",           "Respostas iluminadas",              450, ItemType.EMOTE, "😇", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Emoji: Robô Inteligente 🤖",     "Processador de alto rendimento",    500, ItemType.EMOTE, "🤖", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Emoji: Sol Radiante ☀️",         "Ilumina a sala com respostas",      450, ItemType.EMOTE, "☀️", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));

        // Lendários & Supremos Premium (Cristais)
        items.add(createStoreItem("Emoji: Coroa da Vitória 👑",     "Digno de um verdadeiro mestre",     40, ItemType.EMOTE, "👑", "Lendário", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Emoji: Bola de Cristal 🔮",      "Previsões infalíveis",              30, ItemType.EMOTE, "🔮", "Raro", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Emoji: Cérebro Supremo 🧠",      "Pura inteligência em ação",         45, ItemType.EMOTE, "🧠", "Raro", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Emoji: Dragão Místico 🐉",       "Poder ancestral e indomável",       60, ItemType.EMOTE, "🐉", "Lendário", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Emoji: Fénix Imortal 🦅",        "Ressurge sempre com mais força",    70, ItemType.EMOTE, "🦅", "Lendário", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Emoji: Galáxia Suprema 🌌",      "Conexão com todo o cosmos",         90, ItemType.EMOTE, "🌌", "Supremo", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Emoji: Diamante Brilhante 💎",   "Precioso e imbatível",              750, ItemType.EMOTE, "💎", "Lendário", quizmaster.quiz.enums.CurrencyType.COINS));

        // ── AVATARES REAIS ───────────────────────────────────────────────────
        
        // Avatares Básicos (Gratuitos ou muito baratos)
        items.add(createStoreItem("O Angolano",             "Representante da sabedoria de Angola", 100, ItemType.AVATAR, "images/avatars/avatar_angolano.png", "Básico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("A Angolana",             "Representante da inteligência de Angola", 100, ItemType.AVATAR, "images/avatars/avatar_angolana_f.png", "Básico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("O Moçambicano",          "Representante da sabedoria de Moçambique", 100, ItemType.AVATAR, "images/avatars/avatar_mocambicano.png", "Básico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("A Moçambicana",          "Representante da inteligência de Moçambique", 100, ItemType.AVATAR, "images/avatars/avatar_mocambicana_f.png", "Básico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("O Brasileiro",           "Representante da sabedoria do Brasil", 100, ItemType.AVATAR, "images/avatars/avatar_brasileiro.png", "Básico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("A Brasileira",           "Representante da inteligência do Brasil", 100, ItemType.AVATAR, "images/avatars/avatar_brasileira_f.png", "Básico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Raposa Normal",          "Tudo começa com curiosidade",        150, ItemType.AVATAR, "images/avatars/avatar_raposa_normal.png", "Básico", quizmaster.quiz.enums.CurrencyType.COINS));

        // Personalidades & Poses (Comuns - 200-400 moedas)
        items.add(createStoreItem("Soldado de Honra",       "Pronto para a batalha de perguntas", 200, ItemType.AVATAR, "images/avatars/avatar_soldado.png", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("O Juiz Supremo",         "A verdade acima de tudo",            250, ItemType.AVATAR, "images/avatars/avatar_juiz_rigoroso.png", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("O Filósofo",             "Questiona até as perguntas",         250, ItemType.AVATAR, "images/avatars/avatar_filosofo.png", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Raposa Detetive",        "Investiga os mistérios locais",      350, ItemType.AVATAR, "images/avatars/avatar_rapousa_detetive.png", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));

        // Personagens Pixel (Incomuns - 400-600 moedas)
        items.add(createStoreItem("Pixel: O Estudante",     "A saber mais do que parece",         400, ItemType.AVATAR, "images/avatars/avatar_pixel_estudante.png", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Pixel: A Estudiosa",     "Primeira da turma, sempre",          400, ItemType.AVATAR, "images/avatars/avatar_pixel_estudante_f.png", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Pixel: O Construtor",    "Cada erro é só mais código",         400, ItemType.AVATAR, "images/avatars/avatar_pixel_engenheiro.png", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Pixel: O Herói 8-Bit",   "Lenda nos 32 pixels de altura",      450, ItemType.AVATAR, "images/avatars/avatar_pixel_heroi.png", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Pixel: O Doutor",        "Diagnóstico: muita inteligência",    450, ItemType.AVATAR, "images/avatars/avatar_pixel_medico.png", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Pixel: O Mestre",        "Sabe a resposta antes da pergunta",  450, ItemType.AVATAR, "images/avatars/avatar_pixel_professor.png", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Pixel: O Dev",           "Código é o seu superpoder",          80, ItemType.AVATAR, "images/avatars/avatar_pixel_it.png", "Incomum", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Pixel: O Vilão",         "O lado sombrio do saber",            80, ItemType.AVATAR, "images/avatars/avatar_pixel_vilao.png", "Incomum", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Raposa Exploradora",     "Caiu no mundo Pixel a explorar",     550, ItemType.AVATAR, "images/avatars/avatar_raposa_full_pixel_art.png", "Incomum", quizmaster.quiz.enums.CurrencyType.COINS));

        // Personagens Especiais (Raros)
        items.add(createStoreItem("O Herói Retro",          "Nasceu para ser épico",              600, ItemType.AVATAR, "images/avatars/avatar_heroi_retro.png", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("A Heroína Retro",         "A rainha dos jogos antigos",         600, ItemType.AVATAR, "images/avatars/avatar_heroina_retro.png", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("O Vampiro Eterno",        "Imortal, elegante e letal",          650, ItemType.AVATAR, "images/avatars/avatar_vampiro.png", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("O Zumbi Insaciável",      "Devora respostas, não cérebros",     650, ItemType.AVATAR, "images/avatars/avatar_zumbi.png", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Caçador do Paranormal",   "O impossível é a sua especialidade", 700, ItemType.AVATAR, "images/avatars/avatar_detetive_sobrenatural.png", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("O Ninja das Sombras",     "Invisível. Rápido. Certeiro.",       700, ItemType.AVATAR, "images/avatars/avatar_ninja_sombrio_refinado.png", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Raposa Shinobi",          "A astúcia como única arma",          700, ItemType.AVATAR, "images/avatars/avatar_rapousa_ninja.png", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("A Génio Numérica",        "Vê equações onde os outros veem caos", 750, ItemType.AVATAR, "images/avatars/avatar_genia_matematica.png", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("O Vigilante Encapuzado",  "Protege o quiz das respostas erradas", 800, ItemType.AVATAR, "images/avatars/avatar_super_heroi_encapuzado.png", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("A Musa do Pop",           "Estrela do palco e da loja",         850, ItemType.AVATAR, "images/avatars/avatar_musa_do_pop.png", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Urso do Conhecimento",    "Grande, poderoso e sábio",          80, ItemType.AVATAR, "images/avatars/avatar_urso_fixe.png", "Raro", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("A Coruja Omnisciente",    "Vê na escuridão, sabe tudo",        80, ItemType.AVATAR, "images/avatars/avatar_coruja_sabia.png", "Raro", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("IA Dominante",            "A máquina que aprendeu tudo",       100, ItemType.AVATAR, "images/avatars/avatar_ia_dominante.png", "Raro", quizmaster.quiz.enums.CurrencyType.CRYSTALS));

        // Épicos com Auras (Cristais e Moedas)
        items.add(createStoreItem("Senhor das Águas",        "O oceano obedece à sua vontade",    1000, ItemType.AVATAR, "images/avatars/avatar_aura_agua.png", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Mestre do Vento",         "Livre como o ar, ágil como o pensamento", 1000, ItemType.AVATAR, "images/avatars/avatar_aura_ar.png", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Filho das Chamas",        "Ardente, imparável, lendário",      1000, ItemType.AVATAR, "images/avatars/avatar_aura_fogo.png", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Guardião da Terra",       "A força da natureza em pessoa",     1000, ItemType.AVATAR, "images/avatars/avatar_aura_terra.png", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("O Homem da Aura",         "A energia dele é contagiante",      1200, ItemType.AVATAR, "images/avatars/avatar_homem_aura.png", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("O Cérebro Iluminado",     "A mente mais brilhante da loja",    1200, ItemType.AVATAR, "images/avatars/avatar_cerebro_brilhante.png", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Raposa da Sorte",         "A própria fortuna escolheu-te",     1250, ItemType.AVATAR, "images/avatars/avatar_raposa_transicao_sorte.png", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Ser de Partículas",        "Feito de energia pura do cosmos",  120, ItemType.AVATAR, "images/avatars/avatar_aura_particulas.png", "Épico", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("O Invisível Cósmico",     "Além do visível, além da percepção", 120, ItemType.AVATAR, "images/avatars/avatar_invisivel_aura.png", "Épico", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("IA Consciente",          "Processamento a 100%. Erro não encontrado.", 100, ItemType.AVATAR, "images/avatars/avatar_ia.png", "Incomum", quizmaster.quiz.enums.CurrencyType.CRYSTALS));

        // Lendários Premium
        items.add(createStoreItem("I'm Genius",              "QI acima de qualquer medição",      1500, ItemType.AVATAR, "images/avatars/avatar_im_genius.png", "Lendário", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("I'm King",                "O trono pertence a quem sabe",      1500, ItemType.AVATAR, "images/avatars/avatar_im_king.png", "Lendário", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("I'm Legend",              "Uma lenda não se apaga", 150, ItemType.AVATAR, "images/avatars/avatar_im_legend.png", "Lendário", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("I'm Rich",                "Rico em conhecimento e cristais", 150, ItemType.AVATAR, "images/avatars/avatar_im_rich.png", "Lendário", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("I'm Sorry",               "Humildade épica. Classe rara.", 150, ItemType.AVATAR, "images/avatars/avatar_im_sorry.png", "Lendário", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("No One Beats Me",         "Imbatível. Palavra final.", 180, ItemType.AVATAR, "images/avatars/avatar_no_one_beats_me.png", "Lendário", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Try Me",                  "Desafia-me. Tens coragem?", 180, ItemType.AVATAR, "images/avatars/avatar_try_me.png", "Lendário", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        
        // Supremos (Todos do Supremo em Cristais)
        items.add(createStoreItem("Guardiã do Multiverso",   "Protege todas as realidades", 250, ItemType.AVATAR, "images/avatars/avatar_guardiao_do_multiverso.png", "Supremo", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Guardião do Tempo",       "O relógio do universo", 250, ItemType.AVATAR, "images/avatars/avatar_guardiao_do_tempo.png", "Supremo", quizmaster.quiz.enums.CurrencyType.CRYSTALS));
        items.add(createStoreItem("Mestre do Quiz",          "O criador. O único. O lendário.", 250, ItemType.AVATAR, "images/avatars/avatar_mestre_do_quiz.png", "Supremo", quizmaster.quiz.enums.CurrencyType.CRYSTALS));


        // ── MOLDURAS ──────────────────────────────────────────────────────────
        items.add(createStoreItem("Moldura de Fogo",        "Chamas ardentes ao redor do seu avatar", 800, ItemType.PROFILE_FRAME, "images/frames/moldura_fogo_v2.png", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Moldura de Gelo",        "Aura congelante e impenetrável",         800, ItemType.PROFILE_FRAME, "images/frames/moldura_gelo_v2.png", "Raro", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Moldura de Terra",       "Força e estabilidade da natureza",       1000, ItemType.PROFILE_FRAME, "images/frames/moldura_terra_v2.png", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Moldura de Ar",          "Ventos rápidos e cortantes",             1000, ItemType.PROFILE_FRAME, "images/frames/moldura_ar_v2.png", "Épico", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Moldura de Energia",     "Pura energia cósmica pulsante", 80, ItemType.PROFILE_FRAME, "images/frames/moldura_energia_v2.png", "Lendário", quizmaster.quiz.enums.CurrencyType.CRYSTALS));

        // ── EXTRAS CONSUMÍVEIS ───────────────────────────────────────────────
        items.add(createStoreItem("Recarga de Energia",     "Restaura a energia para 100",   150, ItemType.ENERGY_REFILL, "energy_refill", "Comum", quizmaster.quiz.enums.CurrencyType.COINS));
        items.add(createStoreItem("Boost de XP (1h)",       "Dobra o XP ganho por 1 hora",   40, ItemType.XP_BOOST,     "xp_boost_1h", "Incomum", quizmaster.quiz.enums.CurrencyType.CRYSTALS));

        for (StoreItem item : items) {
            if (storeItemRepo.findFirstByName(item.getName()).isEmpty()) {
                storeItemRepo.save(item);
            }
        }
    }

    private StoreItem createStoreItem(String name, String desc, int price, ItemType type, String value, String rarity, quizmaster.quiz.enums.CurrencyType currencyType) {
        StoreItem item = new StoreItem();
        item.setCurrencyType(currencyType);
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
            // --- Partidas Jogadas (GAMES_PLAYED) ---
            createTitle("Iniciante", "Jogue a sua primeira partida", TitleConditionType.GAMES_PLAYED, 1),
            createTitle("Curioso", "Jogue 10 partidas", TitleConditionType.GAMES_PLAYED, 10),
            createTitle("Aprendiz", "Jogue 25 partidas", TitleConditionType.GAMES_PLAYED, 25),
            createTitle("Entusiasta", "Jogue 50 partidas", TitleConditionType.GAMES_PLAYED, 50),
            createTitle("Veterano", "Jogue 100 partidas", TitleConditionType.GAMES_PLAYED, 100),
            createTitle("Resiliente", "Jogue 250 partidas", TitleConditionType.GAMES_PLAYED, 250),
            createTitle("Viciado", "Jogue 500 partidas", TitleConditionType.GAMES_PLAYED, 500),
            createTitle("Incansável", "Jogue 1000 partidas", TitleConditionType.GAMES_PLAYED, 1000),
            createTitle("Lenda Viva", "Jogue 2500 partidas", TitleConditionType.GAMES_PLAYED, 2500),
            createTitle("Mito do Quiz", "Jogue 5000 partidas", TitleConditionType.GAMES_PLAYED, 5000),
            createTitle("Deus do Conhecimento", "Jogue 10000 partidas", TitleConditionType.GAMES_PLAYED, 10000),

            // --- Vitórias (WINS) ---
            createTitle("Primeira Vitória", "Vença 1 partida", TitleConditionType.WINS, 1),
            createTitle("Vencedor", "Vença 5 partidas", TitleConditionType.WINS, 5),
            createTitle("Campeão", "Vença 15 partidas", TitleConditionType.WINS, 15),
            createTitle("Competitivo", "Vença 30 partidas", TitleConditionType.WINS, 30),
            createTitle("Rei da Trívia", "Vença 50 partidas", TitleConditionType.WINS, 50),
            createTitle("Mestre Estrategista", "Vença 100 partidas", TitleConditionType.WINS, 100),
            createTitle("Imparável", "Vença 250 partidas", TitleConditionType.WINS, 250),
            createTitle("Lenda Invicta", "Vença 500 partidas", TitleConditionType.WINS, 500),
            createTitle("Titã", "Vença 1000 partidas", TitleConditionType.WINS, 1000),
            createTitle("Soberano", "Vença 2500 partidas", TitleConditionType.WINS, 2500),
            createTitle("Imperador", "Vença 5000 partidas", TitleConditionType.WINS, 5000),

            // --- Nível (LEVEL) ---
            createTitle("Nível 5", "Alcance o nível 5", TitleConditionType.LEVEL, 5),
            createTitle("Mestre de Nível 10", "Alcance o nível 10", TitleConditionType.LEVEL, 10),
            createTitle("Especialista", "Alcance o nível 15", TitleConditionType.LEVEL, 15),
            createTitle("Intelectual", "Alcance o nível 20", TitleConditionType.LEVEL, 20),
            createTitle("Sábio", "Alcance o nível 30", TitleConditionType.LEVEL, 30),
            createTitle("Gênio", "Alcance o nível 40", TitleConditionType.LEVEL, 40),
            createTitle("Mestre do Quiz", "Alcance o nível 50", TitleConditionType.LEVEL, 50),
            createTitle("Professor", "Alcance o nível 60", TitleConditionType.LEVEL, 60),
            createTitle("Pesquisador", "Alcance o nível 70", TitleConditionType.LEVEL, 70),
            createTitle("Cientista", "Alcance o nível 80", TitleConditionType.LEVEL, 80),
            createTitle("Polímata", "Alcance o nível 90", TitleConditionType.LEVEL, 90),
            createTitle("Onisciente", "Alcance o nível 100", TitleConditionType.LEVEL, 100),

            // --- Convites / Amigos (INVITES) ---
            createTitle("Amigável", "Convide 1 amigo", TitleConditionType.INVITES, 1),
            createTitle("Socializador", "Convide 5 amigos", TitleConditionType.INVITES, 5),
            createTitle("Influenciador", "Convide 10 amigos", TitleConditionType.INVITES, 10),
            createTitle("Embaixador", "Convide 25 amigos", TitleConditionType.INVITES, 25),
            createTitle("Pop Star", "Convide 50 amigos", TitleConditionType.INVITES, 50),
            createTitle("Ícone", "Convide 100 amigos", TitleConditionType.INVITES, 100),

            // --- VIP (VIP) ---
            createTitle("Apoiador", "Adquira o status VIP", TitleConditionType.VIP, 1),
            createTitle("Membro VIP", "Seja um Membro VIP Especial", TitleConditionType.VIP, 2)
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
