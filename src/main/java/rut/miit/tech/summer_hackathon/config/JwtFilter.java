package rut.miit.tech.summer_hackathon.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import rut.miit.tech.summer_hackathon.domain.dto.ErrorDTO;
import rut.miit.tech.summer_hackathon.service.JwtService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final List<String> openEndpoints;
    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public JwtFilter(JwtService jwtService, List<String> openEndpoints) {
        this.jwtService = jwtService;
        this.openEndpoints = openEndpoints;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (isOpenEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new SecurityException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            DecodedJWT jwt = jwtService.decodeAccessToken(token);

            var authentication = new UsernamePasswordAuthenticationToken(
                    jwt.getSubject(),
                    null,
                    jwt.getClaim("roles").asList(String.class)
                            .stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWT processing error: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(objectMapper.writeValueAsString(
                    ErrorDTO.builder()
                            .message(e.getMessage())
                            .status("401")
                            .date(LocalDateTime.now().toString())
                            .build()));
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
        }
    }

    private boolean isOpenEndpoint(String endpoint) {
        return openEndpoints.stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, endpoint));
    }
}