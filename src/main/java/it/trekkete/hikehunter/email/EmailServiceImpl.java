package it.trekkete.hikehunter.email;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service("EmailService")
@ComponentScan(basePackages = { "it.trekkete.hikehunter.email", "org.springframework.mail" })
public class EmailServiceImpl implements EmailService {

    private final Logger log = LogManager.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private SimpleMailMessage template;

    @Autowired
    private String senderAddress;

    @Override
    public void sendMessage(String to, String subject, String... args) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(String.format(template.getText(), args));

        log.trace("Sending email to {} ", to);

        emailSender.send(message);
    }
}
