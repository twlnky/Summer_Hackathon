package rut.miit.tech.summer_hackathon.service.registration;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import rut.miit.tech.summer_hackathon.domain.dto.RegisterDTO;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.domain.model.email.VerificationEmail;
import rut.miit.tech.summer_hackathon.domain.util.DtoConverter;
import rut.miit.tech.summer_hackathon.repository.UserRepository;
import rut.miit.tech.summer_hackathon.service.email.EmailService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final DtoConverter dtoConverter;

    public User register(@NotNull RegisterDTO request) {
        User user = dtoConverter.toModel(request, User.class);
        String token = UUID.randomUUID().toString();
        userRepository.save(user);
        emailService.sendEmail(VerificationEmail.builder()
                .code(token)
                .to(user.getEmail())
                .build());
        return user;
    }
}
