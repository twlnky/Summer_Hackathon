package rut.miit.tech.summer_hackathon.service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;
import rut.miit.tech.summer_hackathon.service.moderator.ModeratorService;

@Component
@RequiredArgsConstructor
public class SecurityUtils {
    private final ModeratorService moderatorService;

    public static boolean isAdmin() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .map(Object::toString)
                .toList()
                .contains("ADMIN");
    }

    public static boolean isModerator() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .map(Object::toString)
                .toList()
                .contains("MODERATOR");
    }





    public static void checkIsAdmin() throws AccessDeniedException {
        if (!isAdmin()) {
            throw new AccessDeniedException("ADMIN role is required for this action");
        }
    }

    public Moderator getAuthenticatedModerator() {
        if (!isModerator()) {
            throw new IllegalStateException("Current authenticated user is not MODERATOR");
        }
        String login = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return moderatorService.getByLogin(login);
    }

}
