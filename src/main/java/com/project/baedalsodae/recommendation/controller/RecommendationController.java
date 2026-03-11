package com.project.baedalsodae.recommendation.controller;

import com.project.baedalsodae.recommendation.dto.VoiceRecommendationRequest;
import com.project.baedalsodae.recommendation.dto.VoiceRecommendationResponse;
import com.project.baedalsodae.recommendation.service.MenuEmbeddingService;
import com.project.baedalsodae.recommendation.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final MenuEmbeddingService menuEmbeddingService;

    //    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_OWNER', 'ROLE_MANAGER',
    // 'ROLE_MASTER')")
    @PostMapping("/voice")
    public ResponseEntity<VoiceRecommendationResponse> recommendByVoice(
            //            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody VoiceRecommendationRequest request) {

        log.info("[Voice Recommendation] Input Text: {}", request.transcribedText());
        VoiceRecommendationResponse response =
                //                recommendationService.recommendMenuItems(request,
                // userDetails.getUserId());
                recommendationService.recommendMenuItems(request);
        log.info(
                "[Voice Recommendation] Response AI Message: '{}', Recommended Menu Count: {}",
                response.aiMessage(),
                response.recommendedMenus() != null ? response.recommendedMenus().size() : 0);
        return ResponseEntity.ok(response);
    }

    //    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_MASTER')")
    @PostMapping("/sync-embeddings")
    public ResponseEntity<String> syncAllEmbeddings() {
        menuEmbeddingService.syncAllMenuItemsToVectorStore();
        return ResponseEntity.ok("Successfully synchronized menu item embeddings.");
    }
}
