package quizmaster.quiz.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quizmaster.quiz.dto.ActivityLogResponse;
import quizmaster.quiz.models.ActivityLog;
import quizmaster.quiz.models.ActivityType;
import quizmaster.quiz.models.User;
import quizmaster.quiz.repository.ActivityLogRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityService {

    private final ActivityLogRepository activityLogRepository;

    public void logGame(User user, String title, String description, String points) {
        createLog(user, ActivityType.GAME, title, description, points);
    }

    public void logAchievement(User user, String title, String description, String points) {
        createLog(user, ActivityType.ACHIEVEMENT, title, description, points);
    }

    public void logLevelUp(User user, String title, String description) {
        createLog(user, ActivityType.LEVEL_UP, title, description, null);
    }

    public void logGlobalNotification(String title, String description) {
        createLog(null, ActivityType.GLOBAL, title, description, null);
    }

    private void createLog(User user, ActivityType type, String title, String description, String points) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setType(type);
        log.setTitle(title);
        log.setDescription(description);
        log.setPoints(points);
        activityLogRepository.save(log);
    }

    public List<ActivityLogResponse> getFeedForUser(User user, int page, int size) {
        Page<ActivityLog> logs = activityLogRepository.findFeedForUser(user, PageRequest.of(page, size));
        return logs.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private ActivityLogResponse mapToResponse(ActivityLog log) {
        ActivityLogResponse response = new ActivityLogResponse();
        response.setId(log.getId());
        response.setType(log.getType().name());
        response.setTitle(log.getTitle());
        response.setDescription(log.getDescription());
        response.setPoints(log.getPoints());
        response.setCreatedAt(log.getCreatedAt());
        response.setGlobal(log.getUser() == null);
        return response;
    }
}
