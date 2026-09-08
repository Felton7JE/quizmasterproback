package quizmaster.quiz.dto;

import lombok.Builder;
import lombok.Data;
import quizmaster.quiz.enums.League;

@Data
@Builder
public class FriendDTO {
    private Long id;
    private String username;
    private String avatar;
    private Integer level;
    private League currentLeague;
    private boolean isOnline;
    
    // Para identificar se é um pedido pendente que nós recebemos (para a aba de Pedidos)
    private Long friendshipId;
}
