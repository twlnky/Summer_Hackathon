package rut.miit.tech.summer_hackathon.service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import rut.miit.tech.summer_hackathon.service.moderator.ModeratorService;
import rut.miit.tech.summer_hackathon.service.user.UserService;

import java.util.Objects;


@RequiredArgsConstructor
@Service("security")
public class SecurityService {
    private final ModeratorService moderatorService;
    private final UserService userService;

    public boolean checkAccessToModer(Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return moderatorService.getById(id).getLogin().equals(userDetails.getUsername());
    }

    public boolean checkAccessToUser(Long id) {
        return false;
    }

}
