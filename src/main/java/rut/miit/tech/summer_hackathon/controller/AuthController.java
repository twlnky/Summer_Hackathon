package rut.miit.tech.summer_hackathon.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.domain.util.DtoConverter;
import rut.miit.tech.summer_hackathon.service.JwtService;
import rut.miit.tech.summer_hackathon.service.security.RefreshTokenService;
import rut.miit.tech.summer_hackathon.domain.model.RefreshToken;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.repository.UserRepository;
import rut.miit.tech.summer_hackathon.service.registration.RegistrationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RegistrationService registrationService;
    private final DtoConverter dtoConverter;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> payload) {
        String refreshTokenValue = payload.get("refreshToken");
        if (refreshTokenValue == null) {
            return ResponseEntity.badRequest().body("Missing refresh token");
        }
        var refreshTokenOpt = refreshTokenService.findByToken(refreshTokenValue);
        if (refreshTokenOpt.isEmpty() || refreshTokenService.isExpired(refreshTokenOpt.get())) {
            return ResponseEntity.status(401).body("Invalid or expired refresh token");
        }
        RefreshToken refreshToken = refreshTokenOpt.get();
        User user = userRepository.findById(refreshToken.getUserId())
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }
    String role = (user.getModerator() != null) ? "MODERATOR" : "USER";
    String accessToken = jwtService.generateAccessToken(
        org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password("")
            .authorities(role)
            .build()
    );
    return ResponseEntity.ok(Map.of("accessToken", accessToken));
    }


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
            // Можно добавить другие поля, если нужно
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("User info not available");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthDTO dto) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password());

        Authentication authentication = authenticationManager.authenticate(authToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(userDetails);

        // Получаем пользователя из базы по login
    User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        String refreshTokenValue = null;
        if (user != null && user.getId() != null) {
            String deviceInfo = "";
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId(), deviceInfo);
            refreshTokenValue = refreshToken.getToken();
        }

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        if (refreshTokenValue != null) {
            tokens.put("refreshToken", refreshTokenValue);
        }
        return ResponseEntity.ok(tokens);
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
}