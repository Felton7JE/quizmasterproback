package quizmaster.quiz.service;

import quizmaster.quiz.models.User;
import quizmaster.quiz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;
import java.time.LocalDateTime;

@Service("userAuthService")
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    
    @Value("${google.client.id:SEU_CLIENT_ID_AQUI}")
    private String googleClientId;
    
    public User authenticateGoogle(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");

                // Try to find the user by username (which is email)
                return userRepository.findFirstByUsernameAndActiveTrue(email)
                        .orElseGet(() -> {
                            User newUser = new User();
                            newUser.setUsername(email);
                            newUser.setEmail(email);
                            // We can use the avatar from Google if needed, but not mapping it yet or we can map it
                            newUser.setCreatedAt(LocalDateTime.now());
                            newUser.setActive(true);
                            newUser.setCoins(500);
                            newUser.setCrystals(100);
                            newUser.setEnergy(5);
                            newUser.setLevel(1);
                            newUser.setTotalPoints(0);
                            newUser.setCurrentLeague(quizmaster.quiz.enums.League.BRONZE);
                            return userRepository.save(newUser);
                        });
            } else {
                throw new RuntimeException("Invalid Google Token");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Google Authentication failed: " + e.getMessage());
        }
    }
    
    public User authenticate(String username) {
        return userRepository.findFirstByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new RuntimeException("User not found or disabled"));
    }
    
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
}
