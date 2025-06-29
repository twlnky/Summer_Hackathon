package rut.miit.tech.summer_hackathon.domain.model.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerificationEmail implements Email {
    private String to;
    private String code;

    @Override
    public String to() {
        return to;
    }

    @Override
    public String subject() {
        return "Verification Email";
    }

    @Override
    public String body() {
        return "<html><body><h1>Verification Email</h1><p>%s</p></body></html>".formatted(code);
    }
}
