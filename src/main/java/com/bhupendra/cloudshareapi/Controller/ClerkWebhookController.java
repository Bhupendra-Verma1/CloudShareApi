package com.bhupendra.cloudshareapi.Controller;

import com.bhupendra.cloudshareapi.dto.ProfileDTO;
import com.bhupendra.cloudshareapi.repository.FileMetadataRepository;
import com.bhupendra.cloudshareapi.service.FileMetadataService;
import com.bhupendra.cloudshareapi.service.ProfileService;
import com.bhupendra.cloudshareapi.service.UserCreditsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class ClerkWebhookController {
    @Value("${clerk.webhook.secret}")
    private String webhookSecret;

    private final ProfileService profileService;
    private final UserCreditsService userCreditsService;
    private final FileMetadataService fileMetadataService;

    @PostMapping("/clerk")
    public ResponseEntity<?> handleClerkWebhook(@RequestHeader("svix-id") String svixId,
                                                @RequestHeader("svix-timestamp") String svixTimestamp,
                                                @RequestHeader("svix-signature") String svixSignature,
                                                @RequestBody String payLoad) {
        try {
            boolean isValid = verifyWebhooksSignature(svixId, svixTimestamp, svixSignature, payLoad);
            if(!isValid ) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid webhook signature");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(payLoad);
            String eventType = rootNode.path("type").asText();

            switch (eventType) {
                case "user.created" :
                    handleUserCreated(rootNode.path("data"));
                    break;
                case "user.updated" :
                    handleUserUpdated(rootNode.path("data"));
                    break;
                case "user.deleted" :
                    handleUserDeleted(rootNode.path("data"));
                    break;
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    private void handleUserCreated(JsonNode data) {
        String clerkId = data.path("id").asText();

        String email = "";
        JsonNode emailAddresses = data.path("email_addresses");
        if(emailAddresses.isArray() && !emailAddresses.isEmpty()) {
            email = emailAddresses.get(0).path("email_address").asText();
        }

        String firstName = data.path("first_name").asText("");
        String lastName = data.path("last_name").asText("");
        String photoUrl = data.path("image_url").asText("");

        ProfileDTO profileDTO = ProfileDTO.builder()
                .clerkId(clerkId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .photoUrl(photoUrl)
                .build();

        profileService.createProfile(profileDTO);
        userCreditsService.createInitialCredits(clerkId);
    }

    private void handleUserUpdated(JsonNode data) {
        String clerkId = data.path("id").asText();

        String email = "";
        JsonNode emailAddresses = data.path("email_addresses");
        if(emailAddresses.isArray() && !emailAddresses.isEmpty()) {
            email = emailAddresses.get(0).path("email_address").asText();
        }

        String firstName = data.path("first_name").asText("");
        String lastName = data.path("last_name").asText("");
        String photoUrl = data.path("image_url").asText("");

        ProfileDTO updateProfile = ProfileDTO.builder()
                .clerkId(clerkId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .photoUrl(photoUrl)
                .build();

        updateProfile = profileService.createProfile(updateProfile);

        if(updateProfile == null) handleUserCreated(data);
    }

    private void handleUserDeleted(JsonNode data) {
        String clerkId = data.path("id").asText();
        userCreditsService.deleteUserCreditsIfExists(clerkId);
        fileMetadataService.deleteAllUserFiles(clerkId);
        profileService.deleteProfile(clerkId);
    }

    private boolean verifyWebhooksSignature(String svixId, String svixTimestamp, String svixSignature, String payLoad) {
        return true;
    }
}