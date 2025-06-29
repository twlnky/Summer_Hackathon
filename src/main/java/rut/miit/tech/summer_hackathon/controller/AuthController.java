package rut.miit.tech.summer_hackathon.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rut.miit.tech.summer_hackathon.domain.dto.AuthDTO;
import rut.miit.tech.summer_hackathon.domain.dto.JWTResponse;
import rut.miit.tech.summer_hackathon.domain.dto.RegisterDTO;
import rut.miit.tech.summer_hackathon.domain.dto.UserDTO;
import rut.miit.tech.summer_hackathon.domain.model.RevokedToken;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.domain.util.DtoConverter;
import rut.miit.tech.summer_hackathon.repository.RevokedTokenRepository;
import rut.miit.tech.summer_hackathon.service.JwtService;
import rut.miit.tech.summer_hackathon.service.registration.RegistrationService;
import rut.miit.tech.summer_hackathon.service.user.UserService;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final RegistrationService registrationService;
    private final RevokedTokenRepository revokedTokenRepository;
    private final DtoConverter dtoConverter;

    @PostMapping("/login")
    public ResponseEntity<JWTResponse> login(@RequestBody AuthDTO dto) {

        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password())
        );
        String username = authenticate.getName();
        User user = userService.getByUsername(username);
        return getJwtResponseEntity(user);
    }


    @NotNull
    private ResponseEntity<JWTResponse> getJwtResponseEntity(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        ResponseCookie cookie = ResponseCookie.from("refresh-token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new JWTResponse(accessToken));
    }

    @GetMapping("/refresh")
    public ResponseEntity<JWTResponse> refresh(HttpServletRequest request) {
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh-token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        if (refreshToken == null) {
            throw new RuntimeException("refresh token отсутствует в cookie");
        }
        DecodedJWT decodedRefreshToken = jwtService.decodeRefreshToken(refreshToken);
        Long userId = Long.valueOf(decodedRefreshToken.getSubject());
        User user = userService.getById(userId);
        if (user.isBanned()) {
            throw new RuntimeException("польщователь заблокирован");
        }
        String newAccessToken = jwtService.createAccess(decodedRefreshToken);
        return ResponseEntity.ok(new JWTResponse(newAccessToken));
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Optional<Cookie> cookie = Arrays.stream(request.getCookies())
                .toList()
                .stream().filter(c -> "refresh-token".equals(c.getName())).findFirst();
        if (cookie.isPresent()) {
            Cookie refreshCookie = cookie.get();
            RevokedToken revokedToken = new RevokedToken();
            revokedToken.setToken(refreshCookie.getValue());
            revokedTokenRepository.save(revokedToken);
        }
        Cookie revokeTokenCookie = new Cookie("refresh-token", "");
        response.addCookie(revokeTokenCookie);



        //1. Extract JWT From Cookie and Revoke
        //2. Set-Cookie: refresh-token=""
    }


    /* Confirm: GET - /confirm/reg?token=123543&userId=f34jcf3kjckrj
    * 1. Find Confirm Token in DB By token value and userId
    * 2. If exists -> check token expiration -> 30 m
    * 3. If ok -> update user by id set enable = true
    * 4. If not ok -> throw AccessDeniedException
    * */

    /* Reg:
    * 1. Validation
    * 2. Save User In DB with isEnable = false
    * 3. Generate Code And Send On Email And Save In DB - ASYNC
    * 4. Send UserDTO to client
    * */
    @PostMapping("/registration")
    public UserDTO registration(@Validated @RequestBody RegisterDTO dto,
                                BindingResult bindingResult) {

        if(bindingResult.hasErrors()) {
            throw new IllegalArgumentException();
        }
        return dtoConverter.toDto(registrationService.register(dto), UserDTO.class);

    }



}
