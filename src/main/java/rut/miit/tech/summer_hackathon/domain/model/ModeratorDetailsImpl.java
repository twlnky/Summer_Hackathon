package rut.miit.tech.summer_hackathon.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


@Getter
@AllArgsConstructor
public class ModeratorDetailsImpl implements UserDetails {
    private Moderator moderator;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("MODERATOR"));
    }


    @Override
    public String getPassword() {
        return moderator.getPassword();
    }


    @Override
    public String getUsername() {
        return moderator.getLogin();
    }
}