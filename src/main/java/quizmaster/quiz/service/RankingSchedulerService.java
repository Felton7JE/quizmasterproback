package quizmaster.quiz.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import quizmaster.quiz.repository.UserRepository;
import quizmaster.quiz.repository.UserCategoryStatsRepository;

@Service
@RequiredArgsConstructor
public class RankingSchedulerService {

    private final UserRepository userRepository;
    private final UserCategoryStatsRepository userCategoryStatsRepository;

    // Zera os pontos semanais todo Domingo à meia-noite
    @Scheduled(cron = "0 0 0 * * SUN")
    @Transactional
    public void resetWeeklyRankings() {
        userRepository.resetWeeklyPoints();
        userCategoryStatsRepository.resetWeeklyPoints();
        System.out.println("[CRON] Rankings semanais resetados com sucesso.");
    }

    // Zera os pontos mensais no dia 1 de cada mês à meia-noite
    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void resetMonthlyRankings() {
        userRepository.resetMonthlyPoints();
        userCategoryStatsRepository.resetMonthlyPoints();
        System.out.println("[CRON] Rankings mensais resetados com sucesso.");
    }
}
