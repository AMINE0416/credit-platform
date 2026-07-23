package org.eqdom.creditplatform.config;

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

    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");

        // Fallback pour le dev local (pas de DATABASE_URL en local)
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return DataSourceBuilder.create()
                    .url("jdbc:postgresql://localhost:5432/credit_platform")
                    .username(System.getenv().getOrDefault("DB_USERNAME", "postgres"))
                    .password(System.getenv().getOrDefault("DB_PASSWORD", "postgres123"))
                    .driverClassName("org.postgresql.Driver")
                    .build();
        }

        try {
            URI dbUri = new URI(databaseUrl);
            String[] userInfo = dbUri.getUserInfo().split(":", 2);
            String username = userInfo[0];
            String password = userInfo.length > 1 ? userInfo[1] : "";

            String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbUri.getPort() + dbUri.getPath();

            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de parser DATABASE_URL : " + databaseUrl, e);
        }
    }
}
