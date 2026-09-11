package quizmaster.quiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quizmaster.quiz.models.Title;
import quizmaster.quiz.models.User;
import quizmaster.quiz.models.UserTitle;
import quizmaster.quiz.repository.TitleRepository;
import quizmaster.quiz.repository.UserRepository;
import quizmaster.quiz.repository.UserTitleRepository;
import quizmaster.quiz.dto.TitleDTO;
import quizmaster.quiz.dto.UserTitleDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TitleService {

    @Autowired
    private TitleRepository titleRepository;

    @Autowired
    private UserTitleRepository userTitleRepository;

    @Autowired
    private UserRepository userRepository;

    public List<TitleDTO> getAllTitles(User user) {
        List<Title> allTitles = titleRepository.findAll();
        return allTitles.stream().map(t -> {
            TitleDTO dto = new TitleDTO();
            dto.setId(t.getId());
            dto.setName(t.getName());
            dto.setDescription(t.getDescription());
            dto.setConditionType(t.getConditionType());
            dto.setConditionValue(t.getConditionValue());
            dto.setUnlocked(userTitleRepository.existsByUserAndTitle_Id(user, t.getId()));
            return dto;
        }).collect(Collectors.toList());
    }

    public List<UserTitleDTO> getUserTitles(User user) {
        return userTitleRepository.findByUser(user).stream().map(ut -> {
            UserTitleDTO dto = new UserTitleDTO();
            dto.setId(ut.getId());
            dto.setIsEquipped(ut.getIsEquipped());
            
            TitleDTO tdto = new TitleDTO();
            tdto.setId(ut.getTitle().getId());
            tdto.setName(ut.getTitle().getName());
            tdto.setDescription(ut.getTitle().getDescription());
            tdto.setConditionType(ut.getTitle().getConditionType());
            tdto.setConditionValue(ut.getTitle().getConditionValue());
            dto.setTitle(tdto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void equipTitle(User user, Long titleId) {
        UserTitle titleToEquip = userTitleRepository.findByUser(user)
            .stream()
            .filter(ut -> ut.getTitle().getId().equals(titleId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Title not found or not unlocked by user"));
        
        List<UserTitle> currentlyEquipped = userTitleRepository.findByUser(user)
            .stream().filter(UserTitle::getIsEquipped).collect(Collectors.toList());
            
        for (UserTitle ut : currentlyEquipped) {
            ut.setIsEquipped(false);
            userTitleRepository.save(ut);
        }
        
        titleToEquip.setIsEquipped(true);
        userTitleRepository.save(titleToEquip);
        
        user.setActiveTitleId(titleToEquip.getTitle().getId());
        userRepository.save(user);
    }
    
    @Transactional
    public void unequipTitle(User user) {
        List<UserTitle> currentlyEquipped = userTitleRepository.findByUser(user)
            .stream().filter(UserTitle::getIsEquipped).collect(Collectors.toList());
            
        for (UserTitle ut : currentlyEquipped) {
            ut.setIsEquipped(false);
            userTitleRepository.save(ut);
        }
        
        user.setActiveTitleId(null);
        userRepository.save(user);
    }

    @Transactional
    public void evaluateTitles(User user) {
        List<Title> allTitles = titleRepository.findAll();
        for (Title title : allTitles) {
            boolean hasTitle = userTitleRepository.existsByUserAndTitle_Id(user, title.getId());
            if (!hasTitle) {
                boolean meetsCondition = false;
                switch (title.getConditionType()) {
                    case WINS:
                        meetsCondition = user.getGamesWon() != null && user.getGamesWon() >= title.getConditionValue();
                        break;
                    case GAMES_PLAYED:
                        meetsCondition = user.getGamesPlayed() != null && user.getGamesPlayed() >= title.getConditionValue();
                        break;
                    case LEVEL:
                        meetsCondition = user.getLevel() != null && user.getLevel() >= title.getConditionValue();
                        break;
                    case INVITES:
                        meetsCondition = user.getReferralCount() != null && user.getReferralCount() >= title.getConditionValue();
                        break;
                    case VIP:
                        // Assuming VIP condition value 1 means "is VIP" but we will just leave it false for now or skip if not tracked 
                        break;
                }

                if (meetsCondition) {
                    UserTitle newTitle = new UserTitle();
                    newTitle.setUser(user);
                    newTitle.setTitle(title);
                    newTitle.setIsEquipped(false);
                    userTitleRepository.save(newTitle);
                }
            }
        }
    }
}
