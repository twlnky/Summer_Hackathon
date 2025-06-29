package rut.miit.tech.summer_hackathon.domain.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import rut.miit.tech.summer_hackathon.domain.dto.RegisterDTO;
import rut.miit.tech.summer_hackathon.repository.UserRepository;


@Component
@RequiredArgsConstructor
public class UserRegistrationValidator implements Validator {
    private final UserRepository userRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(RegisterDTO.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if(target instanceof RegisterDTO dto) {
            if(userRepository.existsUserByEmail(dto.email())){
                errors.rejectValue("email", null, "This email is already in use");
            }
        }
    }
}
