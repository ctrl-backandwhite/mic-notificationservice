package com.backandwhite.config;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

@Log4j2
@Testcontainers
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@Import(TestContainersConfiguration.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegration {

    public static final String PREFIX = "hibernate_";
    private static final Pattern VALID_TABLE_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    private static final Set<String> EXCLUDED_TABLE_PREFIXES = Set.of(PREFIX, "flyway_", "liquibase_");

    @Autowired
    private JwtTestUtil jwtTestUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    protected WebTestClient webTestClient;

    static {
        org.testcontainers.utility.TestcontainersConfiguration.getInstance().updateUserConfig("checks.disable", "true");
    }

    public String getToken(List<String> roles) {
        return jwtTestUtil.getToken("test-user@example.com", roles);
    }

    @BeforeEach
    public void cleanAllTables() {
        if (webTestClient == null) {
            webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        }
        log.debug("Starting cleanup of all tables...");

        List<String> tableNames;
        try {
            tableNames = jdbcTemplate.queryForList("SELECT tablename FROM pg_tables WHERE schemaname = 'public'",
                    String.class);
        } catch (Exception e) {
            log.error("Error getting table names: {}", e.getMessage());
            throw new IllegalStateException("Could not retrieve table names.", e);
        }

        List<String> tablesToTruncate = tableNames.stream()
                .filter(tableName -> EXCLUDED_TABLE_PREFIXES.stream().noneMatch(tableName::startsWith))
                .filter(tableName -> {
                    if (!VALID_TABLE_NAME_PATTERN.matcher(tableName).matches()) {
                        log.warn("Invalid table name '{}', will not truncate.", tableName);
                        return false;
                    }
                    return true;
                }).toList();

        if (!tablesToTruncate.isEmpty()) {
            String joined = tablesToTruncate.stream().map(t -> "\"" + t + "\"")
                    .collect(java.util.stream.Collectors.joining(", "));
            try {
                jdbcTemplate.execute("TRUNCATE TABLE " + joined + " RESTART IDENTITY CASCADE"); // NOSONAR — table names
                                                                                                // validated by
                                                                                                // VALID_TABLE_NAME_PATTERN
                                                                                                // regex
                log.debug("Tables truncated: {}", joined);
            } catch (Exception e) {
                log.error("Error truncating tables '{}': {}", joined, e.getMessage());
            }
        }
        log.debug("Table cleanup completed.");
    }
}
