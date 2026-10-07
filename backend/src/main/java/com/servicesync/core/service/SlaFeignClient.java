package com.servicesync.core.service;

import com.servicesync.core.api.dto.SlaEventRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "sla-service", url = "${sla.service.url}")
public interface SlaFeignClient {

    @PostMapping("/api/internal/v1/sla/events")
    void sendSlaEvent(@RequestBody SlaEventRequest request);
}
