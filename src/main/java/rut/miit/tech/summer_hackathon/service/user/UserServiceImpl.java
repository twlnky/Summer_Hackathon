package rut.miit.tech.summer_hackathon.service.user;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import rut.miit.tech.summer_hackathon.domain.dto.RegisterDTO;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.domain.model.UserDetailsImpl;
import rut.miit.tech.summer_hackathon.repository.UserRepository;

@Service
@Primary
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;



    @Override
    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException(id.toString()));
    }

    @Override
    public User getByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }

    @Override
    public User save(@NotNull RegisterDTO dto) {
        if (userRepository.findByUsername(dto.username()).isPresent()) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        if (!dto.password().equals(dto.confirmPassword())) {
            throw new RuntimeException("Пароли не совпадают");
        }
        User user = new User();
        user.setUsername(dto.username());
        user.setPassword((dto.password()));
        return userRepository.save(user);
    }




    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new UserDetailsImpl(this.getByUsername(username));
    }
}
