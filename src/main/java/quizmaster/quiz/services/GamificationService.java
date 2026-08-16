package quizmaster.quiz.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quizmaster.quiz.models.Mission;
import quizmaster.quiz.enums.MissionType;
import quizmaster.quiz.models.User;
import quizmaster.quiz.models.UserMission;
import quizmaster.quiz.repository.MissionRepository;
import quizmaster.quiz.repository.UserMissionRepository;
import quizmaster.quiz.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

@Service
public class GamificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private UserMissionRepository userMissionRepository;

    @Autowired
    private quizmaster.quiz.service.ActivityService activityService;

    @Transactional
    public User updateStreakOnPlay(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastPlayed = user.getLastPlayedDate();

        if (lastPlayed == null) {
            user.setCurrentStreak(1);
            user.setBestStreak(1);
        } else {
            long hoursSinceLastPlay = ChronoUnit.HOURS.between(lastPlayed, now);
            if (hoursSinceLastPlay > 48) {
                user.setCurrentStreak(1);
            } else if (hoursSinceLastPlay > 12) {
                user.setCurrentStreak(user.getCurrentStreak() + 1);
                if (user.getCurrentStreak() > user.getBestStreak()) {
                    user.setBestStreak(user.getCurrentStreak());
                }
            }
        }
        
        user.setLastPlayedDate(now);
        return userRepository.save(user);
    }
    
    @Transactional
    public User addMatchRewards(User user, int coinsEarned, int eloChange) {
        user.setCoins(user.getCoins() + coinsEarned);
        
        int newElo = user.getEloPoints() + eloChange;
        if (newElo < 0) newElo = 0;
        user.setEloPoints(newElo);
        
        updateLeague(user);
        
        return userRepository.save(user);
    }
    
    private void updateLeague(User user) {
        int elo = user.getEloPoints();
        if (elo < 500) user.setCurrentLeague(quizmaster.quiz.enums.League.BRONZE);
        else if (elo < 1500) user.setCurrentLeague(quizmaster.quiz.enums.League.SILVER);
        else if (elo < 3000) user.setCurrentLeague(quizmaster.quiz.enums.League.GOLD);
        else if (elo < 5000) user.setCurrentLeague(quizmaster.quiz.enums.League.PLATINUM);
        else if (elo < 8000) user.setCurrentLeague(quizmaster.quiz.enums.League.DIAMOND);
        else user.setCurrentLeague(quizmaster.quiz.enums.League.MASTER);
    }

    @Transactional
    public List<UserMission> getOrGenerateMissions(User user) {
        List<UserMission> activeMissions = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);

        // Daily Missions
        List<UserMission> dailyMissions = userMissionRepository.findByUserAndMission_TypeAndAssignedDate(user, MissionType.DAILY, today);
        if (dailyMissions.isEmpty()) {
            List<Mission> allDaily = missionRepository.findByType(MissionType.DAILY);
            Collections.shuffle(allDaily);
            int count = Math.min(3, allDaily.size());
            for (int i = 0; i < count; i++) {
                dailyMissions.add(createUserMission(user, allDaily.get(i), today));
            }
        }
        activeMissions.addAll(dailyMissions);

        // Monthly Missions
        List<UserMission> monthlyMissions = userMissionRepository.findByUserAndMission_TypeAndAssignedDate(user, MissionType.MONTHLY, startOfMonth);
        if (monthlyMissions.isEmpty()) {
            List<Mission> allMonthly = missionRepository.findByType(MissionType.MONTHLY);
            for (Mission m : allMonthly) {
                monthlyMissions.add(createUserMission(user, m, startOfMonth));
            }
        }
        activeMissions.addAll(monthlyMissions);

        // Milestone Missions (assigned once forever, no specific date matters)
        List<UserMission> milestoneMissions = userMissionRepository.findByUserAndMission_Type(user, MissionType.MILESTONE);
        if (milestoneMissions.isEmpty()) {
            List<Mission> allMilestones = missionRepository.findByType(MissionType.MILESTONE);
            for (Mission m : allMilestones) {
                milestoneMissions.add(createUserMission(user, m, today));
            }
        } else {
            // Check if there are new milestones added
            List<Mission> allMilestones = missionRepository.findByType(MissionType.MILESTONE);
            for (Mission m : allMilestones) {
                boolean exists = milestoneMissions.stream().anyMatch(um -> um.getMission().getId().equals(m.getId()));
                if (!exists) {
                    milestoneMissions.add(createUserMission(user, m, today));
                }
            }
        }
        activeMissions.addAll(milestoneMissions);

        return activeMissions;
    }

    private UserMission createUserMission(User user, Mission mission, LocalDate assignedDate) {
        UserMission um = new UserMission();
        um.setUser(user);
        um.setMission(mission);
        um.setAssignedDate(assignedDate);
        um.setCurrentValue(0);
        um.setIsCompleted(false);
        um.setRewardClaimed(false);
        return userMissionRepository.save(um);
    }

    @Transactional
    public void progressMission(User user, String actionType) {
        // Obter as missões atuais para o usuário (já cuida da lógica diária vs mensal)
        List<UserMission> missions = getOrGenerateMissions(user);
        
        for (UserMission um : missions) {
            if (!um.getIsCompleted() && um.getMission().getActionType().equals(actionType)) {
                um.setCurrentValue(um.getCurrentValue() + 1);
                if (um.getCurrentValue() >= um.getMission().getTargetValue()) {
                    um.setIsCompleted(true);
                    activityService.logAchievement(user, "Nova Conquista!", "Completou a missão: " + um.getMission().getDescription(), "+" + um.getMission().getRewardCoins() + " Moedas");
                }
                userMissionRepository.save(um);
            }
        }
    }

    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void regenerateEnergyPeriodically() {
        userRepository.regenerateEnergy();
    }
}
