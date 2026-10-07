package com.servicesync.sla.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "core-service", url = "${core.service.url:http://localhost:8080}")
public interface CoreTicketClient {

    @GetMapping("/api/internal/v1/tickets/breaches")
    List<Long> getSlaBreachedTicketIds(@RequestParam("threshold") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime threshold);
}
