package it.trekkete.hikehunter.ui.views.login;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.trekkete.hikehunter.data.Role;
import it.trekkete.hikehunter.data.entity.PasswordRecoveryToken;
import it.trekkete.hikehunter.data.entity.User;
import it.trekkete.hikehunter.data.service.PasswordRecoveryTokenRepository;
import it.trekkete.hikehunter.data.service.PasswordRecoveryTokenService;
import it.trekkete.hikehunter.data.service.UserRepository;
import it.trekkete.hikehunter.email.EmailService;
import it.trekkete.hikehunter.email.EmailTemplate;
import it.trekkete.hikehunter.ui.views.general.HomeView;
import it.trekkete.hikehunter.ui.window.PasswordInfoWindow;
import it.trekkete.hikehunter.utils.AppEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.mail.MessagingException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@PageTitle("Recupera password")
@Route(value = "/support")
@AnonymousAllowed
public class PasswordRecoveryView extends VerticalLayout  implements HasUrlParameter<String> {

    private final Logger log = LogManager.getLogger(PasswordRecoveryView.class);

    private final UserRepository userRepository;

    private final PasswordRecoveryTokenService passwordRecoveryTokenService;

    @Autowired
    private EmailService emailService;

    public PasswordRecoveryView(@Autowired UserRepository userRepository,
                                @Autowired PasswordRecoveryTokenRepository passwordRecoveryTokenRepository) {
        this.userRepository = userRepository;
        this.passwordRecoveryTokenService = new PasswordRecoveryTokenService(passwordRecoveryTokenRepository);
    }

    public void constructUI(String token) {

        removeAll();

        setSpacing(false);
        setSizeFull();
        addClassNames(LumoUtility.AlignItems.CENTER);

        VerticalLayout container = new VerticalLayout();
        container.setSpacing(false);
        container.addClassNames(LumoUtility.AlignItems.CENTER);

        Image logo = new Image("images/default-logo.png", "hike-hunter-logo");
        logo.setWidth("100%");
        logo.setMaxWidth("400px");
        logo.getStyle()
                .set("object-fit", "contain");

        add(logo);

        if (token == null) {

            log.trace("No token in support view, showing recovery form");

            TextField username = new TextField("Username");
            username.setHelperText("Verrà inviata una mail all'indirizzo associato per recuperare la password");
            username.setWidthFull();

            username.setRequired(true);
            username.setPattern("^[A-Za-z][A-Za-z0-9_.]{4,29}$");

            Button recovery = new Button("Recupera");
            recovery.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            recovery.setWidthFull();
            recovery.addClickListener(click -> {

                if (username.isInvalid()) {
                    username.setErrorMessage("Inserire uno username valido");

                    return;
                }

                User user = userRepository.findByUsername(username.getValue());

                if (user != null) {

                    PasswordRecoveryToken prt = new PasswordRecoveryToken();
                    prt.setUser(user.getId());
                    prt.setToken(UUID.randomUUID());
                    prt.setCreationTs(ZonedDateTime.now().toEpochSecond());

                    try {
                        emailService.sendMessage(user.getEmail(), "Reimposta la password del tuo account hikehunter.it", EmailTemplate.PASSWORD_RECOVERY, prt.getToken().toString());

                        passwordRecoveryTokenService.save(prt);
                    } catch (MessagingException e) {
                        log.warn("Error sending recovery password email to {}, msg: {}", username.getValue(), e.getMessage());

                        Notification.show("Qualcosa è andato storto nell'invio dell'email. Riprova più tardi.", 5000, Notification.Position.BOTTOM_CENTER);
                    }
                }

                Notification.show("È stata inviata una email all'indirizzo associato, controlla la tua casella.", 3000, Notification.Position.BOTTOM_CENTER);

                UI.getCurrent().navigate(LoginView.class);
            });

            container.add(username, recovery);
        }
        else {

            log.trace("Found token '{}' in support view", token);

            passwordRecoveryTokenService.deleteOldTokens();

            UUID real;
            try {
                real = UUID.fromString(token);
            } catch (Exception e) {
                real = UUID.randomUUID();
            }

            PasswordRecoveryToken prt = passwordRecoveryTokenService.findByToken(real);

            if (prt == null) {

                log.info("Invalid token '{}' found, showing error view", token);

                Span span = new Span("Il link che hai cliccato è invalido o scaduto. Premi Continua per tornare alla pagina principale.");

                Button button = new Button("Continua", click -> {
                    UI.getCurrent().navigate(HomeView.class);
                });
                button.setWidthFull();
                button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                container.add(span, button);

                add(container);

                return;
            }

            User user = userRepository.getReferenceById(prt.getUser());

            log.trace("Token '{}' associated with user '{}'", token, user.getId());

            passwordRecoveryTokenService.delete(prt);

            H4 title = new H4("Reimposta la password");
            title.addClassNames(LumoUtility.Margin.NONE);

            container.add(title);

            FormLayout formLayout = new FormLayout();
            formLayout.setWidthFull();
            formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0px", 1),
                    new FormLayout.ResponsiveStep("600px", 2));

            PasswordField password = new PasswordField("Nuova password");
            password.setRequired(true);
            password.setPattern("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
            PasswordField confirm = new PasswordField("Ripeti password");
            confirm.setRequired(true);

            Button more = new Button(FontAwesome.Solid.QUESTION_CIRCLE.create());
            more.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            more.addClassNames(LumoUtility.Margin.NONE, LumoUtility.Padding.NONE);

            ContextMenu infoDialog = new ContextMenu(more);
            infoDialog.setOpenOnClick(true);
            infoDialog.addItem(new PasswordInfoWindow());

            password.setSuffixComponent(more);

            formLayout.add(password, confirm);

            Button next = new Button("Reimposta");
            next.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            next.addClickListener(click -> {

                if (password.isInvalid()) {
                    password.setErrorMessage("La password inserita non rispetta i vincoli");
                    more.click();

                    return;
                }

                if(confirm.getValue() == null || !confirm.getValue().equals(password.getValue())) {
                    confirm.setInvalid(true);
                    confirm.setErrorMessage("Le password non corrispondono");

                    return;
                }

                user.setHashedPassword(new BCryptPasswordEncoder().encode(password.getValue()));

                userRepository.save(user);

                Notification.show("Password resettata correttamente!", 3000, Notification.Position.BOTTOM_CENTER);

                UI.getCurrent().navigate(LoginView.class);
            });

            container.add(formLayout);

            container.addAndExpand(new Span());

            FormLayout formButtons = new FormLayout();
            formButtons.setWidthFull();
            formButtons.setResponsiveSteps(new FormLayout.ResponsiveStep("0px", 1),
                    new FormLayout.ResponsiveStep("600px", 2));

            formButtons.add(next);

            container.add(formButtons);
        }

        add(container);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String token) {

        final Location location = event.getLocation();
        final QueryParameters queryParameters = location.getQueryParameters();

        final Map<String, List<String>> parametersMap = queryParameters.getParameters();

        if (parametersMap != null && parametersMap.containsKey("t")) {

            List<String> tokenParam = parametersMap.get("t");
            if (tokenParam != null && !tokenParam.isEmpty()) {
                constructUI(tokenParam.get(0));

                return;
            }
        }

        constructUI(null);
    }
}
