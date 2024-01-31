package it.trekkete.hikehunter.ui.views.login;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.trekkete.hikehunter.security.AuthenticatedUser;
import it.trekkete.hikehunter.security.SecurityConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.PermitAll;
import java.util.List;
import java.util.Map;

@PageTitle("Login")
@Route(value = "signin")
@PermitAll
public class LoginView extends VerticalLayout implements HasUrlParameter<String> {

    private final AuthenticatedUser authenticatedUser;
    private final LoginForm loginForm;
    private final LoginI18n loginI18n;

    public LoginView(@Autowired AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;

        loginForm = new LoginForm();
        loginI18n = LoginI18n.createDefault();
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {

        final Location location = event.getLocation();
        final QueryParameters queryParameters = location.getQueryParameters();

        final Map<String, List<String>> parametersMap = queryParameters.getParameters();

        if (parametersMap != null && parametersMap.containsKey("errorMessage")) {

            loginForm.setError(true);

            // Setto il messagigo di errore nella lingua corretta
            LoginI18n.ErrorMessage i18nErrorMessage = loginI18n.getErrorMessage();
            i18nErrorMessage.setTitle("Credenziali incorrette");
            i18nErrorMessage.setMessage("Controlla di aver inserito le credenziali giuste e riprova.");

            loginI18n.setErrorMessage(i18nErrorMessage);
            loginForm.setI18n(loginI18n);
        }
    }

    private void constructUI() {

        setSpacing(false);
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

        loginForm.setAction("." + SecurityConfiguration.LOGIN_PROCESSING_URL);
        loginForm.addForgotPasswordListener(event -> {
            UI.getCurrent().navigate(PasswordRecoveryView.class);
        });

        loginI18n.setAdditionalInformation(null);

        LoginI18n.Form form = loginI18n.getForm();
        form.setTitle("");
        form.setSubmit("Accedi");
        form.setForgotPassword("Password dimenticata? Recuperala.");

        loginForm.setI18n(loginI18n);
        loginForm.setId("loginForm");

        Button register = new Button("Non hai un account? Registrati!");
        register.addClickListener(click -> {
            UI.getCurrent().navigate(RegistrationView.class);
        });
        register.setWidthFull();

        container.add(loginForm, register);

        add(container);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        constructUI();
    }
}
