package com.servicesync.sla.api;

import com.servicesync.sla.dto.SlaEventRequestDto;
import com.servicesync.sla.service.SlaProcessorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/v1/sla")
public class SlaEventController {

    private final SlaProcessorService slaProcessorService;

    public SlaEventController(SlaProcessorService slaProcessorService) {
        this.slaProcessorService = slaProcessorService;
    }

    @PostMapping("/events")
    public ResponseEntity<Void> handleSlaEvent(@RequestBody SlaEventRequestDto request) {
        slaProcessorService.processEvent(request);
        return ResponseEntity.accepted().build(); // 202 Accepted, processed asynchronously
    }
}
