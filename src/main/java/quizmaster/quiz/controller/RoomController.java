package quizmaster.quiz.controller;

import quizmaster.quiz.dto.*;
import quizmaster.quiz.models.Category;
import quizmaster.quiz.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import quizmaster.quiz.service.RoomEventsPublisher;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoomController {

    private final RoomService roomService;
    private final RoomEventsPublisher roomEventsPublisher;

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        RoomResponse room = roomService.createRoom(request, request.getHostId());
        return ResponseEntity.ok(room);
    }

    @GetMapping("/{roomCode}")
    public ResponseEntity<RoomResponse> getRoomByCode(@PathVariable String roomCode) {
        RoomResponse room = roomService.getRoomByCode(roomCode);
        return ResponseEntity.ok(room);
    }

    @PostMapping("/filter")
    public ResponseEntity<List<RoomResponse>> getAllRooms(@Valid @RequestBody FilterRoomsRequest request) {
        List<RoomResponse> rooms = roomService.getAllRooms(request.getStatus());
        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/{roomCode}/join")
    public ResponseEntity<RoomResponse> joinRoom(
            @PathVariable String roomCode,
            @Valid @RequestBody JoinRoomRequest request) {
        RoomResponse room = roomService.joinRoom(roomCode, request.getUserId(), request.getPassword());
        return ResponseEntity.ok(room);
    }

    @PostMapping("/{roomCode}/leave")
    public ResponseEntity<Void> leaveRoom(
            @PathVariable String roomCode,
            @Valid @RequestBody LeaveRoomRequest request) {
        roomService.leaveRoom(roomCode, request.getUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomCode}/start")
    public ResponseEntity<GameResponse> startGame(
            @PathVariable String roomCode,
            @Valid @RequestBody StartGameRequest request) {
        GameResponse game = roomService.startGame(roomCode, request.getHostId());
        return ResponseEntity.ok(game);
    }

    @PostMapping("/{roomCode}/players/{userId}/team")
    public ResponseEntity<Void> assignTeam(
            @PathVariable String roomCode,
            @PathVariable Long userId,
            @Valid @RequestBody AssignTeamRequest request) {
        roomService.assignTeam(roomCode, userId, request.getTeam());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomCode}/players/{userId}/ready")
    public ResponseEntity<Void> toggleReady(
            @PathVariable String roomCode,
            @PathVariable Long userId) {
        roomService.toggleReady(roomCode, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{roomCode}/settings")
    public ResponseEntity<RoomResponse> updateRoomSettings(
            @PathVariable String roomCode,
            @Valid @RequestBody UpdateRoomRequest request) {
        RoomResponse room = roomService.updateRoomSettings(roomCode, request, request.getHostId());
        return ResponseEntity.ok(room);
    }

    @DeleteMapping("/{roomCode}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable String roomCode,
            @Valid @RequestBody DeleteRoomRequest request) {
        roomService.deleteRoom(roomCode, request.getHostId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomCode}/kick")
    public ResponseEntity<Void> kickPlayer(
            @PathVariable String roomCode,
            @Valid @RequestBody KickPlayerRequest request) {
        roomService.kickPlayer(roomCode, request.getHostId(), request.getPlayerId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roomCode}/players")
    public ResponseEntity<List<PlayerResponse>> getRoomPlayers(@PathVariable String roomCode) {
        List<PlayerResponse> players = roomService.getRoomPlayers(roomCode);
        return ResponseEntity.ok(players);
    }

    // Novos endpoints para atribuição de disciplinas

    @PostMapping("/{roomCode}/assign-category")
    public ResponseEntity<Void> assignCategoryToPlayer(
            @PathVariable String roomCode,
            @Valid @RequestBody AssignCategoryRequest request) {
        AssignCategoryParams params = new AssignCategoryParams();
        params.setRoomCode(roomCode);
        params.setPlayerId(request.getPlayerId());
        params.setCategoryId(request.getCategoryId());
        roomService.assignCategoryToPlayer(params);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomCode}/distribute-categories")
    public ResponseEntity<Void> distributeCategoriesAutomatically(
            @PathVariable String roomCode,
            @Valid @RequestBody DistributeCategoriesRequest request) {
        roomService.distributeCategoriesAutomatically(roomCode, request.getHostId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roomCode}/category-distribution")
    public ResponseEntity<CategoryDistributionStatsResponse> getCategoryDistributionStats(
            @PathVariable String roomCode) {
        CategoryDistributionStatsResponse stats = roomService.getCategoryDistributionStats(roomCode);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{roomCode}/players/{playerId}/available-categories")
    public ResponseEntity<List<Category>> getAvailableCategoriesForPlayer(
            @PathVariable String roomCode,
            @PathVariable Long playerId) {
        List<Category> categories = roomService.getAvailableCategoriesForPlayer(roomCode, playerId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{roomCode}/categories-assignment-status")
    public ResponseEntity<Boolean> areAllCategoriesAssigned(
            @PathVariable String roomCode) {
        boolean allAssigned = roomService.areAllCategoriesAssigned(roomCode);
        return ResponseEntity.ok(allAssigned);
    }

    // NEW: SSE subscribe
    @GetMapping("/{roomCode}/events")
    public SseEmitter subscribeRoomEvents(@PathVariable String roomCode) {
        return roomEventsPublisher.subscribe(roomCode);
    }


}
