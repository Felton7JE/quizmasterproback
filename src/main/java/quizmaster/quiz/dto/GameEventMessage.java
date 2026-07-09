package quizmaster.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Mensagem enviada via WebSocket STOMP para todos os clientes da sala/jogo.
 * O campo "type" identifica o tipo de evento:
 *   - GAME_STARTED: o host iniciou o jogo
 *   - PLAYER_JOINED / PLAYER_LEFT: jogador entrou ou saiu
 *   - ROOM_UPDATED: dados da sala foram atualizados
 *   - NEXT_QUESTION: próxima pergunta do modo Kahoot (enviada pelo servidor)
 *   - GAME_ENDED: jogo finalizado (modo Kahoot)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameEventMessage {

    /** Tipo do evento, ex: "GAME_STARTED", "ROOM_UPDATED", "NEXT_QUESTION", "GAME_ENDED" */
    private String type;

    /** Código da sala (sempre presente) */
    private String roomCode;

    /** ID do jogo (presente em eventos de jogo) */
    private Long gameId;

    /** Timestamp (epoch ms) em que o jogo começa (presente em GAME_STARTED) */
    private Long startsAt;

    /**
     * Lista de jogadores com as suas categorias atribuídas.
     * Cada entrada: { "userId": 1, "category": "História" }
     * Presente em GAME_STARTED para que cada cliente descubra a sua categoria.
     */
    private List<Map<String, Object>> playerCategories;

    // ---- Campos específicos do modo Kahoot ----

    /** Índice da pergunta atual (0-based). Presente em NEXT_QUESTION. */
    private Integer questionIndex;

    /** Total de perguntas no jogo. Presente em NEXT_QUESTION. */
    private Integer totalQuestions;

    /** Indica se esta é a última pergunta. Presente em NEXT_QUESTION. */
    private Boolean isLastQuestion;

    /** Dados da pergunta (id, texto, opções, resposta correta). Presente em NEXT_QUESTION. */
    private Map<String, Object> questionData;

    /** Payload genérico (usado para enviar Leaderboard, etc) */
    private Object payload;
}
