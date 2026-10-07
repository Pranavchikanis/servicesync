package com.servicesync.core.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class EntityMappingTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testContextLoadsAndMappingIsCorrect() {
        // Just checking context loads and basic persist is enough to validate JPA mappings.
        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setPasswordHash("hash");
        admin.setRole(Role.ADMIN);
        admin.setName("Admin User");
        
        User savedAdmin = entityManager.persistAndFlush(admin);
        
        assertThat(savedAdmin.getId()).isNotNull();
        assertThat(savedAdmin.getCreatedAt()).isNotNull();
    }
}
