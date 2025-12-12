package com.bhupendra.cloudshareapi.service;

import com.bhupendra.cloudshareapi.document.UserCredits;
import com.bhupendra.cloudshareapi.repository.UserCreditsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCreditsService {

    private final UserCreditsRepository userCreditsRepository;
    private final ProfileService profileService;

    public UserCredits createInitialCredits(String clerkId) {
        UserCredits userCredits = UserCredits.builder()
                .clerkId(clerkId)
                .credits(5)
                .plan("BASIC")
                .build();
        return userCreditsRepository.save(userCredits);
    }

    public UserCredits getUserCredits(String clerkId) {
        return userCreditsRepository.findByClerkId(clerkId)
                .orElseGet(() -> createInitialCredits(clerkId));
    }

    public UserCredits getUserCredits() {
        String clerkId = profileService.getCurrentProfile().getClerkId();
        return getUserCredits(clerkId);
    }

    public Boolean haveEnoughCredits(int requiredCredits) {
        UserCredits credits = getUserCredits();
        return credits.getCredits() >= requiredCredits;
    }

    public UserCredits consumeCredit() {
        UserCredits userCredits = getUserCredits();

        if(userCredits.getCredits() <= 0) {
            return null;
        }

        userCredits.setCredits(userCredits.getCredits() - 1);
        return userCreditsRepository.save(userCredits);
    }

    public UserCredits addCredits(String clerkId, int creditsToAdd, String plan) {
        UserCredits userCredits = userCreditsRepository.findByClerkId(clerkId)
                .orElseGet(() -> createInitialCredits(clerkId));

        userCredits.setCredits(userCredits.getCredits() + creditsToAdd);
        userCredits.setPlan(plan);

        return userCreditsRepository.save(userCredits);
    }

    public void deleteUserCreditsIfExists(String clerk) {
        UserCredits existingCredits = userCreditsRepository.findByClerkId(clerk).orElse(null);
        if(existingCredits != null) {
            userCreditsRepository.delete(existingCredits);
        }
    }

}
