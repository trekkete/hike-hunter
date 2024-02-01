package it.trekkete.hikehunter.ui.views.logged.preferences;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.trekkete.hikehunter.security.AuthenticatedUser;
import it.trekkete.hikehunter.ui.views.MainLayout;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.PermitAll;

@PageTitle("Termini legali")
@Route(value = "prefs/terms", layout = MainLayout.class)
@PermitAll
public class LegalTermsView extends VerticalLayout {

    private final AuthenticatedUser authenticatedUser;

    public LegalTermsView(@Autowired AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    private void constructUI() {

        setPadding(false);
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);

        VerticalLayout container = new VerticalLayout();
        container.setPadding(false);
        container.setSpacing(false);
        container.setMaxWidth("800px");

        add(container);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        constructUI();
    }
}
