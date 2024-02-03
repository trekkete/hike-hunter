package it.trekkete.hikehunter.data.service;

import it.trekkete.hikehunter.data.entity.PasswordRecoveryToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PasswordRecoveryTokenRepository extends JpaRepository<PasswordRecoveryToken, UUID> {

    PasswordRecoveryToken findByToken(UUID token);

    List<PasswordRecoveryToken> findByCreationTsLessThan(Long creationTs);
}
