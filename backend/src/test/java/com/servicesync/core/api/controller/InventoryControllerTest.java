package com.servicesync.core.api.controller;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.servicesync.core.domain.Inventory;
import com.servicesync.core.security.CustomUserDetailsService;
import com.servicesync.core.security.JwtAuthenticationFilter;
import com.servicesync.core.security.JwtUtil;
import com.servicesync.core.security.SecurityConfig;
import com.servicesync.core.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
public class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        Claim roleClaim = mock(Claim.class);
        Claim userIdClaim = mock(Claim.class);

        when(roleClaim.asString()).thenReturn("TECH");
        when(userIdClaim.asLong()).thenReturn(2L);

        when(decodedJWT.getSubject()).thenReturn("tech@example.com");
        when(decodedJWT.getClaim("role")).thenReturn(roleClaim);
        when(decodedJWT.getClaim("userId")).thenReturn(userIdClaim);

        when(jwtUtil.validateTokenAndGetDecodedJWT("mock-token")).thenReturn(decodedJWT);
    }

    @Test
    void testListInventory() throws Exception {
        Inventory inv = new Inventory();
        inv.setId(1L);
        inv.setSku("PART-123");
        inv.setPartName("Screen");
        inv.setQuantityInStock(10);
        inv.setPrice(new BigDecimal("99.99"));

        when(inventoryService.getPaginatedInventory(any(Pageable.class), isNull()))
                .thenReturn(new PageImpl<>(List.of(inv)));

        mockMvc.perform(get("/api/v1/inventory")
                .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value("PART-123"));
    }
}
