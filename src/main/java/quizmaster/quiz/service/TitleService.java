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
    public void equipTitle(User user, Long userTitleId) {
        UserTitle titleToEquip = userTitleRepository.findById(userTitleId)
            .orElseThrow(() -> new RuntimeException("Title not found"));
            
        if (!titleToEquip.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized");
        }
        
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
}
