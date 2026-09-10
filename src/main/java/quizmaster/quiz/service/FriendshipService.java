package quizmaster.quiz.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import quizmaster.quiz.dto.FriendDTO;
import quizmaster.quiz.enums.FriendshipStatus;
import quizmaster.quiz.models.Friendship;
import quizmaster.quiz.models.User;
import quizmaster.quiz.repository.FriendshipRepository;
import quizmaster.quiz.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final PresenceService presenceService;
    
    private static final int MAX_FRIENDS = 30;

    public void sendFriendRequest(Long requesterId, String targetUsername) {
        if (targetUsername == null || targetUsername.trim().isEmpty()) {
            throw new RuntimeException("Nome de utilizador inválido");
        }
        
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));
                
        if (requester.getUsername().equalsIgnoreCase(targetUsername)) {
            throw new RuntimeException("Não podes enviar pedido a ti próprio");
        }

        User target = userRepository.findFirstByUsername(targetUsername)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado: " + targetUsername));

        // Verificar limite de amigos de quem pede
        if (friendshipRepository.countAcceptedFriends(requester) >= MAX_FRIENDS) {
            throw new RuntimeException("Atingiste o limite máximo de " + MAX_FRIENDS + " amigos!");
        }

        // Verificar limite de amigos do alvo
        if (friendshipRepository.countAcceptedFriends(target) >= MAX_FRIENDS) {
            throw new RuntimeException("O utilizador " + targetUsername + " já tem a lista de amigos cheia.");
        }

        Optional<Friendship> existing = friendshipRepository.findFriendshipBetween(requester, target);
        if (existing.isPresent()) {
            Friendship f = existing.get();
            if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new RuntimeException("Já são amigos!");
            }
            if (f.getStatus() == FriendshipStatus.PENDING) {
                throw new RuntimeException("Já existe um pedido de amizade pendente.");
            }
            if (f.getStatus() == FriendshipStatus.BLOCKED) {
                throw new RuntimeException("Não é possível enviar pedido de amizade a este utilizador.");
            }
        }

        Friendship friendship = new Friendship();
        friendship.setUser(requester);
        friendship.setFriend(target);
        friendship.setStatus(FriendshipStatus.PENDING);
        
        friendshipRepository.save(friendship);
    }

    public void acceptFriendRequest(Long userId, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
                
        // Apenas quem RECEBEU o pedido pode aceitar (o 'friend' no nosso schema)
        if (!friendship.getFriend().getId().equals(userId)) {
            throw new RuntimeException("Apenas o destinatário pode aceitar este pedido");
        }
        
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new RuntimeException("Este pedido não está pendente");
        }

        User requester = friendship.getUser();
        User receiver = friendship.getFriend();

        if (friendshipRepository.countAcceptedFriends(requester) >= MAX_FRIENDS ||
            friendshipRepository.countAcceptedFriends(receiver) >= MAX_FRIENDS) {
            throw new RuntimeException("Limite de amigos excedido.");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);
    }

    public void rejectFriendRequest(Long userId, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
                
        if (!friendship.getFriend().getId().equals(userId)) {
            throw new RuntimeException("Apenas o destinatário pode rejeitar este pedido");
        }
        
        friendshipRepository.delete(friendship);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Amigo não encontrado"));
                
        Friendship friendship = friendshipRepository.findFriendshipBetween(user, friend)
                .orElseThrow(() -> new RuntimeException("Amizade não encontrada"));
                
        friendshipRepository.delete(friendship);
    }

    public List<FriendDTO> getFriendsList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));
                
        List<Friendship> accepted = friendshipRepository.findAllByUserAndStatus(user, FriendshipStatus.ACCEPTED);
        
        return accepted.stream().map(f -> {
            User friendUser = f.getUser().getId().equals(userId) ? f.getFriend() : f.getUser();
            return mapToDTO(friendUser, f.getId());
        }).collect(Collectors.toList());
    }

    public List<FriendDTO> getPendingRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));
                
        // Queremos ver os pedidos onde NÓS somos o "friend" (o destinatário)
        List<Friendship> pending = friendshipRepository.findByFriendAndStatus(user, FriendshipStatus.PENDING);
        
        return pending.stream().map(f -> {
            // O remetente é o "user"
            return mapToDTO(f.getUser(), f.getId());
        }).collect(Collectors.toList());
    }

    public List<FriendDTO> getSentRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));
                
        // Queremos ver os pedidos onde NÓS somos o "user" (o remetente)
        List<Friendship> pending = friendshipRepository.findByUserAndStatus(user, FriendshipStatus.PENDING);
        
        return pending.stream().map(f -> {
            // O destinatário é o "friend"
            return mapToDTO(f.getFriend(), f.getId());
        }).collect(Collectors.toList());
    }

    private FriendDTO mapToDTO(User user, Long friendshipId) {
        return FriendDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .level(user.getLevel() != null ? user.getLevel() : 1)
                .currentLeague(user.getCurrentLeague())
                .isOnline(presenceService.isUserOnline(user.getId()))
                .friendshipId(friendshipId)
                .build();
    }
}
