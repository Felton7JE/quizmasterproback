package quizmaster.quiz.service;

import lombok.RequiredArgsConstructor;
import quizmaster.quiz.dto.CategoryDistributionStatsResponse;
import quizmaster.quiz.dto.CategoryResponse;
import quizmaster.quiz.dto.AssignCategoryParams;
import quizmaster.quiz.dto.CreateRoomRequest;
import quizmaster.quiz.dto.GameResponse;
import quizmaster.quiz.dto.PlayerResponse;
import quizmaster.quiz.dto.RoomResponse;
import quizmaster.quiz.dto.UpdateRoomRequest;
import quizmaster.quiz.models.Category;
import quizmaster.quiz.enums.GameMode;
import quizmaster.quiz.enums.GameStatus;
import quizmaster.quiz.enums.RoomStatus;
import quizmaster.quiz.enums.Team;
import quizmaster.quiz.models.Game;
import quizmaster.quiz.models.Room;
import quizmaster.quiz.models.RoomPlayer;
import quizmaster.quiz.models.User;
import quizmaster.quiz.repository.GameRepository;
import quizmaster.quiz.repository.RoomPlayerRepository;
import quizmaster.quiz.repository.RoomRepository;
import quizmaster.quiz.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {
    
    private final RoomRepository roomRepository;
    private final RoomPlayerRepository roomPlayerRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final CategoryService categoryService;
    // NEW: publisher para SSE
    private final RoomEventsPublisher roomEventsPublisher;

    public RoomResponse createRoom(CreateRoomRequest request, Long hostId) {
        // Verificar se o usuário existe
        User host = userRepository.findById(hostId)
            .orElseThrow(() -> new RuntimeException("Host não encontrado"));
        
        // Criar a sala
        Room room = new Room();
        room.setRoomName(request.getRoomName());
        room.setRoomCode(generateRoomCode());
        room.setHost(host);
        room.setPassword(request.getPassword());
        room.setGameMode(request.getGameMode());
        room.setDifficulty(request.getDifficulty());
        room.setMaxPlayers(request.getMaxPlayers());
        room.setQuestionTime(request.getQuestionTime());
        room.setQuestionCount(request.getQuestionCount());
        
        // Processar categoryIds e buscar as categorias
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = new ArrayList<>();
            for (Long categoryId : request.getCategoryIds()) {
                Category category = categoryService.getCategoryById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Categoria não encontrada: " + categoryId));
                categories.add(category);
            }
            room.setCategories(categories);
        }
        
        room.setAssignmentType(request.getAssignmentType());
        room.setCategoryAssignmentMode(request.getCategoryAssignmentMode());
        room.setAllowSpectators(request.getAllowSpectators());
        room.setEnableChat(request.getEnableChat());
        room.setShowRealTimeRanking(request.getShowRealTimeRanking());
        room.setAllowReconnection(request.getAllowReconnection());
        room.setStatus(RoomStatus.WAITING);
        room.setCreatedAt(LocalDateTime.now());
        
        // ✅ CORREÇÃO: Inicializar lista antes de salvar
        room.setPlayers(new ArrayList<>());
        
        // Salvar a sala primeiro
        Room savedRoom = roomRepository.save(room);
        
        // ✅ NOVO: Adicionar o host como jogador da sala
        RoomPlayer hostPlayer = new RoomPlayer();
        hostPlayer.setRoom(savedRoom);
        hostPlayer.setUser(host);
        hostPlayer.setIsReady(false);  // ✅ CORREÇÃO: Host já está pronto
        hostPlayer.setIsHost(true);
        hostPlayer.setJoinedAt(LocalDateTime.now());

        roomPlayerRepository.save(hostPlayer);
        
        // ✅ IMPORTANTE: Recarregar a sala para ter a lista atualizada
        savedRoom = roomRepository.findById(savedRoom.getId()).orElse(savedRoom);
        
        return convertToRoomResponse(savedRoom);
    }

    public RoomResponse getRoomByCode(String roomCode) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));
        return convertToRoomResponse(room);
    }

    public List<RoomResponse> getAllRooms(String status) {
        return roomRepository.findByStatus(RoomStatus.valueOf(status.toUpperCase())).stream()
                .map(this::convertToRoomResponse)
                .collect(Collectors.toList());
    }

    public RoomResponse joinRoom(String roomCode, Long userId, String password) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        if (room.getStatus() != RoomStatus.WAITING) {
            throw new RuntimeException("Sala não está disponível para novos jogadores");
        }

        if (room.getPlayers().size() >= room.getMaxPlayers()) {
            throw new RuntimeException("Sala está cheia");
        }

        if (room.getPassword() != null && !room.getPassword().equals(password)) {
            throw new RuntimeException("Senha incorreta");
        }

        // Verificar se o usuário já está na sala
        boolean alreadyInRoom = room.getPlayers().stream()
                .anyMatch(p -> p.getUser().getId().equals(userId));
        
        if (alreadyInRoom) {
            throw new RuntimeException("Usuário já está na sala");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        RoomPlayer roomPlayer = new RoomPlayer();
        roomPlayer.setRoom(room);
        roomPlayer.setUser(user);
        roomPlayer.setIsReady(false); // Mudança aqui
        roomPlayer.setIsHost(false);  // Adicione esta linha
        roomPlayer.setJoinedAt(LocalDateTime.now());

        roomPlayerRepository.save(roomPlayer);

        return convertToRoomResponse(room);
    }

    public void leaveRoom(String roomCode, Long userId) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        RoomPlayer roomPlayer = room.getPlayers().stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Jogador não encontrado na sala"));

        roomPlayerRepository.delete(roomPlayer);

        // Se o host saiu, transferir para próximo jogador
        if (room.getHost().getId().equals(userId)) {
            transferHostToNextPlayer(room, userId);
        }

        // Se a sala ficou vazia, deletar
        if (room.getPlayers().size() <= 1) {
            roomRepository.delete(room);
        }
    }

    public void assignTeam(String roomCode, Long userId, String team) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));
        
        RoomPlayer roomPlayer = room.getPlayers().stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Jogador não encontrado na sala"));
        
        try {
            roomPlayer.setTeam(Team.valueOf(team.toUpperCase()));
            roomPlayerRepository.save(roomPlayer);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Time inválido: " + team);
        }
    }

    public void toggleReady(String roomCode, Long userId) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));
        
        RoomPlayer roomPlayer = room.getPlayers().stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Jogador não encontrado na sala"));
        
        roomPlayer.setIsReady(!roomPlayer.getIsReady()); // Mudança aqui
        roomPlayerRepository.save(roomPlayer);
    }

    public void kickPlayer(String roomCode, Long targetUserId, Long hostId) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        if (!room.getHost().getId().equals(hostId)) {
            throw new RuntimeException("Apenas o host pode expulsar jogadores");
        }

        RoomPlayer roomPlayer = room.getPlayers().stream()
                .filter(p -> p.getUser().getId().equals(targetUserId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Jogador não encontrado na sala"));

        roomPlayerRepository.delete(roomPlayer);
    }

    public RoomResponse updateRoomSettings(String roomCode, UpdateRoomRequest request, Long hostId) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        if (!room.getHost().getId().equals(hostId)) {
            throw new RuntimeException("Apenas o host pode alterar configurações da sala");
        }

        if (request.getName() != null) room.setRoomName(request.getName());
        if (request.getMaxPlayers() != null) room.setMaxPlayers(request.getMaxPlayers());
        if (request.getGameMode() != null) room.setGameMode(GameMode.valueOf(request.getGameMode().toUpperCase()));
        if (request.getPassword() != null) room.setPassword(request.getPassword());

        room = roomRepository.save(room);
        return convertToRoomResponse(room);
    }

    public GameResponse startGame(String roomCode, Long hostId) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        if (!room.getHost().getId().equals(hostId)) {
            throw new RuntimeException("Apenas o host pode iniciar o jogo");
        }

        if (room.getPlayers() == null || room.getPlayers().size() < 2) {
            throw new RuntimeException("É necessário pelo menos 2 jogadores para iniciar o jogo");
        }

        boolean allReady = room.getPlayers().stream()
                .allMatch(player -> player.getIsReady());
        
        if (!allReady) {
            throw new RuntimeException("Nem todos os jogadores estão prontos");
        }

        // NEW: alinhar com o app - STARTING + startsAt para sincronizar countdown
        LocalDateTime startsAt = LocalDateTime.now().plusSeconds(3);
        room.setStatus(RoomStatus.STARTING);
        room.setStartsAt(startsAt);
        roomRepository.save(room);

        // Criar o jogo imediatamente (o estado do jogo em si fica IN_PROGRESS)
        Game game = new Game();
        game.setRoom(room);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setStartedAt(LocalDateTime.now());
        game = gameRepository.save(game);

        // Notificar clientes (web/mobile) em tempo real
        roomEventsPublisher.publishRoomStarted(room.getRoomCode(), startsAt, game.getId());

        // Resposta
        GameResponse response = new GameResponse();
        response.setId(game.getId());
        response.setRoomCode(room.getRoomCode());
        response.setStatus(game.getStatus().toString());
        response.setCurrentQuestionIndex(0);
        response.setTotalQuestions(room.getQuestionCount());
        response.setGameMode(room.getGameMode().toString());
        response.setDifficulty(room.getDifficulty().toString());
        response.setStartTime(game.getStartedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        return response;
    }

    private void transferHostToNextPlayer(Room room, Long currentHostId) {
        List<RoomPlayer> otherPlayers = room.getPlayers().stream()
                .filter(p -> !p.getUser().getId().equals(currentHostId))
                .collect(Collectors.toList());

        if (!otherPlayers.isEmpty()) {
            User newHost = otherPlayers.get(0).getUser();
            room.setHost(newHost);
            roomRepository.save(room);
        }
    }

    private String generateRoomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        
        do {
            code.setLength(0);
            for (int i = 0; i < 6; i++) {
                code.append(chars.charAt(random.nextInt(chars.length())));
            }
        } while (roomRepository.findByRoomCode(code.toString()).isPresent());
        
        return code.toString();
    }

    private RoomResponse convertToRoomResponse(Room room) {
        RoomResponse response = new RoomResponse();
        response.setId(room.getId());
        response.setRoomName(room.getRoomName());
        response.setRoomCode(room.getRoomCode());
        
        // ✅ CORREÇÃO: Use setHostName()
        response.setHostName(room.getHost() != null ? room.getHost().getUsername() : null);
        
        // map enums to objects
        response.setGameMode(room.getGameMode());
        response.setDifficulty(room.getDifficulty());
        response.setMaxPlayers(room.getMaxPlayers());
        response.setQuestionTime(room.getQuestionTime());
        response.setQuestionCount(room.getQuestionCount());
        
        // Converter categorias para CategoryResponse
        if (room.getCategories() != null) {
            List<CategoryResponse> categoryResponses = room.getCategories().stream()
                    .map(CategoryResponse::fromEntity)
                    .collect(Collectors.toList());
            response.setCategories(categoryResponses);
        }
        
        response.setAssignmentType(room.getAssignmentType());
        response.setCategoryAssignmentMode(room.getCategoryAssignmentMode());
        // status como string
        response.setStatus(room.getStatus());
        response.setCreatedAt(room.getCreatedAt());
        response.setAllowSpectators(room.getAllowSpectators());
        response.setEnableChat(room.getEnableChat());
        response.setAssignmentType(room.getAssignmentType());
        response.setShowRealTimeRanking(room.getShowRealTimeRanking());
        response.setAllowReconnection(room.getAllowReconnection());
        response.setIsPrivate(room.getPassword() != null);
        
        // players -> PlayerResponse
        if (room.getPlayers() != null) {
            response.setCurrentPlayers(room.getPlayers().size());
            List<PlayerResponse> players = room.getPlayers().stream()
                .map(this::convertToPlayerResponse)
                .collect(Collectors.toList());
            response.setPlayers(players);
        } else {
            response.setCurrentPlayers(0);
            response.setPlayers(new ArrayList<>());
        }

        // NEW: startsAt como LocalDateTime
        response.setStartsAt(room.getStartsAt());
        
        return response;
    }
    
    private PlayerResponse convertToPlayerResponse(RoomPlayer roomPlayer) {
        if (roomPlayer == null) return null;
        
        PlayerResponse response = new PlayerResponse();
        response.setUserId(roomPlayer.getUser().getId());
        response.setUsername(roomPlayer.getUser().getUsername());
        response.setAvatar(roomPlayer.getUser().getAvatar());
        response.setIsReady(roomPlayer.getIsReady()); // Mudança aqui
        response.setTeam(roomPlayer.getTeam());
        response.setPreferredCategory(roomPlayer.getPreferredCategory());
        response.setAssignedCategory(roomPlayer.getAssignedCategory());
        response.setIsHost(roomPlayer.getIsHost()); // Mudança aqui
        // isLeader removed — use isHost only

        return response;
    }
    
    public void deleteRoom(String roomCode, Long hostId) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        if (!room.getHost().getId().equals(hostId)) {
            throw new RuntimeException("Apenas o host pode deletar a sala");
        }

        roomRepository.delete(room);
    }
    
    public List<PlayerResponse> getRoomPlayers(String roomCode) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));
        
        return room.getPlayers().stream()
                .map(this::convertToPlayerResponse)
                .collect(Collectors.toList());
    }
    
    // Novos métodos para atribuição de disciplinas
    
    public void assignCategoryToPlayer(String roomCode, Long playerId, Long categoryId) {
        AssignCategoryParams params = new AssignCategoryParams();
        params.setRoomCode(roomCode);
        params.setPlayerId(playerId);
        params.setCategoryId(categoryId);
        assignCategoryToPlayer(params);
    }
    
    // Novo método: usa DTO de parâmetro para padronização
    public void assignCategoryToPlayer(AssignCategoryParams params) {
        String roomCode = params.getRoomCode();
        Long playerId = params.getPlayerId();
        Long categoryId = params.getCategoryId();

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        RoomPlayer roomPlayer = room.getPlayers().stream()
                .filter(p -> p.getUser().getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Jogador não encontrado na sala"));

        if (roomPlayer.getTeam() == null) {
            throw new RuntimeException("Jogador deve estar em uma equipe antes de selecionar disciplina");
        }

        Category category = categoryService.getCategoryById(categoryId)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        if (!isCategoryAvailableForTeam(room, category, roomPlayer.getTeam())) {
            throw new RuntimeException("Esta disciplina já está ocupada por outro jogador da sua equipe");
        }

        if (!room.getCategories().contains(category)) {
            throw new RuntimeException("Disciplina não disponível nesta sala");
        }

        roomPlayer.setAssignedCategory(category);
        roomPlayerRepository.save(roomPlayer);
    }
    
  
  
    public void distributeCategoriesAutomatically(String roomCode, Long hostId) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));
        
        if (!room.getHost().getId().equals(hostId)) {
            throw new RuntimeException("Apenas o host pode distribuir disciplinas automaticamente");
        }
        
        // Obter jogadores divididos por equipe
        List<RoomPlayer> teamAPlayers = room.getPlayers().stream()
                .filter(p -> p.getTeam() == Team.A)
                .collect(Collectors.toList());
        
        List<RoomPlayer> teamBPlayers = room.getPlayers().stream()
                .filter(p -> p.getTeam() == Team.B)
                .collect(Collectors.toList());
        
        // Limpar atribuições anteriores
        room.getPlayers().forEach(player -> {
            player.setAssignedCategory(null);
            roomPlayerRepository.save(player);
        });
        
        // Distribuir disciplinas de forma balanceada
        Random random = new Random();
        List<Category> availableCategories = new ArrayList<>(room.getCategories());
        
        // Distribuir para equipe A
        distributeCategoriesToTeam(teamAPlayers, availableCategories, random);
        
        // Distribuir para equipe B
        distributeCategoriesToTeam(teamBPlayers, availableCategories, random);
    }
    
    private void distributeCategoriesToTeam(List<RoomPlayer> teamPlayers, List<Category> availableCategories, Random random) {
        for (RoomPlayer player : teamPlayers) {
            if (!availableCategories.isEmpty()) {
                int categoryIndex = random.nextInt(availableCategories.size());
                Category selectedCategory = availableCategories.get(categoryIndex);

                player.setAssignedCategory(selectedCategory);
                roomPlayerRepository.save(player);

                // Remover categoria da lista para que não seja atribuída novamente para a mesma equipe
                // mas mantê-la disponível para a outra equipe
                // availableCategories.remove(categoryIndex); // Comentado para permitir que ambas as equipes usem a mesma disciplina
            }
        }
    }
    
    private boolean isCategoryAvailableForTeam(Room room, Category category, Team team) {
        return room.getPlayers().stream()
                .filter(p -> p.getTeam() == team)
                .noneMatch(p -> category.equals(p.getAssignedCategory()));
    }
    
    public CategoryDistributionStatsResponse getCategoryDistributionStats(String roomCode) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));
        
        CategoryDistributionStatsResponse response = new CategoryDistributionStatsResponse();
        Map<Category, CategoryDistributionStatsResponse.CategoryTeamStats> distribution = new HashMap<>();
        
        for (Category category : room.getCategories()) {
            int teamAPlayers = (int) room.getPlayers().stream()
                    .filter(p -> p.getTeam() == Team.A && category.equals(p.getAssignedCategory()))
                    .count();
            
            int teamBPlayers = (int) room.getPlayers().stream()
                    .filter(p -> p.getTeam() == Team.B && category.equals(p.getAssignedCategory()))
                    .count();
            
            CategoryDistributionStatsResponse.CategoryTeamStats stats = 
                    new CategoryDistributionStatsResponse.CategoryTeamStats(teamAPlayers, teamBPlayers, teamAPlayers + teamBPlayers);
            
            distribution.put(category, stats);
        }
        
        response.setDistribution(distribution);
        return response;
    }
    
    public List<Category> getAvailableCategoriesForPlayer(String roomCode, Long playerId) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));
        
        RoomPlayer roomPlayer = room.getPlayers().stream()
                .filter(p -> p.getUser().getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Jogador não encontrado na sala"));
        
        if (roomPlayer.getTeam() == null) {
            return new ArrayList<>();
        }
        
        // Obter disciplinas já ocupadas pela equipe do jogador
        List<Category> occupiedCategories = room.getPlayers().stream()
                .filter(p -> p.getTeam() == roomPlayer.getTeam())
                .map(RoomPlayer::getAssignedCategory)
                .filter(category -> category != null)
                .collect(Collectors.toList());
        
        // Retornar disciplinas disponíveis (não ocupadas pela equipe)
        return room.getCategories().stream()
                .filter(category -> !occupiedCategories.contains(category))
                .collect(Collectors.toList());
    }
    
    public boolean areAllCategoriesAssigned(String roomCode) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));
        
        List<RoomPlayer> playersWithTeams = room.getPlayers().stream()
                .filter(p -> p.getTeam() != null)
                .collect(Collectors.toList());
        
        if (playersWithTeams.isEmpty()) {
            return false;
        }
        
        return playersWithTeams.stream()
                .allMatch(p -> p.getAssignedCategory() != null);
    }
}
 