package it.trekkete.hikehunter.ui.views.login;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.google.gson.Gson;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.cookieconsent.CookieConsent;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.trekkete.hikehunter.data.Role;
import it.trekkete.hikehunter.data.entity.EmailVerificationToken;
import it.trekkete.hikehunter.data.entity.User;
import it.trekkete.hikehunter.data.entity.UserExtendedData;
import it.trekkete.hikehunter.data.service.EmailVerificationTokenRepository;
import it.trekkete.hikehunter.data.service.EmailVerificationTokenService;
import it.trekkete.hikehunter.data.service.UserRepository;
import it.trekkete.hikehunter.email.EmailService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.mail.MessagingException;
import java.time.ZonedDateTime;
import java.util.Set;

@PageTitle(value = "Registrati")
@Route(value = "register")
@AnonymousAllowed
public class RegistrationView extends VerticalLayout {

    private final Logger log = LogManager.getLogger(RegistrationView.class);

    private final UserRepository userRepository;
    private final EmailVerificationTokenService emailVerificationTokenService;

    private final VerticalLayout container;

    @Autowired
    EmailService emailService;

    public RegistrationView(@Autowired UserRepository userRepository,
                            @Autowired EmailVerificationTokenRepository emailVerificationTokenRepository) {
        this.userRepository = userRepository;
        this.emailVerificationTokenService = new EmailVerificationTokenService(emailVerificationTokenRepository);

        this.container = new VerticalLayout();
        this.container.setSpacing(false);
        this.container.addClassNames(LumoUtility.AlignItems.CENTER);
    }

    private void constructUI() {

        setSpacing(false);
        addClassNames(LumoUtility.AlignItems.CENTER);

        Image logo = new Image("images/default-logo.png", "hike-hunter-logo");
        logo.setWidth("100%");
        logo.setMaxWidth("400px");
        logo.getStyle()
                .set("object-fit", "contain");

        add(logo);

        setStageOne(new User());

        add(container);
    }

    private void setStageOne(User user) {

        container.removeAll();

        FormLayout stageOneLayout = new FormLayout();
        stageOneLayout.setWidthFull();
        stageOneLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0px", 1),
                new FormLayout.ResponsiveStep("600px", 2));

        UserExtendedData userExtendedData;
        if (user.getExtendedData() != null)
            userExtendedData = new Gson().fromJson(user.getExtendedData(), UserExtendedData.class);
        else
            userExtendedData = new UserExtendedData();

        TextField firstName = new TextField("Nome");
        if (userExtendedData.getName() != null)
            firstName.setValue(userExtendedData.getName());

        TextField lastName = new TextField("Cognome");
        if (userExtendedData.getSurname() != null)
            lastName.setValue(userExtendedData.getSurname());

        TextField username = new TextField("Username");
        if (user.getUsername() != null)
            username.setValue(user.getUsername());

        stageOneLayout.add(firstName, lastName, username);

        Button next = new Button("Avanti");
        next.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        next.getStyle().set("margin-top", "1.5em");
        next.addClickListener(click -> {

            if (username.getValue() == null || username.isEmpty()) {
                username.setInvalid(true);
                username.setErrorMessage("Lo username non può essere vuoto");

                return;
            }

            if (userRepository.findByUsername(username.getValue()) != null) {
                username.setInvalid(true);
                username.setErrorMessage("Esiste già un account associato a questo username");

                return;
            }

            if(firstName.getValue() != null && !firstName.isEmpty()) {
                userExtendedData.setName(firstName.getValue());
            }

            if(lastName.getValue() != null && !lastName.isEmpty()) {
                userExtendedData.setSurname(lastName.getValue());
            }

            user.setUsername(username.getValue());
            user.setExtendedData(new Gson().toJson(userExtendedData));

            setStageTwo(user);
        });

        stageOneLayout.add(next);

        container.add(stageOneLayout);
    }

    private void setStageTwo(User user) {

        container.removeAll();

        FormLayout stageTwoLayout = new FormLayout();
        stageTwoLayout.setWidthFull();
        stageTwoLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0px", 1),
                new FormLayout.ResponsiveStep("600px", 2));

        UserExtendedData userExtendedData;
        if (user.getExtendedData() != null)
            userExtendedData = new Gson().fromJson(user.getExtendedData(), UserExtendedData.class);
        else
            userExtendedData = new UserExtendedData();

        TextField email = new TextField("Email");
        if (userExtendedData.getEmail() != null)
            email.setValue(userExtendedData.getEmail());

        TextField phoneNumber = new TextField("Numero di telefono");
        if (userExtendedData.getPhoneNumber() != null)
            phoneNumber.setValue(userExtendedData.getPhoneNumber());

        stageTwoLayout.add(email, phoneNumber);

        Button back = new Button("Indietro");
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        back.getStyle().set("margin-top", "1.5em");
        back.addClickListener(click -> setStageOne(user));

        Button next = new Button("Avanti");
        next.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        next.addClickListener(click -> {

            if(email.getValue() != null && !email.isEmpty()) {
                userExtendedData.setEmail(email.getValue());
            }

            if(phoneNumber.getValue() != null && !phoneNumber.isEmpty()) {
                userExtendedData.setPhoneNumber(phoneNumber.getValue());
            }

            user.setExtendedData(new Gson().toJson(userExtendedData));

            EmailVerificationToken emailVerificationToken = new EmailVerificationToken();
            emailVerificationToken.setEmail(userExtendedData.getEmail());
            emailVerificationToken.setToken(RandomStringUtils.random(6, false, true));
            emailVerificationToken.setCreationTs(ZonedDateTime.now().toEpochSecond());

            emailVerificationTokenService.save(emailVerificationToken);

            //TODO invia la mail con il token per la verifica
            try {
                emailService.sendMessage(userExtendedData.getEmail(), "Verifica la tua mail", emailVerificationToken.getToken());
            } catch (MessagingException e) {
                log.warn("Error sending mail verification token to {}", userExtendedData.getEmail());

                Notification notification = new Notification();
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

                Icon icon = FontAwesome.Solid.WARNING.create();
                Div info = new Div(new Text("C'è stato un errore nell'invio dell'e-mail di verifica. Riprova più tardi."));

                HorizontalLayout layout = new HorizontalLayout(icon, info);
                layout.setAlignItems(FlexComponent.Alignment.CENTER);

                notification.add(layout);
                notification.setPosition(Notification.Position.TOP_CENTER);
                notification.setDuration(0);
                layout.addClickListener(click1 -> notification.close());

                notification.open();

                return;
            }

            setStageThree(user);
        });

        stageTwoLayout.add(back, next);

        container.add(stageTwoLayout);
    }

    private void setStageThree(User user) {

        container.removeAll();

        FormLayout stageThreeLayout = new FormLayout();
        stageThreeLayout.setWidthFull();
        stageThreeLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0px", 1),
                new FormLayout.ResponsiveStep("600px", 2));

        UserExtendedData userExtendedData = new Gson().fromJson(user.getExtendedData(), UserExtendedData.class);

        TextField code = new TextField("Codice");
        code.setMinLength(6);
        code.setMaxLength(6);
        code.setHelperText("Abbiamo inviato un codice a " + userExtendedData.getEmail() + ". Inseriscilo per verificare la tua mail.");
        code.addThemeVariants(TextFieldVariant.LUMO_HELPER_ABOVE_FIELD, TextFieldVariant.LUMO_ALIGN_CENTER);

        stageThreeLayout.add(code);

        Button back = new Button("Indietro");
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        back.getStyle().set("margin-top", "1.5em");
        back.addClickListener(click -> setStageTwo(user));

        Button next = new Button("Avanti");
        next.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        next.addClickListener(click -> {

            emailVerificationTokenService.deleteOldTokens();

            EmailVerificationToken evt = emailVerificationTokenService.findByEmail(userExtendedData.getEmail());
            if (evt == null) {
                code.setInvalid(true);
                code.setErrorMessage("Non esiste un codice di validazione per la mail impostata. Riprovare più tardi.");

                return;
            }

            if (!evt.getToken().equals(code.getValue())) {
                code.setInvalid(true);
                code.setErrorMessage("Il codice non corrisponde a quello inviato.");

                return;
            }

            emailVerificationTokenService.delete(evt);

            setStageFour(user);
        });

        stageThreeLayout.add(back, next);

        container.add(stageThreeLayout);
    }

    private void setStageFour(User user) {

        container.removeAll();

        FormLayout stageFourLayout = new FormLayout();
        stageFourLayout.setWidthFull();
        stageFourLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0px", 1),
                new FormLayout.ResponsiveStep("600px", 2));

        PasswordField password = new PasswordField("Password");
        PasswordField confirm = new PasswordField("Ripeti password");

        stageFourLayout.add(password, confirm);

        Button back = new Button("Indietro");
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        back.getStyle().set("margin-top", "1.5em");
        back.addClickListener(click -> setStageTwo(user));

        Button next = new Button("Inizia l'avventura");
        next.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        next.addClickListener(click -> {

            if(password.getValue() == null || password.getValue().length() < 8) {
                password.setInvalid(true);
                password.setErrorMessage("La password deve essere di almeno 8 caratteri");

                return;
            }

            if(confirm.getValue() == null || !confirm.getValue().equals(password.getValue())) {
                confirm.setInvalid(true);
                confirm.setErrorMessage("Le password non corrispondono");

                return;
            }

            user.setRoles(Set.of(Role.USER));
            user.setHashedPassword(new BCryptPasswordEncoder().encode(password.getValue()));
            user.setCreationTs(ZonedDateTime.now().toEpochSecond());

            userRepository.save(user);

            UI.getCurrent().navigate(LoginView.class);
        });

        stageFourLayout.add(back, next);

        container.add(stageFourLayout);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        constructUI();
    }
}