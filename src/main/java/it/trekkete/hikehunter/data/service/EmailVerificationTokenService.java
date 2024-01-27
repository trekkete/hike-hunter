package it.trekkete.hikehunter.data.service;

import it.trekkete.hikehunter.data.entity.EmailVerificationToken;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class EmailVerificationTokenService {

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    public EmailVerificationTokenService(EmailVerificationTokenRepository emailVerificationTokenRepository) {
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
    }

    public void save(EmailVerificationToken emailVerificationToken) {
        this.emailVerificationTokenRepository.save(emailVerificationToken);
    }

    public EmailVerificationToken findByEmail(String email) {
        return this.emailVerificationTokenRepository.findByEmail(email);
    }

    public void delete(EmailVerificationToken emailVerificationToken) {
        this.emailVerificationTokenRepository.delete(emailVerificationToken);
    }

    public void deleteOldTokens() {

        long now = ZonedDateTime.now().toEpochSecond();

        // delete tokens older than 10 minutes
        Long olderTs = now - (10 * 60);

        List<EmailVerificationToken> oldTokens = emailVerificationTokenRepository.findByCreationTsLessThan(olderTs);

        oldTokens.forEach(emailVerificationTokenRepository::delete);

    }
}
