package it.trekkete.ui.views.login;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import it.trekkete.ui.components.RegistrationForm;
import it.trekkete.ui.components.RegistrationFormBinder;

@PageTitle(value = "Registrati")
@Route(value = "register")
@AnonymousAllowed
public class RegistrationView extends VerticalLayout {

    public RegistrationView() {
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

        RegistrationFormBinder registrationFormBinder = new RegistrationFormBinder(registrationForm);
        registrationFormBinder.addBindingAndValidation();
    }
}