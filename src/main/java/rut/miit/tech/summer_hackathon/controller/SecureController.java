package rut.miit.tech.summer_hackathon.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.service.user.UserServiceImpl;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/secure")
public class SecureController {
    private final UserServiceImpl userServiceImpl;

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String adminAuth()  {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userServiceImpl.getByUsername(username);
        if (user.isBanned()){
            log.info("Permission denied");
            throw new AccessDeniedException("Permission denied");
        }

        return "Hello ADMIN!"+username;
    }

    @GetMapping
    public String userAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User username = userServiceImpl.getById(Long.parseLong(authentication.getPrincipal().toString()));
        if (username.isBanned()){
            log.info("Permission denied");
            throw new AccessDeniedException("Permission denied");
        }
        return "Hello USER!" + username;
    }
}
