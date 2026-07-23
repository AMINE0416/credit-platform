package org.eqdom.creditplatform.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Configuration de la DataSource avec support pour DATABASE_URL (Railway, Heroku, etc.)
 * Parse automatiquement DATABASE_URL au format postgres://user:pass@host:port/db
 * et le convertit en format JDBC jdbc:postgresql://host:port/db
 */
@Configuration
public class DataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");

        logger.info("=== DataSource Configuration ===");
        logger.info("DATABASE_URL exists: {}", databaseUrl != null);

        // Fallback pour le dev local (pas de DATABASE_URL en local)
        if (databaseUrl == null || databaseUrl.isBlank()) {
            logger.warn("DATABASE_URL not found, using localhost fallback for development");
            return DataSourceBuilder.create()
                    .url("jdbc:postgresql://localhost:5432/credit_platform")
                    .username(System.getenv().getOrDefault("DB_USERNAME", "postgres"))
                    .password(System.getenv().getOrDefault("DB_PASSWORD", "postgres123"))
                    .driverClassName("org.postgresql.Driver")
                    .build();
        }

        try {
            // Nettoyer l'URL (trim espaces, retours à la ligne, etc.)
            databaseUrl = databaseUrl.trim();
            logger.info("Parsing DATABASE_URL (length: {})...", databaseUrl.length());
            logger.info("Raw DATABASE_URL: [{}]", databaseUrl);
            
            URI dbUri = new URI(databaseUrl);
            String[] userInfo = dbUri.getUserInfo().split(":", 2);
            String username = userInfo[0];
            String password = userInfo.length > 1 ? userInfo[1] : "";

            String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbUri.getPort() + dbUri.getPath();

            logger.info("Database connection configured:");
            logger.info("  - Host: {}", dbUri.getHost());
            logger.info("  - Port: {}", dbUri.getPort());
            logger.info("  - Database: {}", dbUri.getPath());
            logger.info("  - Username: {}", username);
            logger.info("  - JDBC URL: {}", jdbcUrl);

            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();
        } catch (Exception e) {
            logger.error("Failed to parse DATABASE_URL: [{}]", databaseUrl, e);
            logger.error("URL length: {}, last 10 chars: [{}]", 
                        databaseUrl.length(), 
                        databaseUrl.substring(Math.max(0, databaseUrl.length() - 10)));
            throw new IllegalStateException("Impossible de parser DATABASE_URL : " + databaseUrl, e);
        }
    }
}
