package quizmaster.quiz.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import quizmaster.quiz.enums.RewardType;
import quizmaster.quiz.models.Season;
import quizmaster.quiz.models.SeasonReward;
import quizmaster.quiz.repository.SeasonRepository;
import quizmaster.quiz.repository.SeasonRewardRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SeasonDataSeeder implements CommandLineRunner {

    private final SeasonRepository seasonRepository;
    private final SeasonRewardRepository seasonRewardRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        String baseUrl = "/assets/temporada1";

        if (seasonRepository.count() > 0) {
            // Atualiza URLs antigas se estiverem usando localhost
            List<Season> seasons = seasonRepository.findAll();
            if (!seasons.isEmpty()) {
                Season existing = seasons.get(0);
                if (existing.getBannerUrl() != null && existing.getBannerUrl().contains("http://localhost:8080")) {
                    existing.setBannerUrl(baseUrl + "/banner/banner_temporada.jpg");
                    existing.setMapBackgroundUrl(baseUrl + "/map/map_background.jpg");
                    existing.setLockedNodeIconUrl(baseUrl + "/map/icon_cadeado_temporada1.png");
                    existing.setCurrentNodeIconUrl(baseUrl + "/map/icon_fase_atual.png");
                    existing.setCompletedNodeIconUrl(baseUrl + "/map/icon_fase_concluida.png");
                    seasonRepository.save(existing);
                    
                    List<SeasonReward> rewards = seasonRewardRepository.findAll();
                    for (SeasonReward reward : rewards) {
                        if (reward.getBossImageUrl() != null && reward.getBossImageUrl().contains("http://localhost:8080")) {
                            reward.setBossImageUrl(reward.getBossImageUrl().replace("http://localhost:8080/assets/temporada1", baseUrl));
                        }
                        if (reward.getPremiumRewardImageUrl() != null && reward.getPremiumRewardImageUrl().contains("http://localhost:8080")) {
                            reward.setPremiumRewardImageUrl(reward.getPremiumRewardImageUrl().replace("http://localhost:8080/assets/temporada1", baseUrl));
                        }
                        seasonRewardRepository.save(reward);
                    }
                    System.out.println("✅ [SeasonDataSeeder] URLs da Temporada 1 atualizadas para caminhos relativos!");
                }
            }
            return; // Temporada já configurada no banco
        }

        Season season = new Season();
        season.setName("Temporada 1: Mestres do Entretenimento");
        season.setDescription("Passe de Batalha: Cinema, TV e Cultura Pop!");
        season.setStartDate(LocalDateTime.now().minusDays(1));
        season.setEndDate(LocalDateTime.now().plusDays(30));
        season.setActive(true);
        season.setExclusiveCategoryId(1L); 
        
        // URLs relativas
        season.setBannerUrl(baseUrl + "/banner/banner_temporada.jpg");
        season.setMapBackgroundUrl(baseUrl + "/map/map_background.jpg");
        season.setLockedNodeIconUrl(baseUrl + "/map/icon_cadeado_temporada1.png");
        season.setCurrentNodeIconUrl(baseUrl + "/map/icon_fase_atual.png");
        season.setCompletedNodeIconUrl(baseUrl + "/map/icon_fase_concluida.png");
        
        season = seasonRepository.save(season);

        for (int i = 1; i <= 30; i++) {
            SeasonReward reward = new SeasonReward();
            reward.setSeason(season);
            reward.setLevelRequired(i);
            
            // Recompensas intercaladas
            if (i % 5 == 0) {
                reward.setFreeRewardType(RewardType.COIN);
                reward.setFreeRewardValue(String.valueOf(100 * i));
                
                reward.setPremiumRewardType(RewardType.AVATAR);
                reward.setPremiumRewardValue("avatar_vip_" + i);
                
                // Chefões e Prêmios da Temporada
                if (i == 5) {
                    reward.setIsBossLevel(true);
                    reward.setBossName("Maratoneira");
                    reward.setBossImageUrl(baseUrl + "/bosses/boss_maratoneira.png");
                    reward.setPremiumRewardImageUrl(baseUrl + "/Premios/avatar_lvl5.png");
                    reward.setPremiumRewardValue("Avatar Nível 5");
                } else if (i == 10) {
                    reward.setIsBossLevel(true);
                    reward.setBossName("Mestre dos Controles");
                    reward.setBossImageUrl(baseUrl + "/bosses/boss_mestre_controles.png");
                    reward.setPremiumRewardImageUrl(baseUrl + "/Premios/avatar_lvl10.png");
                    reward.setPremiumRewardValue("Avatar Nível 10");
                } else if (i == 15) {
                    reward.setPremiumRewardType(RewardType.TITLE);
                    reward.setPremiumRewardValue("Crítico Estelar");
                } else if (i == 20) {
                    reward.setIsBossLevel(true);
                    reward.setBossName("Mago da Animação");
                    reward.setBossImageUrl(baseUrl + "/bosses/boss_mago_animacao.png");
                    reward.setPremiumRewardImageUrl(baseUrl + "/Premios/avatar_lvl20.png");
                    reward.setPremiumRewardValue("Avatar Nível 20");
                } else if (i == 25) {
                    reward.setIsBossLevel(true);
                    reward.setBossName("Crítico de Ouro");
                    reward.setBossImageUrl(baseUrl + "/bosses/boss_critico_ouro.png");
                    reward.setPremiumRewardImageUrl(baseUrl + "/Premios/avatar_lvl25.png");
                    reward.setPremiumRewardValue("Avatar Nível 25");
                } else if (i == 30) {
                    reward.setIsBossLevel(true);
                    reward.setBossName("Diretora Suprema");
                    reward.setBossImageUrl(baseUrl + "/bosses/boss_diretora_suprema_final.png");
                    reward.setPremiumRewardImageUrl(baseUrl + "/Premios/avatar_lvl30.png");
                    reward.setPremiumRewardValue("Avatar Diretor Supremo");
                }
                
            } else {
                reward.setFreeRewardType(RewardType.COIN);
                reward.setFreeRewardValue(String.valueOf(50 * i));
                reward.setPremiumRewardType(RewardType.ENERGY);
                reward.setPremiumRewardValue("100");
            }
            
            seasonRewardRepository.save(reward);
        }
        System.out.println("✅ [SeasonDataSeeder] Temporada 1 Mestres do Entretenimento criada com sucesso!");
    }
}
