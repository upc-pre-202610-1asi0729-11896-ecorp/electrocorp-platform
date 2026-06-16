package com.electrocorp.electrocorpplatform.reporting.interfaces.rest.controllers;

import com.electrocorp.electrocorpplatform.reporting.application.services.PlatformInsightApplicationService;
import com.electrocorp.electrocorpplatform.reporting.interfaces.rest.resources.PlatformSummaryResource;
import com.electrocorp.electrocorpplatform.shared.infrastructure.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reporting/platform")
@RequiredArgsConstructor
public class PlatformInsightController {

    private final PlatformInsightApplicationService platformInsightApplicationService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/summary")
    public PlatformSummaryResource getPlatformSummary() {
        var summary = platformInsightApplicationService.getPlatformSummary(currentUserProvider.getCurrentUserId());
        return PlatformSummaryResource.from(summary);
    }
}
