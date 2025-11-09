package rut.miit.tech.summer_hackathon.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rut.miit.tech.summer_hackathon.domain.dto.AuthDTO;
import rut.miit.tech.summer_hackathon.domain.dto.JWTResponse;
import rut.miit.tech.summer_hackathon.domain.dto.RegisterDTO;
import rut.miit.tech.summer_hackathon.domain.dto.UserDTO;
import rut.miit.tech.summer_hackathon.domain.exception.UnauthorizedException;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;
import rut.miit.tech.summer_hackathon.domain.model.RevokedToken;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.domain.model.RefreshToken;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.domain.util.DtoConverter;
import rut.miit.tech.summer_hackathon.repository.ModeratorRepository;
import rut.miit.tech.summer_hackathon.repository.RefreshTokenRepository;
import rut.miit.tech.summer_hackathon.repository.UserRepository;
import rut.miit.tech.summer_hackathon.service.JwtService;
import rut.miit.tech.summer_hackathon.service.registration.RegistrationService;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RegistrationService registrationService;
    private final DtoConverter dtoConverter;
    private final rut.miit.tech.summer_hackathon.repository.RevokedTokenRepository revokedTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final ModeratorRepository moderatorRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUse(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = authHeader != null ? authHeader.substring(7) : null;
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Missing authorization header"));
        }
        try {
            DecodedJWT decodedJWT = jwtService.decodeAccessToken(token);
            String username = decodedJWT.getSubject();
            User user = userRepository.findByEmail(username).orElse(null);
            if (user == null) {
                Moderator mod = moderatorRepository.findByLogin(username).orElse(null);
                if (mod != null) {
                    user = userRepository.findById(mod.getId()).orElse(null);
                }
            }
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("message", "User not found"));
            }

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", decodedJWT.getSubject());
            userInfo.put("authorities", decodedJWT.getClaim("roles").asList(String.class)
                    .stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList());
            userInfo.put("firstName", user.getFirstName());
            userInfo.put("lastName", user.getLastName());
            userInfo.put("middleName", user.getMiddleName());
            userInfo.put("email", user.getEmail());
            userInfo.put("moderatorId", user.getModerator() != null ? user.getModerator().getId() : null);

            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid token"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<JWTResponse> login(@RequestBody AuthDTO dto, HttpServletResponse response) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(dto.username(), dto.password());

        Authentication authentication = authenticationManager.authenticate(authToken);

        String refreshToken = jwtService.generateRefreshToken((UserDetails) authentication.getPrincipal());

        String accessToken = jwtService.generateAccessToken(
                (UserDetails) authentication.getPrincipal(), refreshToken);

        try {
            DecodedJWT decoded = JWT.decode(refreshToken);
            String jti = decoded.getId();

            Instant issued = decoded.getIssuedAt() != null
                    ? decoded.getIssuedAt().toInstant()
                    : Instant.now();

            Instant expires = decoded.getExpiresAt() != null
                    ? decoded.getExpiresAt().toInstant()
                    //Проверка не истек ли срок годности
                    : Instant.now().plus(Duration.ofDays(30));

            User user = userRepository.findByEmail(dto.username()).orElse(null);

            if (user == null) {
                Moderator moderator = moderatorRepository.findByLogin(dto.username())
                        .orElseThrow(() -> new RuntimeException("Moderator not found"));

                user = userRepository.findById(moderator.getId()).orElse(null);
                if (user == null) {
                    user = new User();
                    user.setFirstName(moderator.getFirstName());
                    user.setLastName(moderator.getLastName());
                    user.setMiddleName(moderator.getMiddleName());
                    user.setEmail(moderator.getLogin());
                    user.setModerator(moderator);
                    user = userRepository.save(user);
                }
            }

            RefreshToken rt = RefreshToken.builder()
                    .jti(jti)
                    .token(refreshToken)
                    .user(user)
                    .issuedAt(issued)
                    .expiresAt(expires)
                    .build();

            refreshTokenRepository.save(rt);

        } catch (Exception ex) {
            log.error("Error saving refresh token", ex);
        }

        boolean secureForLocal = false;
        ResponseCookie cookie = ResponseCookie.from("refresh-token", refreshToken)
                .httpOnly(true)
                .secure(secureForLocal)
                .path("/")
                .maxAge(Duration.ofDays(30))
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new JWTResponse(accessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {

            Cookie[] cookies = request.getCookies();
            String refreshTokenValue = null;
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("refresh-token".equals(c.getName())) {
                        refreshTokenValue = c.getValue();
                        break;
                    }
                }
            }

            if (refreshTokenValue != null && !refreshTokenValue.isBlank()) {
                try {
                    Optional<RefreshToken> optionalRt = refreshTokenRepository.findByToken(refreshTokenValue);
                    if (optionalRt.isPresent()) {
                        RefreshToken rt = optionalRt.get();
                        //Не ревокаю, а прост удаляю
                        refreshTokenRepository.delete(rt);
                    } else {
                        try {
                            DecodedJWT decoded = JWT.decode(refreshTokenValue);
                            String jti = decoded.getId();
                            if (jti != null) {
                                Optional<RefreshToken> tokenByJti = refreshTokenRepository.findByJti(jti);
                                if (tokenByJti.isPresent()) {
                                    RefreshToken rt = tokenByJti.get();
                                    //Тут тоже самое
                                    refreshTokenRepository.delete(rt);
                                }
                            }
                        } catch (Exception e) {}
                    }
                } catch (Exception e) {}
            }

            try {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String accessToken = authHeader.substring(7);
                    try {
                        DecodedJWT decoded = JWT.decode(accessToken);
                        String jti = decoded.getId();
                        if (jti != null) {
                            rut.miit.tech.summer_hackathon.domain.model.RevokedToken revoked = new rut.miit.tech.summer_hackathon.domain.model.RevokedToken();
                            revoked.setToken(jti);
                            revoked.setTokenType("access");
                            revoked.setRevokedAt(Instant.now());
                            revokedTokenRepository.save(revoked);
                        }
                    } catch (Exception ex) {}
                } else {}
            } catch (Exception e) {}
            //Я не особо понял про безопасность, сказано что это плохая практика, но без этого не работает
            ResponseCookie deleteCookie = ResponseCookie.from("refresh-token", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(0)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
            return ResponseEntity.ok().body(Map.of("message", "Logged out successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Logout failed"));
        }
    }

    @PostMapping("/registration")
    public UserDTO registration(
            @Validated @RequestBody RegisterDTO dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException("Invalid registration data");
        }

        User registeredUser = registrationService.register(dto);
        return dtoConverter.toDto(registeredUser, UserDTO.class);
    }

    private User convertModeratorToUser(Moderator moderator) {
        User user = new User();
        user.setId(moderator.getId());
        user.setFirstName(moderator.getFirstName());
        user.setLastName(moderator.getLastName());
        user.setMiddleName(moderator.getMiddleName());
        user.setEmail(moderator.getLogin());
        user.setModerator(moderator);
        return user;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenValue = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("refresh-token".equals(c.getName())) {
                    refreshTokenValue = c.getValue();
                    break;
                }
            }
        }

        if (refreshTokenValue == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Refresh token not provided"));
        }

        try {
            Optional<RefreshToken> optionalRt = refreshTokenRepository.findByToken(refreshTokenValue);
            //Я его теперь удаляю, поэтому проверка на пустоту
            if (optionalRt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("message", "Refresh token revoked or invalid"));
            }

            RefreshToken rt = optionalRt.get();
            if (rt.getExpiresAt().isBefore(Instant.now())) {
                refreshTokenRepository.delete(rt);
                return ResponseEntity.status(401).body(Map.of("message", "Refresh token expired"));
            }

            DecodedJWT decoded = JWT.decode(refreshTokenValue);
            String newAccess = jwtService.createAccess(decoded);

            return ResponseEntity.ok(Map.of("accessToken", newAccess));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }
}