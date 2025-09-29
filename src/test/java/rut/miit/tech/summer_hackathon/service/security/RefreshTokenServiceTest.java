package rut.miit.tech.summer_hackathon.service.security;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import rut.miit.tech.summer_hackathon.domain.model.RefreshToken;
import rut.miit.tech.summer_hackathon.repository.RefreshTokenRepository;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenServiceTest {
    @Test
    void testIsExpired() {
        RefreshTokenRepository repo = Mockito.mock(RefreshTokenRepository.class);
        RefreshTokenService service = new RefreshTokenService(repo);
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().minusSeconds(10));
        assertTrue(service.isExpired(token));
        token.setExpiryDate(Instant.now().plusSeconds(10));
        assertFalse(service.isExpired(token));
    }

    @Test
    void testFindByToken() {
        RefreshTokenRepository repo = Mockito.mock(RefreshTokenRepository.class);
        RefreshTokenService service = new RefreshTokenService(repo);
        RefreshToken token = new RefreshToken();
        token.setToken("abc");
        Mockito.when(repo.findByToken("abc")).thenReturn(Optional.of(token));
        Optional<RefreshToken> found = service.findByToken("abc");
        assertTrue(found.isPresent());
        assertEquals("abc", found.get().getToken());
    }
}
