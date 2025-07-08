package rut.miit.tech.summer_hackathon.service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import rut.miit.tech.summer_hackathon.domain.exception.ResourceNotFoundException;
import rut.miit.tech.summer_hackathon.domain.model.ModeratorDetailsImpl;
import rut.miit.tech.summer_hackathon.service.moderator.ModeratorService;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final ModeratorService moderatorService;
    private final UserDetails admin;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if(username.equals(admin.getUsername())){
            return admin;
        }
        try {
            return new ModeratorDetailsImpl(moderatorService.getByLogin(username));
        }catch (ResourceNotFoundException e){
            throw new UsernameNotFoundException(username + " not found");
        }
    }
}
