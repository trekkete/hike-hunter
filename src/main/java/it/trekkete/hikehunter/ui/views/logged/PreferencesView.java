package it.trekkete.hikehunter.ui.views.logged;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.trekkete.hikehunter.security.AuthenticatedUser;
import it.trekkete.hikehunter.ui.views.MainLayout;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.PermitAll;

@PageTitle("Impostazioni")
@Route(value = "prefs", layout = MainLayout.class)
@PermitAll
public class PreferencesView extends VerticalLayout {

    private final AuthenticatedUser authenticatedUser;

    public PreferencesView(@Autowired AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;

        constructUI();
    }

    private void constructUI() {

        setPadding(false);
        setSizeFull();

        Button logout = new Button("Logout", click -> {
            authenticatedUser.logout();
        });

        add(logout);
    }
}
