package quizmaster.quiz.service;

import lombok.RequiredArgsConstructor;
import quizmaster.quiz.dto.CreateUserRequest;
import quizmaster.quiz.dto.GameResultResponse;
import quizmaster.quiz.dto.RankingResponse;
import quizmaster.quiz.dto.UserResponse;
import quizmaster.quiz.dto.UserStatsResponse;
import quizmaster.quiz.models.User;
// import quizmaster.quiz.repository.GameResultRepository; // TODO: Use when implementing game history
import quizmaster.quiz.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    // private final GameResultRepository gameResultRepository; // TODO: Use when implementing game history
    
    public UserResponse createUser(CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setAvatar(request.getAvatar());
        user.setCreatedAt(LocalDateTime.now());
        
        user = userRepository.save(user);
        return convertToUserResponse(user);
    }
    
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return convertToUserResponse(user);
    }
    
    public UserStatsResponse getUserStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        UserStatsResponse stats = new UserStatsResponse();
        stats.setUserId(user.getId());
        stats.setUsername(user.getUsername());
        stats.setTotalPoints(user.getTotalPoints());
        stats.setGamesPlayed(user.getGamesPlayed());
        stats.setGamesWon(user.getGamesWon());
        stats.setAccuracy(user.getAccuracy());
        stats.setBestStreak(user.getBestStreak());
        
        // Calcular win rate
        if (user.getGamesPlayed() > 0) {
            stats.setWinRate((double) user.getGamesWon() / user.getGamesPlayed() * 100);
        } else {
            stats.setWinRate(0.0);
        }
        
        // Calcular rankings
        List<User> allUsers = userRepository.findAllOrderByTotalPointsDesc();
        stats.setGlobalRanking(IntStream.range(0, allUsers.size())
                .filter(i -> allUsers.get(i).getId().equals(userId))
                .findFirst()
                .orElse(-1) + 1);
        
        return stats;
    }
    
    public List<RankingResponse> getRanking(String period, String category) {
        List<User> users = userRepository.findAllOrderByTotalPointsDesc();
        
        return IntStream.range(0, Math.min(users.size(), 100))
                .mapToObj(i -> {
                    User user = users.get(i);
                    RankingResponse ranking = new RankingResponse();
                    ranking.setPosition(i + 1);
                    ranking.setUserId(user.getId());
                    ranking.setUsername(user.getUsername());
                    ranking.setAvatar(user.getAvatar());
                    ranking.setTotalPoints(user.getTotalPoints());
                    ranking.setGamesPlayed(user.getGamesPlayed());
                    ranking.setGamesWon(user.getGamesWon());
                    ranking.setAccuracy(user.getAccuracy());
                    
                    if (user.getGamesPlayed() > 0) {
                        ranking.setWinRate((double) user.getGamesWon() / user.getGamesPlayed() * 100);
                    } else {
                        ranking.setWinRate(0.0);
                    }
                    
                    return ranking;
                })
                .collect(Collectors.toList());
    }
    
    public List<RankingResponse> getRanking(String period, String category, int page, int size) {
        // TODO: Implement pagination and filtering
        return getRanking(period, category);
    }
    
    public UserResponse updateUser(Long userId, CreateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setAvatar(request.getAvatar());
        
        user = userRepository.save(user);
        return convertToUserResponse(user);
    }
    
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        userRepository.delete(user);
    }
    
    public List<GameResultResponse> getUserGameHistory(Long userId) {
        // TODO: Implement game history retrieval
        return List.of();
    }
    
    public List<UserResponse> searchUsers(String query) {
        // TODO: Implement user search
        return List.of();
    }
    
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        response.setTotalPoints(user.getTotalPoints());
        response.setGamesPlayed(user.getGamesPlayed());
        response.setGamesWon(user.getGamesWon());
        response.setAccuracy(user.getAccuracy());
        response.setBestStreak(user.getBestStreak());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}