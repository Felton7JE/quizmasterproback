package quizmaster.quiz.controller;

import quizmaster.quiz.dto.*;
import quizmaster.quiz.service.AdminService;
import quizmaster.quiz.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {
    
    private final AdminService adminService;
    private final CategoryService categoryService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        AdminDashboardResponse dashboard = adminService.getDashboard();
        return ResponseEntity.ok(dashboard);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        Map<String, Object> stats = adminService.getSystemStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<UserResponse> users = adminService.getAllUsers(page, size);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/rooms")
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        List<RoomResponse> rooms = adminService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }
    
    @GetMapping("/games")
    public ResponseEntity<List<GameResponse>> getAllGames(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<GameResponse> games = adminService.getAllGames(page, size);
        return ResponseEntity.ok(games);
    }
    
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<Void> banUser(@PathVariable Long userId, @RequestParam String reason) {
        adminService.banUser(userId, reason);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/rooms/{roomCode}/close")
    public ResponseEntity<Void> closeRoom(@PathVariable String roomCode) {
        adminService.closeRoom(roomCode);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/maintenance")
    public ResponseEntity<Void> enableMaintenance(@RequestParam boolean enabled) {
        adminService.setMaintenanceMode(enabled);
        return ResponseEntity.ok().build();
    }
    
    // Category Management Endpoints
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories()
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/categories/active")
    public ResponseEntity<List<CategoryResponse>> getActiveCategories() {
        List<CategoryResponse> categories = categoryService.getAllActiveCategories()
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(categories);
    }
    
    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CreateCategoryRequest request) {
        var category = categoryService.createCategory(
                request.getName(),
                request.getDisplayName(),
                request.getDescription()
        );
        return ResponseEntity.ok(CategoryResponse.fromEntity(category));
    }
    
    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody UpdateCategoryRequest request) {
        var category = categoryService.updateCategory(
                id,
                request.getDisplayName(),
                request.getDescription(),
                request.getIsActive()
        );
        return ResponseEntity.ok(CategoryResponse.fromEntity(category));
    }
    
    @PostMapping("/categories/{id}/activate")
    public ResponseEntity<Void> activateCategory(@PathVariable Long id) {
        categoryService.activateCategory(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/categories/{id}/deactivate")
    public ResponseEntity<Void> deactivateCategory(@PathVariable Long id) {
        categoryService.deactivateCategory(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/categories/initialize")
    public ResponseEntity<Void> initializeDefaultCategories() {
        categoryService.initializeDefaultCategories();
        return ResponseEntity.ok().build();
    }
}
