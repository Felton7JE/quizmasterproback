package quizmaster.quiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quizmaster.quiz.models.StoreItem;
import quizmaster.quiz.models.User;
import quizmaster.quiz.models.UserItem;
import quizmaster.quiz.repository.StoreItemRepository;
import quizmaster.quiz.repository.UserItemRepository;
import quizmaster.quiz.repository.UserRepository;
import quizmaster.quiz.dto.StoreItemDTO;
import quizmaster.quiz.dto.UserItemDTO;
import quizmaster.quiz.enums.ItemType;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoreService {

    @Autowired
    private StoreItemRepository storeItemRepository;

    @Autowired
    private UserItemRepository userItemRepository;

    @Autowired
    private UserRepository userRepository;

    public List<StoreItemDTO> getStoreFront(User user) {
        List<StoreItem> allItems = storeItemRepository.findAll();
        return allItems.stream().map(item -> {
            StoreItemDTO dto = new StoreItemDTO();
            dto.setId(item.getId());
            dto.setName(item.getName());
            dto.setDescription(item.getDescription());
            dto.setPrice(item.getPrice());
            dto.setType(item.getType());
            dto.setValue(item.getValue());
            dto.setRarity(item.getRarity());
            dto.setOwned(userItemRepository.existsByUserAndStoreItem_Id(user, item.getId()));
            return dto;
        }).collect(Collectors.toList());
    }

    public List<UserItemDTO> getUserInventory(User user) {
        return userItemRepository.findByUser(user).stream().map(ui -> {
            UserItemDTO dto = new UserItemDTO();
            dto.setId(ui.getId());
            dto.setIsEquipped(ui.getIsEquipped());
            
            StoreItemDTO storeDto = new StoreItemDTO();
            storeDto.setId(ui.getStoreItem().getId());
            storeDto.setName(ui.getStoreItem().getName());
            storeDto.setDescription(ui.getStoreItem().getDescription());
            storeDto.setPrice(ui.getStoreItem().getPrice());
            storeDto.setType(ui.getStoreItem().getType());
            storeDto.setValue(ui.getStoreItem().getValue());
            storeDto.setRarity(ui.getStoreItem().getRarity());
            
            dto.setStoreItem(storeDto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void buyItem(User user, Long storeItemId) {
        StoreItem item = storeItemRepository.findById(storeItemId)
            .orElseThrow(() -> new RuntimeException("Item not found"));
            
        if (userItemRepository.existsByUserAndStoreItem_Id(user, storeItemId)) {
            throw new RuntimeException("Item already owned");
        }
        
        if (user.getCoins() < item.getPrice()) {
            throw new RuntimeException("Insufficient coins");
        }
        
        user.setCoins(user.getCoins() - item.getPrice());
        userRepository.save(user);
        
        UserItem userItem = new UserItem();
        userItem.setUser(user);
        userItem.setStoreItem(item);
        userItem.setIsEquipped(false);
        userItemRepository.save(userItem);
    }

    @Transactional
    public void equipItem(User user, Long storeItemId) {
        List<UserItem> userItems = userItemRepository.findByUser(user);
        UserItem itemToEquip = userItems.stream()
            .filter(ui -> ui.getStoreItem().getId().equals(storeItemId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("User item not found"));
            
        ItemType type = itemToEquip.getStoreItem().getType();
        
        if (type == ItemType.BANNER || type == ItemType.AVATAR || type == ItemType.PROFILE_FRAME) {
            List<UserItem> currentlyEquipped = userItemRepository.findByUserAndStoreItem_Type(user, type)
                .stream().filter(UserItem::getIsEquipped).collect(Collectors.toList());
                
            for (UserItem ui : currentlyEquipped) {
                ui.setIsEquipped(false);
                userItemRepository.save(ui);
            }
            
            itemToEquip.setIsEquipped(true);
            userItemRepository.save(itemToEquip);
            
            if (type == ItemType.BANNER) {
                user.setActiveBannerId(itemToEquip.getStoreItem().getId());
            } else if (type == ItemType.AVATAR) {
                user.setActiveAvatarId(itemToEquip.getStoreItem().getId());
                user.setAvatar(itemToEquip.getStoreItem().getValue());
            } else if (type == ItemType.PROFILE_FRAME) {
                user.setActiveFrameId(itemToEquip.getStoreItem().getId());
            }
        } else if (type == ItemType.TEXT_PHRASE) {
            List<UserItem> currentlyEquipped = userItemRepository.findByUserAndStoreItem_Type(user, type)
                .stream().filter(UserItem::getIsEquipped).collect(Collectors.toList());
                
            if (!itemToEquip.getIsEquipped()) {
                if (currentlyEquipped.size() >= 5) {
                    throw new RuntimeException("Limite de 5 frases equipadas atingido. Desequipe uma frase primeiro.");
                }
                itemToEquip.setIsEquipped(true);
                userItemRepository.save(itemToEquip);
            }
            user.setActivePhraseId(itemToEquip.getStoreItem().getId());
        } else if (type == ItemType.EMOTE) {
            List<UserItem> currentlyEquipped = userItemRepository.findByUserAndStoreItem_Type(user, type)
                .stream().filter(UserItem::getIsEquipped).collect(Collectors.toList());
                
            if (!itemToEquip.getIsEquipped()) {
                if (currentlyEquipped.size() >= 10) {
                    throw new RuntimeException("Limite de 10 emojis equipados atingido. Desequipe um emoji primeiro.");
                }
                itemToEquip.setIsEquipped(true);
                userItemRepository.save(itemToEquip);
            }
            user.setActiveEmoteId(itemToEquip.getStoreItem().getId());
        }
        userRepository.save(user);
    }
    
    @Transactional
    public void unequipSpecificItem(User user, Long storeItemId) {
        List<UserItem> userItems = userItemRepository.findByUser(user);
        UserItem itemToUnequip = userItems.stream()
            .filter(ui -> ui.getStoreItem().getId().equals(storeItemId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("User item not found"));
            
        itemToUnequip.setIsEquipped(false);
        userItemRepository.save(itemToUnequip);
        
        ItemType type = itemToUnequip.getStoreItem().getType();
        if (type == ItemType.BANNER && user.getActiveBannerId() != null && user.getActiveBannerId().equals(storeItemId)) {
            user.setActiveBannerId(null);
        } else if (type == ItemType.TEXT_PHRASE && user.getActivePhraseId() != null && user.getActivePhraseId().equals(storeItemId)) {
            UserItem nextEquipped = userItemRepository.findByUserAndStoreItem_Type(user, type)
                .stream().filter(UserItem::getIsEquipped).findFirst().orElse(null);
            user.setActivePhraseId(nextEquipped != null ? nextEquipped.getStoreItem().getId() : null);
        } else if (type == ItemType.AVATAR && user.getActiveAvatarId() != null && user.getActiveAvatarId().equals(storeItemId)) {
            user.setActiveAvatarId(null);
            user.setAvatar("");
        } else if (type == ItemType.PROFILE_FRAME && user.getActiveFrameId() != null && user.getActiveFrameId().equals(storeItemId)) {
            user.setActiveFrameId(null);
        } else if (type == ItemType.EMOTE && user.getActiveEmoteId() != null && user.getActiveEmoteId().equals(storeItemId)) {
            UserItem nextEquipped = userItemRepository.findByUserAndStoreItem_Type(user, type)
                .stream().filter(UserItem::getIsEquipped).findFirst().orElse(null);
            user.setActiveEmoteId(nextEquipped != null ? nextEquipped.getStoreItem().getId() : null);
        }
        userRepository.save(user);
    }

    @Transactional
    public void unequipItem(User user, ItemType itemType) {
        List<UserItem> currentlyEquipped = userItemRepository.findByUserAndStoreItem_Type(user, itemType)
            .stream().filter(UserItem::getIsEquipped).collect(Collectors.toList());
            
        for (UserItem ui : currentlyEquipped) {
            ui.setIsEquipped(false);
            userItemRepository.save(ui);
        }
        
        if (itemType == ItemType.BANNER) {
            user.setActiveBannerId(null);
        } else if (itemType == ItemType.TEXT_PHRASE) {
            user.setActivePhraseId(null);
        } else if (itemType == ItemType.AVATAR) {
            user.setActiveAvatarId(null);
            user.setAvatar(""); // clear avatar or set default
        } else if (itemType == ItemType.PROFILE_FRAME) {
            user.setActiveFrameId(null);
        } else if (itemType == ItemType.EMOTE) {
            user.setActiveEmoteId(null);
        }
        userRepository.save(user);
    }
    @Transactional
    public void consumeItem(User user, ItemType itemType) {
        List<UserItem> userItems = userItemRepository.findByUser(user);
        UserItem itemToConsume = userItems.stream()
            .filter(ui -> ui.getStoreItem().getType() == itemType)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Item consumível não encontrado"));
            
        if (itemType == ItemType.ENERGY_REFILL) {
            user.setEnergy(100);
            userRepository.save(user);
        }
        
        userItemRepository.delete(itemToConsume);
    }
}
