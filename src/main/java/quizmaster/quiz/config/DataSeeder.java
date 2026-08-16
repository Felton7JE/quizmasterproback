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

    @Override
    public void run(String... args) throws Exception {
        if (missionRepository.count() == 0) {
            // Daily / Starter Missions
            Mission m1 = new Mission();
            m1.setDescription("Jogar 1 partida de Quiz");
            m1.setTargetValue(1);
            m1.setRewardCoins(50);
            m1.setActionType("PLAY_ANY");
            m1.setType(MissionType.DAILY);
            m1.setRewardItemType("EMOTE");
            m1.setRewardItemName("Emoji: Raio Veloz ⚡");
            m1.setRewardItemValue("⚡");
            
            Mission m2 = new Mission();
            m2.setDescription("Vencer 1 partida de Quiz");
            m2.setTargetValue(1);
            m2.setRewardCoins(100);
            m2.setActionType("WIN_ANY");
            m2.setType(MissionType.DAILY);
            m2.setRewardItemType("EMOTE");
            m2.setRewardItemName("Emoji: Na Mosca 🎯");
            m2.setRewardItemValue("🎯");
            
            Mission m3 = new Mission();
            m3.setDescription("Acertar 10 perguntas no Quiz");
            m3.setTargetValue(10);
            m3.setRewardCoins(150);
            m3.setActionType("ANSWER_CORRECT");
            m3.setType(MissionType.DAILY);
            m3.setRewardItemType("TEXT_PHRASE");
            m3.setRewardItemName("Frase: Sou o Novo Campeão! 👑");
            m3.setRewardItemValue("Sou o Novo Campeão! 👑");

            Mission m4 = new Mission();
            m4.setDescription("Vencer 1 partida de Duelo");
            m4.setTargetValue(1);
            m4.setRewardCoins(150);
            m4.setActionType("WIN_DUEL");
            m4.setType(MissionType.DAILY);

            // Monthly Missions
            Mission m5 = new Mission();
            m5.setDescription("Responder 500 perguntas este mês");
            m5.setTargetValue(500);
            m5.setRewardCoins(1500);
            m5.setActionType("ANSWER_ANY");
            m5.setType(MissionType.MONTHLY);

            Mission m6 = new Mission();
            m6.setDescription("Jogar 50 partidas num mês");
            m6.setTargetValue(50);
            m6.setRewardCoins(2000);
            m6.setActionType("PLAY_ANY");
            m6.setType(MissionType.MONTHLY);

            // Milestone Missions
            Mission m7 = new Mission();
            m7.setDescription("Jogar 10 partidas totais");
            m7.setTargetValue(10);
            m7.setRewardCoins(500);
            m7.setActionType("PLAY_ANY");
            m7.setType(MissionType.MILESTONE);

            Mission m8 = new Mission();
            m8.setDescription("Jogar 50 partidas totais");
            m8.setTargetValue(50);
            m8.setRewardCoins(2500);
            m8.setActionType("PLAY_ANY");
            m8.setType(MissionType.MILESTONE);

            Mission m9 = new Mission();
            m9.setDescription("Jogar 100 partidas totais");
            m9.setTargetValue(100);
            m9.setRewardCoins(5000);
            m9.setActionType("PLAY_ANY");
            m9.setType(MissionType.MILESTONE);

            Mission m10 = new Mission();
            m10.setDescription("Acertar 100 perguntas corretamente");
            m10.setTargetValue(100);
            m10.setRewardCoins(4000);
            m10.setActionType("ANSWER_CORRECT");
            m10.setType(MissionType.MILESTONE);
            
            missionRepository.save(m1);
            missionRepository.save(m2);
            missionRepository.save(m3);
            missionRepository.save(m4);
            missionRepository.save(m5);
            missionRepository.save(m6);
            missionRepository.save(m7);
            missionRepository.save(m8);
            missionRepository.save(m9);
            missionRepository.save(m10);
        }
    }
}
