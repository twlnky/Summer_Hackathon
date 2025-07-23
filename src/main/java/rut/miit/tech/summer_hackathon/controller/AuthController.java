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
import rut.miit.tech.summer_hackathon.service.registration.RegistrationService;

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
    public ResponseEntity<JWTResponse> login(@RequestBody AuthDTO dto) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password());

        Authentication authentication = authenticationManager.authenticate(authToken);

        String accessToken = jwtService.generateAccessToken(
                (UserDetails) authentication.getPrincipal());

        return ResponseEntity.ok(new JWTResponse(accessToken));
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