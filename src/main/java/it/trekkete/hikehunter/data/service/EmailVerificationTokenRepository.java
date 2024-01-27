package it.trekkete.hikehunter.data.service;

import it.trekkete.hikehunter.data.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, String> {

    EmailVerificationToken findByEmail(String email);

    List<EmailVerificationToken> findByCreationTsLessThan(Long creationTs);
}
