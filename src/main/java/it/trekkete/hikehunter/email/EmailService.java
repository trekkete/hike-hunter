package it.trekkete.hikehunter.email;

import javax.mail.MessagingException;

public interface EmailService {

    void sendMessage(String to, String subject, String... args) throws MessagingException;
}
