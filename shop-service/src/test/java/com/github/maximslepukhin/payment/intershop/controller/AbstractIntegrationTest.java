package com.github.maximslepukhin.payment.intershop.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
@ImportTestcontainers
@AutoConfigureMockMvc
@ContextConfiguration(initializers = AbstractIntegrationTest.Initializer.class)
public class AbstractIntegrationTest {

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("junit")
            .withPassword("junit");

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext ctx) {
            postgres.start();

            String r2dbcUrl = "r2dbc:postgresql://"
                              + postgres.getHost() + ":" + postgres.getFirstMappedPort()
                              + "/" + postgres.getDatabaseName();

            TestPropertyValues.of(
                    "spring.r2dbc.url=" + r2dbcUrl,
                    "spring.r2dbc.username=" + postgres.getUsername(),
                    "spring.r2dbc.password=" + postgres.getPassword()
            ).applyTo(ctx.getEnvironment());
        }
    }

    @Autowired
    private DatabaseClient databaseClient;

    @Test
    void testReactiveConnectionWithBlock() {
        assertNotNull(databaseClient);

        Integer result = databaseClient.sql("SELECT 1")
                .map((row, meta) -> row.get(0, Integer.class))
                .one()
                .block();

        assertNotNull(result);
        assertEquals(1, result);
    }
}