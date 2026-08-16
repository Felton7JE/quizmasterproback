package quizmaster.quiz.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quizmaster.quiz.dto.CreateBugReportRequest;
import quizmaster.quiz.models.BugReport;
import quizmaster.quiz.models.User;
import quizmaster.quiz.repository.BugReportRepository;
import quizmaster.quiz.repository.UserRepository;

@RestController
@RequestMapping("/api/bugs")
@RequiredArgsConstructor
public class BugReportController {

    private final BugReportRepository bugReportRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Void> createBugReport(@Valid @RequestBody CreateBugReportRequest request) {
        BugReport report = new BugReport();
        report.setDescription(request.getDescription());
        
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId()).orElse(null);
            report.setUser(user);
        }
        
        bugReportRepository.save(report);
        return ResponseEntity.ok().build();
    }
}
