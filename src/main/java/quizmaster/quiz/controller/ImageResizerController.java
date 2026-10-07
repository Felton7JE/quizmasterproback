package quizmaster.quiz.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/images")
public class ImageResizerController {

    @GetMapping("/thumb")
    public ResponseEntity<byte[]> getThumbnail(@RequestParam String path, @RequestParam(defaultValue = "150") int width) {
        try {
            // Remove initial slash if present
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            
            Resource resource = new ClassPathResource("static/" + path);
            if (!resource.exists()) {
                log.warn("Thumbnail requested but file not found: {}", path);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            try (InputStream is = resource.getInputStream()) {
                BufferedImage originalImage = ImageIO.read(is);
                if (originalImage == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }

                int height = (int) (originalImage.getHeight() * ((double) width / originalImage.getWidth()));
                
                Image resultingImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(outputImage, "png", baos);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_PNG);
                headers.setCacheControl("public, max-age=86400"); // Cache for 1 day
                
                return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error("Error generating thumbnail for path: {}", path, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
