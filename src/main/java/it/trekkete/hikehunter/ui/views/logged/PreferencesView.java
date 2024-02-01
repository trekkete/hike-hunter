package it.trekkete.hikehunter.ui.views.logged;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.trekkete.hikehunter.security.AuthenticatedUser;
import it.trekkete.hikehunter.ui.components.ListMenuItem;
import it.trekkete.hikehunter.ui.views.MainLayout;
import it.trekkete.hikehunter.ui.views.logged.preferences.ConfigurationView;
import it.trekkete.hikehunter.ui.views.logged.preferences.EditProfileView;
import it.trekkete.hikehunter.ui.views.logged.preferences.HelpdeskView;
import it.trekkete.hikehunter.ui.views.logged.preferences.LegalTermsView;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.PermitAll;

@PageTitle("Impostazioni")
@Route(value = "prefs", layout = MainLayout.class)
@PermitAll
public class PreferencesView extends VerticalLayout {

    private AuthenticatedUser authenticatedUser;

    private final ListMenuItem[] menu = new ListMenuItem[] {
            new ListMenuItem("Modifica profilo", FontAwesome.Solid.USER_PEN.create(), click -> UI.getCurrent().navigate(EditProfileView.class)),
            new ListMenuItem("Impostazioni", FontAwesome.Solid.COG.create(), click -> UI.getCurrent().navigate(ConfigurationView.class)),
            new ListMenuItem("Termini legali", FontAwesome.Solid.LEGAL.create(), click -> UI.getCurrent().navigate(LegalTermsView.class)),
            new ListMenuItem("Assistenza", FontAwesome.Solid.QUESTION.create(), click -> UI.getCurrent().navigate(HelpdeskView.class)),
            new ListMenuItem("Esci", FontAwesome.Solid.SIGN_OUT_ALT.create(), click -> authenticatedUser.logout()){{
                this.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.FontWeight.BOLD);
            }}
    };

    public PreferencesView(@Autowired AuthenticatedUser authenticatedUser) {
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

        for (ListMenuItem item : menu) {
            container.add(item);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        constructUI();
    }
}
