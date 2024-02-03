package it.trekkete.hikehunter.data.service;

import it.trekkete.hikehunter.data.entity.EmailVerificationToken;
import it.trekkete.hikehunter.data.entity.PasswordRecoveryToken;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PasswordRecoveryTokenService {

    private final PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;

    public PasswordRecoveryTokenService(PasswordRecoveryTokenRepository passwordRecoveryTokenRepository) {
        this.passwordRecoveryTokenRepository = passwordRecoveryTokenRepository;
    }

    public void save(PasswordRecoveryToken passwordRecoveryToken) {
        this.passwordRecoveryTokenRepository.save(passwordRecoveryToken);
    }

    public PasswordRecoveryToken findByToken(UUID token) {
        return this.passwordRecoveryTokenRepository.findByToken(token);
    }

    public void delete(PasswordRecoveryToken passwordRecoveryToken) {
        this.passwordRecoveryTokenRepository.delete(passwordRecoveryToken);
    }

    public void deleteOldTokens() {

        long now = ZonedDateTime.now().toEpochSecond();

        // delete tokens older than 30 minutes
        Long olderTs = now - (30 * 60);

        List<PasswordRecoveryToken> oldTokens = passwordRecoveryTokenRepository.findByCreationTsLessThan(olderTs);

        oldTokens.forEach(passwordRecoveryTokenRepository::delete);

    }
}
