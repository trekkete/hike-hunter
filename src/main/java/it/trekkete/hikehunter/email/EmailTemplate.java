package it.trekkete.hikehunter.email;

import org.springframework.mail.SimpleMailMessage;

public class EmailTemplate {

    public static final SimpleMailMessage MAIL_VALIDATION = new SimpleMailMessage(){{
        setText("Il tuo codice per la verifica della mail è:\n%s\n");
    }};

    public static final SimpleMailMessage PASSWORD_RECOVERY = new SimpleMailMessage(){{
        setText("Vai al seguente link per reimpostare la tua password:\nhttps://hikehunter.it/support?t=%s\n");
    }};
}
