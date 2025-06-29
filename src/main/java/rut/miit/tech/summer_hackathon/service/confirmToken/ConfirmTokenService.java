package rut.miit.tech.summer_hackathon.service.confirmToken;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rut.miit.tech.summer_hackathon.domain.model.ConfirmToken;
import rut.miit.tech.summer_hackathon.repository.ConfirmTokenRepository;

@Service
@RequiredArgsConstructor
public class ConfirmTokenService {

    private final ConfirmTokenRepository confirmTokenRepository;

    public void confirmToken(ConfirmToken confirmToken) {
        confirmTokenRepository.save(confirmToken);
    }
}
