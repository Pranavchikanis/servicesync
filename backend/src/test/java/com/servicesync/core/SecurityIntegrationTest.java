package com.servicesync.core;

import com.servicesync.core.api.dto.LoginRequest;
import com.servicesync.core.api.dto.AuthResponse;
import com.servicesync.core.api.dto.PartConsumptionRequest;
import com.servicesync.core.domain.Inventory;
import com.servicesync.core.domain.Role;
import com.servicesync.core.domain.Ticket;
import com.servicesync.core.domain.TicketStatus;
import com.servicesync.core.domain.User;
import com.servicesync.core.repository.InventoryRepository;
import com.servicesync.core.repository.TicketRepository;
import com.servicesync.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SecurityIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long techATicketId;
    private Long inventoryId;
    private String techBToken;

    @BeforeEach
    public void setup() {
        ticketRepository.deleteAll();
        inventoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create Tech A
        User techA = new User();
        techA.setName("Tech A");
        techA.setEmail("techa@test.com");
        techA.setPasswordHash(passwordEncoder.encode("password"));
        techA.setRole(Role.TECH);
        techA = userRepository.save(techA);

        // Create Tech B
        User techB = new User();
        techB.setName("Tech B");
        techB.setEmail("techb@test.com");
        techB.setPasswordHash(passwordEncoder.encode("password"));
        techB.setRole(Role.TECH);
        techB = userRepository.save(techB);

        // Create Inventory
        Inventory part = new Inventory();
        part.setSku("SEC-PART-1");
        part.setPartName("Security Part");
        part.setQuantityInStock(10);
        part.setPrice(BigDecimal.valueOf(10.0));
        part = inventoryRepository.save(part);
        inventoryId = part.getId();

        // Create Ticket and assign to Tech A in DIAGNOSING state
        Ticket ticket = new Ticket();
        ticket.setCustomerName("Test Customer");
        ticket.setCustomerPhone("555-1234");
        ticket.setDeviceInfo("Test Device");
        ticket.setIssueDesc("Test Issue");
        ticket.setStatus(TicketStatus.DIAGNOSING);
        ticket.setCreatedBy(techA);
        ticket.setTechnician(techA);
        ticket = ticketRepository.save(ticket);
        techATicketId = ticket.getId();

        // Login as Tech B
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("techb@test.com");
        loginReq.setPassword("password");
        ResponseEntity<AuthResponse> loginRes = restTemplate.postForEntity("/api/v1/auth/login", loginReq, AuthResponse.class);
        techBToken = Objects.requireNonNull(loginRes.getBody()).getToken();
    }

    @Test
    public void testIdorPreventionOnAddPart() {
        // Tech B tries to add a part to Tech A's ticket
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(techBToken);

        PartConsumptionRequest partReq = new PartConsumptionRequest();
        partReq.setInventoryId(inventoryId);
        partReq.setQuantity(1);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/tickets/" + techATicketId + "/parts",
                HttpMethod.POST,
                new HttpEntity<>(partReq, headers),
                String.class
        );

        // Should return 403 Forbidden due to IDOR check
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
