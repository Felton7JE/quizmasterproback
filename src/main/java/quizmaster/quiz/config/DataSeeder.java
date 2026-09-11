package quizmaster.quiz.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import quizmaster.quiz.models.Mission;
import quizmaster.quiz.enums.MissionType;
import quizmaster.quiz.repository.MissionRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private quizmaster.quiz.repository.PromoCampaignRepository promoCampaignRepository;

    @Autowired
    private quizmaster.quiz.repository.PromoCodeRepository promoCodeRepository;

    @Override
    public void run(String... args) throws Exception {
        initMissions();
        initPromoCodes();
        initSeason();
    }

    private void initPromoCodes() {
        if (promoCampaignRepository.count() > 0) return;

        // Campanha 1: Cristais para testar VIP
        quizmaster.quiz.models.PromoCampaign c1 = new quizmaster.quiz.models.PromoCampaign();
        c1.setName("Campanha de Boas-Vindas");
        c1.setRewardType("CRYSTALS");
        c1.setRewardAmount(100);
        c1.setIsActive(true);
        c1.setGlobalUsageLimit(20);
        promoCampaignRepository.save(c1);

        quizmaster.quiz.models.PromoCode code1 = new quizmaster.quiz.models.PromoCode();
        code1.setCampaign(c1);
        code1.setCode("QUIZPRO100");
        code1.setIsSingleUse(false);
        promoCodeRepository.save(code1);

        // Campanha 2: Moedas
        quizmaster.quiz.models.PromoCampaign c2 = new quizmaster.quiz.models.PromoCampaign();
        c2.setName("Campanha Milionária");
        c2.setRewardType("COINS");
        c2.setRewardAmount(5000);
        c2.setIsActive(true);
        c2.setGlobalUsageLimit(40);
        promoCampaignRepository.save(c2);

        quizmaster.quiz.models.PromoCode code2 = new quizmaster.quiz.models.PromoCode();
        code2.setCampaign(c2);
        code2.setCode("RICO2026");
        code2.setIsSingleUse(false);
        promoCodeRepository.save(code2);
    }

    @Autowired
    private quizmaster.quiz.repository.SeasonRepository seasonRepository;

    @Autowired
    private quizmaster.quiz.repository.CategoryEntityRepository categoryRepository;

    private void initSeason() {
        if (seasonRepository.count() > 0) return;

        quizmaster.quiz.models.Category seasonCat = categoryRepository.findByNameIgnoreCase("Temporada").orElse(null);

        quizmaster.quiz.models.Season s = new quizmaster.quiz.models.Season();
        s.setName("Temporada Inicial");
        s.setDescription("A primeira temporada do QuizMaster Pro!");
        s.setActive(true);
        s.setStartDate(java.time.LocalDateTime.now().minusDays(1));
        s.setEndDate(java.time.LocalDateTime.now().plusMonths(3));
        if (seasonCat != null) {
            s.setExclusiveCategoryId(seasonCat.getId());
        }
        seasonRepository.save(s);
    }

    private void initMissions() {
        if (missionRepository.count() > 0) return;

        java.util.List<Mission> missions = java.util.List.of(
            // --- Diárias ---
            createMission("Jogar 1 partida de Quiz", 1, 50, "PLAY_ANY", MissionType.DAILY),
            createMission("Jogar 3 partidas", 3, 100, "PLAY_ANY", MissionType.DAILY),
            createMission("Jogar 5 partidas", 5, 200, "PLAY_ANY", MissionType.DAILY),
            createMission("Jogar 10 partidas", 10, 500, "PLAY_ANY", MissionType.DAILY),
            createMission("Vencer 1 partida", 1, 100, "WIN_ANY", MissionType.DAILY),
            createMission("Vencer 3 partidas", 3, 300, "WIN_ANY", MissionType.DAILY),
            createMission("Vencer 5 partidas", 5, 500, "WIN_ANY", MissionType.DAILY),
            createMission("Acertar 10 perguntas", 10, 100, "ANSWER_CORRECT", MissionType.DAILY),
            createMission("Acertar 25 perguntas", 25, 250, "ANSWER_CORRECT", MissionType.DAILY),
            createMission("Acertar 50 perguntas", 50, 500, "ANSWER_CORRECT", MissionType.DAILY),
            createMission("Jogar 1 partida Solo", 1, 100, "PLAY_SOLO", MissionType.DAILY),
            createMission("Jogar 3 partidas Solo", 3, 300, "PLAY_SOLO", MissionType.DAILY),
            createMission("Jogar 1 partida Multiplayer", 1, 150, "PLAY_MULTIPLAYER", MissionType.DAILY),
            createMission("Gastar 50 de Energia", 50, 100, "SPEND_ENERGY", MissionType.DAILY),
            createMission("Gastar 100 de Energia", 100, 250, "SPEND_ENERGY", MissionType.DAILY),

            // --- Mensais ---
            createMission("Jogar 25 partidas", 25, 1000, "PLAY_ANY", MissionType.MONTHLY),
            createMission("Jogar 50 partidas", 50, 2500, "PLAY_ANY", MissionType.MONTHLY),
            createMission("Jogar 100 partidas", 100, 6000, "PLAY_ANY", MissionType.MONTHLY),
            createMission("Vencer 10 partidas", 10, 1200, "WIN_ANY", MissionType.MONTHLY),
            createMission("Vencer 25 partidas", 25, 3000, "WIN_ANY", MissionType.MONTHLY),
            createMission("Vencer 50 partidas", 50, 7000, "WIN_ANY", MissionType.MONTHLY),
            createMission("Acertar 100 perguntas", 100, 2000, "ANSWER_CORRECT", MissionType.MONTHLY),
            createMission("Acertar 500 perguntas", 500, 10000, "ANSWER_CORRECT", MissionType.MONTHLY),
            createMission("Comprar 1 item na Loja", 1, 500, "BUY_ITEM", MissionType.MONTHLY),
            createMission("Comprar 3 itens na Loja", 3, 2000, "BUY_ITEM", MissionType.MONTHLY),
            createMission("Comprar 5 itens na Loja", 5, 4000, "BUY_ITEM", MissionType.MONTHLY),
            createMission("Gastar 500 de Energia", 500, 2000, "SPEND_ENERGY", MissionType.MONTHLY),
            createMission("Convidar 1 amigo", 1, 1000, "INVITE_FRIEND", MissionType.MONTHLY),
            createMission("Acertar 250 perguntas", 250, 4500, "ANSWER_CORRECT", MissionType.MONTHLY),
            createMission("Acertar 750 perguntas", 750, 15000, "ANSWER_CORRECT", MissionType.MONTHLY),
            createMission("Jogar 75 partidas", 75, 4000, "PLAY_ANY", MissionType.MONTHLY),
            createMission("Gastar 1000 de Energia", 1000, 5000, "SPEND_ENERGY", MissionType.MONTHLY),
            createMission("Vencer 75 partidas", 75, 10000, "WIN_ANY", MissionType.MONTHLY),

            // --- Milestone (Vitalícias / Iniciante) ---
            createMission("Jogar a 1ª partida", 1, 200, "PLAY_ANY", MissionType.MILESTONE),
            createMission("Jogar 10 partidas totais", 10, 1000, "PLAY_ANY", MissionType.MILESTONE),
            createMission("Jogar 100 partidas totais", 100, 5000, "PLAY_ANY", MissionType.MILESTONE),
            createMission("Jogar 500 partidas totais", 500, 25000, "PLAY_ANY", MissionType.MILESTONE),
            createMission("Vencer a 1ª partida", 1, 300, "WIN_ANY", MissionType.MILESTONE),
            createMission("Vencer 10 partidas totais", 10, 1500, "WIN_ANY", MissionType.MILESTONE),
            createMission("Vencer 50 partidas totais", 50, 8000, "WIN_ANY", MissionType.MILESTONE),
            createMission("Vencer 100 partidas totais", 100, 20000, "WIN_ANY", MissionType.MILESTONE),
            createMission("Acertar 100 perguntas totais", 100, 3000, "ANSWER_CORRECT", MissionType.MILESTONE),
            createMission("Acertar 1000 perguntas totais", 1000, 40000, "ANSWER_CORRECT", MissionType.MILESTONE),
            createMission("Comprar 1 item na Loja", 1, 1000, "BUY_ITEM", MissionType.MILESTONE),
            createMission("Comprar 10 itens na Loja", 10, 10000, "BUY_ITEM", MissionType.MILESTONE),
            createMission("Convidar 1 amigo", 1, 2500, "INVITE_FRIEND", MissionType.MILESTONE),
            createMission("Convidar 5 amigos", 5, 15000, "INVITE_FRIEND", MissionType.MILESTONE),
            createMission("Atingir o Nível 10", 10, 5000, "REACH_LEVEL", MissionType.MILESTONE),
            createMission("Atingir o Nível 50", 50, 50000, "REACH_LEVEL", MissionType.MILESTONE),
            createMission("Tornar-se VIP", 1, 10000, "BECOME_VIP", MissionType.MILESTONE)
        );
        missionRepository.saveAll(missions);
    }

    private Mission createMission(String desc, int target, int coins, String actionType, MissionType type) {
        Mission m = new Mission();
        m.setDescription(desc);
        m.setTargetValue(target);
        m.setRewardCoins(coins);
        m.setActionType(actionType);
        m.setType(type);
        return m;
    }
}
