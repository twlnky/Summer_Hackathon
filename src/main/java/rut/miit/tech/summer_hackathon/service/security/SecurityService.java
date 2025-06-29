package rut.miit.tech.summer_hackathon.service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Service("security")
public class SecurityService {

    public SecurityService() {
    }

    public boolean checkAccessToUser(String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities()
                .stream()
                .map(Objects::toString)
                .anyMatch(s -> s.equals("ADMIN"))) {
            return true;
        }
        String userId = (String) authentication.getPrincipal();
        return id.equals(userId);
    }

}
