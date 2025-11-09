package rut.miit.tech.summer_hackathon.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
import java.util.HashMap;
import java.util.Map;

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
            throw new UnauthorizedException("Missing authorization header");
        }
        try {
            DecodedJWT decodedJWT = jwtService.decodeAccessToken(token);
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", decodedJWT.getSubject());
            userInfo.put("authorities", decodedJWT.getClaim("roles").asList(String.class)
                    .stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList());
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("User info not available");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<JWTResponse> login(@RequestBody AuthDTO dto) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(dto.username(), dto.password());

        Authentication authentication = authenticationManager.authenticate(authToken);

        String accessToken = jwtService.generateAccessToken(
                (UserDetails) authentication.getPrincipal());

        String refreshToken = jwtService.generateRefreshToken((UserDetails) authentication.getPrincipal());

        try {
            DecodedJWT decoded = jwtService.decodeRefreshToken(refreshToken);
            String jti = decoded.getId();

            Instant issued = decoded.getIssuedAt() != null
                    ? decoded.getIssuedAt().toInstant()
                    : Instant.now();

            Instant expires = decoded.getExpiresAt() != null
                    ? decoded.getExpiresAt().toInstant()
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
                    .revoked(false)
                    .build();

            refreshTokenRepository.save(rt);
            System.out.println(" Refresh token saved");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("!!!!!!!НИЧЕГО НЕ СОХРАНИЛОСЬ");
        }

        Cookie cookie = new Cookie("refresh-token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) Duration.ofDays(30).getSeconds());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new JWTResponse(accessToken));
    }



    @GetMapping("/refresh")
    public ResponseEntity<JWTResponse> refresh(HttpServletRequest request) {
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("refresh-token".equals(c.getName())) {
                    refreshToken = c.getValue();
                    break;
                }
            }
        }
        if (refreshToken == null) {
            throw new RuntimeException("refresh token not found in cookie");
        }
        com.auth0.jwt.interfaces.DecodedJWT decodedRefreshToken = jwtService.decodeRefreshToken(refreshToken);
        String newAccessToken = jwtService.createAccess(decodedRefreshToken);
        return ResponseEntity.ok(new JWTResponse(newAccessToken));
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("refresh-token".equals(c.getName())) {
                    String value = c.getValue();
                    try {
                        com.auth0.jwt.interfaces.DecodedJWT decoded = jwtService.decodeRefreshToken(value);
                        String jti = decoded.getId();
                        refreshTokenRepository.findByJti(jti).ifPresent(rt -> {
                            rt.setRevoked(true);
                            refreshTokenRepository.save(rt);
                        });
                    } catch (Exception ex) {
                        rut.miit.tech.summer_hackathon.domain.model.RevokedToken revokedToken = new rut.miit.tech.summer_hackathon.domain.model.RevokedToken();
                        revokedToken.setToken(value);
                        revokedTokenRepository.save(revokedToken);
                    }
                    break;
                }
            }
        }
        Cookie revokeCookie = new Cookie("refresh-token", "");
        revokeCookie.setPath("/");
        revokeCookie.setHttpOnly(true);
        revokeCookie.setSecure(true);
        revokeCookie.setMaxAge(0);
        response.addCookie(revokeCookie);
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

}