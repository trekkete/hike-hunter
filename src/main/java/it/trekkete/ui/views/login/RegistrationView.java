package it.trekkete.ui.views.login;

import com.google.gson.Gson;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import it.trekkete.data.Role;
import it.trekkete.data.entity.User;
import it.trekkete.data.entity.UserExtendedData;
import it.trekkete.data.service.UserRepository;
import it.trekkete.ui.components.RegistrationForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.ZonedDateTime;
import java.util.Set;

@PageTitle(value = "Registrati")
@Route(value = "register")
@AnonymousAllowed
public class RegistrationView extends VerticalLayout {

    public RegistrationView(@Autowired UserRepository userRepository) {
        RegistrationForm registrationForm = new RegistrationForm();
        // Center the RegistrationForm
        setHorizontalComponentAlignment(Alignment.CENTER, registrationForm);

        //getElement().setAttribute("theme", "hike-hunter");

        Div header = new Div();

        H1 title = new H1("hike-hunter");
        title.getStyle().set("color", "white");
        header.add(title);
        header.setSizeFull();
        header.getStyle()
                .set("background-color", "var(--lumo-primary-color)")
                .set("padding", "var(--lumo-space-l) var(--lumo-space-xl) var(--lumo-space-l) var(--lumo-space-l)")
                .set("box-sizing", "border-box")
                .set("overflow", "hidden")
                .set("border-radius", "var(--lumo-border-radius-l) var(--lumo-border-radius-l) 0 0")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("justify-content", "flex-end");

        Div content = new Div();
        content.getStyle()
                .set("padding", "var(--lumo-space-l) var(--lumo-space-xl) var(--lumo-space-l) var(--lumo-space-l)");
        content.add(registrationForm);

        VerticalLayout container = new VerticalLayout(header, content);
        container.setSpacing(false);
        container.setPadding(false);
        container.setMaxWidth("calc(var(--lumo-size-m) * 20)");
        container.getStyle()
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("margin", "var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-l)");

        setAlignItems(Alignment.CENTER);
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);

        add(container);

        registrationForm.getSubmitButton().addClickListener(click -> {

            String pass1 = registrationForm.getPasswordField().getValue();
            String pass2 = registrationForm.getPasswordConfirmField().getValue();

            if (userRepository.findByUsername(registrationForm.getEmail().getValue()) != null) {
                registrationForm.getEmail().setInvalid(true);
                registrationForm.getEmail().setErrorMessage("Esiste già un account associato a questa mail");
            }

            if (pass1.length() < 8) {
                registrationForm.getPasswordField().setInvalid(true);
                registrationForm.getPasswordField().setErrorMessage("Deve contenere almeno 8 caratteri");

                return;
            }

            if (!pass1.equals(pass2)) {
                registrationForm.getPasswordConfirmField().setInvalid(true);
                registrationForm.getPasswordConfirmField().setErrorMessage("Le password non sono uguali");

                return;
            }

            User user = new User();

            UserExtendedData extendedData = new UserExtendedData();
            extendedData.setEmail(registrationForm.getEmail().getValue());
            extendedData.setName(registrationForm.getFirstName().getValue());
            extendedData.setSurname(registrationForm.getLastName().getValue());
            extendedData.setPhoneNumber(registrationForm.getPhoneNumber().getValue());

            user.setUsername(registrationForm.getEmail().getValue());
            user.setRoles(Set.of(Role.USER));
            user.setHashedPassword(new BCryptPasswordEncoder().encode(registrationForm.getPasswordField().getValue()));
            user.setCreationTs(ZonedDateTime.now().toEpochSecond());
            user.setExtendedData(new Gson().toJson(extendedData));

            userRepository.save(user);

            Notification.show("Benvenuto su Hike Hunter", 3, Notification.Position.BOTTOM_END);

            UI.getCurrent().navigate(LoginView.class);
        });
    }
}