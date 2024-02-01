package it.trekkete.hikehunter.ui.views.logged.preferences;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.trekkete.hikehunter.security.AuthenticatedUser;
import it.trekkete.hikehunter.ui.views.MainLayout;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.PermitAll;

@PageTitle("Modifica profilo")
@Route(value = "prefs/edit-profile", layout = MainLayout.class)
@PermitAll
public class EditProfileView extends VerticalLayout {

    private final AuthenticatedUser authenticatedUser;

    public EditProfileView(@Autowired AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    private void constructUI() {

        setPadding(false);
        setSizeFull();
        setAlignItems(Alignment.CENTER);

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
