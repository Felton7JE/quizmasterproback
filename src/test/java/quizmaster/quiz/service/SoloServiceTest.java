package quizmaster.quiz.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import quizmaster.quiz.dto.SoloFinishLevelRequest;
import quizmaster.quiz.dto.SoloFinishLevelResponse;
import quizmaster.quiz.models.SoloBossProgress;
import quizmaster.quiz.models.SoloLevelProgress;
import quizmaster.quiz.models.User;
import quizmaster.quiz.repository.*;
import quizmaster.quiz.service.SeasonService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SoloServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CategoryEntityRepository categoryRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private UserQuestionHistoryRepository userQuestionHistoryRepository;
    @Mock private SoloLevelProgressRepository soloLevelProgressRepository;
    @Mock private SoloBossProgressRepository soloBossProgressRepository;
    @Mock private SeasonService seasonService;

    @InjectMocks
    private SoloService soloService;

    private User user;
    private SoloLevelProgress bossLevelProgress;
    private SoloBossProgress bossProgress;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setXp(0);
        user.setCoins(0);

        bossLevelProgress = new SoloLevelProgress(user, 5, true, true);
        bossLevelProgress.setCompleted(false);

        bossProgress = new SoloBossProgress(user, 5);
        bossProgress.setLivesRemaining(1); // Set to 1 so one defeat triggers game over
    }

    @Test
    void testBossDefeat_ReducesLives_RevertsCheckpoint() {
        SoloFinishLevelRequest request = new SoloFinishLevelRequest();
        request.setUserId(1L);
        request.setLevelNumber(5);
        request.setPlayerScore(50);
        request.setBotScore(100); // Player lost
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(soloLevelProgressRepository.findByUserIdAndLevelNumber(1L, 5))
                .thenReturn(Optional.of(bossLevelProgress));
        when(soloBossProgressRepository.findByUserIdAndBossLevelNumber(1L, 5))
                .thenReturn(Optional.of(bossProgress));

        // Mock levels to revert
        for (int i = 3; i <= 5; i++) {
            when(soloLevelProgressRepository.findByUserIdAndLevelNumber(1L, i))
                    .thenReturn(Optional.of(new SoloLevelProgress(user, i, true, i == 5)));
        }

        SoloFinishLevelResponse response = soloService.finishLevel(request);

        assertFalse(response.getVictory());
        assertTrue(response.getCheckpointReverted());
        assertEquals(2, response.getNewCurrentLevel());
        assertEquals(0, response.getBossLivesRemaining());
        
        // Verify lives reset for future attempts
        assertEquals(3, bossProgress.getLivesRemaining());
        
        // Verify levels were locked
        verify(soloLevelProgressRepository, atLeast(3)).save(any(SoloLevelProgress.class));
    }

    @Test
    void testBossVictory_ResetsLives_UnlocksNextLevel() {
        SoloFinishLevelRequest request = new SoloFinishLevelRequest();
        request.setUserId(1L);
        request.setLevelNumber(5);
        request.setPlayerScore(100); // Player won
        request.setBotScore(50);
        request.setCorrectCount(5);
        request.setTotalQuestions(5);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(soloLevelProgressRepository.findByUserIdAndLevelNumber(1L, 5))
                .thenReturn(Optional.of(bossLevelProgress));
        
        SoloLevelProgress nextLevel = new SoloLevelProgress(user, 6, false, false);
        when(soloLevelProgressRepository.findByUserIdAndLevelNumber(1L, 6))
                .thenReturn(Optional.of(nextLevel));
                
        when(soloBossProgressRepository.findByUserIdAndBossLevelNumber(1L, 5))
                .thenReturn(Optional.of(bossProgress));

        SoloLevelProgress previousProgress = new SoloLevelProgress(user, 1, true, false);
        previousProgress.setStarsCount(10);
        when(soloLevelProgressRepository.findByUserIdOrderByLevelNumberAsc(1L))
                .thenReturn(java.util.List.of(previousProgress, bossLevelProgress));

        SoloFinishLevelResponse response = soloService.finishLevel(request);

        assertTrue(response.getVictory());
        assertFalse(response.getCheckpointReverted());
        assertEquals(6, response.getNewCurrentLevel());
        
        // Verify level unlocked
        assertTrue(nextLevel.getUnlocked());
        verify(soloLevelProgressRepository).save(nextLevel);
        
        // Verify boss marked as defeated and lives reset
        assertTrue(bossProgress.getDefeated());
        assertEquals(3, bossProgress.getLivesRemaining());
        verify(soloBossProgressRepository).save(bossProgress);
    }
}
