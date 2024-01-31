package it.trekkete.hikehunter.ui.views.login;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;

@PageTitle("Recupera password")
@Route(value = "/support")
@AnonymousAllowed
public class PasswordRecoveryView extends VerticalLayout {

    public PasswordRecoveryView() {}

    public void constructUI() {

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

        TextField email = new TextField("Email");
        email.setHelperText("Verrà inviata una mail all'indirizzo indicato per recuperare la password");
        email.setWidthFull();

        Button recovery = new Button("Recupera");
        recovery.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        recovery.setWidthFull();
        recovery.addClickListener(click -> {

        });

        container.add(email, recovery);

        add(container);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        constructUI();
    }
}
