package it.trekkete.hikehunter.email;

import org.springframework.mail.SimpleMailMessage;

import javax.mail.MessagingException;

public interface EmailService {

    void sendMessage(String to, String subject, SimpleMailMessage template, String... args) throws MessagingException;
}
