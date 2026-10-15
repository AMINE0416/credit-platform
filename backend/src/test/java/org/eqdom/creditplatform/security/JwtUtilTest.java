package org.eqdom.creditplatform.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil — Tests unitaires")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    // Secret de 32+ caractères (requis par HMAC-SHA)
    private static final String TEST_SECRET = "test-secret-key-minimum-32-chars-eqdom";
    private static final long EXPIRATION_MS = 3_600_000L; // 1 heure

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", EXPIRATION_MS);
    }

    // ──────────────────────────────────────────────────
    // generateToken
    // ──────────────────────────────────────────────────

    @Test
    @DisplayName("generateToken → retourne un token non vide")
    void generateToken_shouldReturnNonBlankToken() {
        String token = jwtUtil.generateToken("agent@eqdom.ma", List.of("AGENT"));
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("generateToken → token est au format JWT (3 parties séparées par '.')")
    void generateToken_shouldBeValidJwtFormat() {
        String token = jwtUtil.generateToken("admin@eqdom.ma", List.of("ADMIN"));
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
    }

    @Test
    @DisplayName("generateToken → deux tokens pour des emails différents sont différents")
    void generateToken_twoTokensForDifferentEmailsAreDifferent() {
        String token1 = jwtUtil.generateToken("agent1@eqdom.ma", List.of("AGENT"));
        String token2 = jwtUtil.generateToken("agent2@eqdom.ma", List.of("AGENT"));
        assertThat(token1).isNotEqualTo(token2);
    }

    // ──────────────────────────────────────────────────
    // extractUsername
    // ──────────────────────────────────────────────────

    @Test
    @DisplayName("extractUsername → retourne l'email encodé dans le token")
    void extractUsername_shouldReturnCorrectEmail() {
        String email = "agent@eqdom.ma";
        String token = jwtUtil.generateToken(email, List.of("AGENT"));

        String extracted = jwtUtil.extractUsername(token);

        assertThat(extracted).isEqualTo(email);
    }

    @Test
    @DisplayName("extractUsername → fonctionne avec email ADMIN")
    void extractUsername_shouldWorkForAdminEmail() {
        String email = "admin@eqdom.ma";
        String token = jwtUtil.generateToken(email, List.of("ADMIN"));

        assertThat(jwtUtil.extractUsername(token)).isEqualTo(email);
    }

    // ──────────────────────────────────────────────────
    // isTokenValid
    // ──────────────────────────────────────────────────

    @Test
    @DisplayName("isTokenValid → retourne true pour un token valide")
    void isTokenValid_shouldReturnTrueForValidToken() {
        String email = "agent@eqdom.ma";
        String token = jwtUtil.generateToken(email, List.of("AGENT"));

        assertTrue(jwtUtil.isTokenValid(token, email));
    }

    @Test
    @DisplayName("isTokenValid → retourne false si l'email ne correspond pas")
    void isTokenValid_shouldReturnFalseIfEmailMismatch() {
        String token = jwtUtil.generateToken("agent@eqdom.ma", List.of("AGENT"));

        assertFalse(jwtUtil.isTokenValid(token, "autre@eqdom.ma"));
    }

    @Test
    @DisplayName("isTokenValid → lève ExpiredJwtException pour un token expiré")
    void isTokenValid_shouldThrowForExpiredToken() {
        // Token avec expiration passée (−1 ms)
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", -1L);
        String token = jwtUtil.generateToken("agent@eqdom.ma", List.of("AGENT"));

        // Restaurer expiration normale
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", EXPIRATION_MS);

        // JwtUtil.isTokenValid() propage l'exception plutôt que de retourner false
        assertThrows(Exception.class, () -> jwtUtil.isTokenValid(token, "agent@eqdom.ma"));
    }

    @Test
    @DisplayName("isTokenValid → lève une exception pour un token complètement invalide")
    void isTokenValid_shouldThrowForGarbageToken() {
        assertThrows(Exception.class, () -> jwtUtil.isTokenValid("token.invalide.garbage", "email@test.ma"));
    }

    // ──────────────────────────────────────────────────
    // Cas limites
    // ──────────────────────────────────────────────────

    @Test
    @DisplayName("generateToken → accepte une liste de rôles vide")
    void generateToken_withEmptyRoles_shouldNotThrow() {
        assertDoesNotThrow(() -> jwtUtil.generateToken("user@eqdom.ma", List.of()));
    }

    @Test
    @DisplayName("generateToken → accepte plusieurs rôles")
    void generateToken_withMultipleRoles_shouldSucceed() {
        String token = jwtUtil.generateToken("superadmin@eqdom.ma", List.of("ADMIN", "AGENT"));
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("superadmin@eqdom.ma");
    }
}
