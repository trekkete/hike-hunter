package it.trekkete.ui.views.esplora;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Section;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import it.trekkete.ui.views.MainLayout;

@PageTitle("Come funziona?")
@Route(value = "about", layout = MainLayout.class)
@AnonymousAllowed
public class ComeFunzionaView extends VerticalLayout {

    public ComeFunzionaView() {

        constructUI();
    }

    public void constructUI() {
        getStyle().set("background-image", "url('images/background.png')");

        VerticalLayout container = new VerticalLayout();
        container.addClassNames("about-view", "main-container");
        container.setPadding(false);

        container.add(createSection("Vuoi andare in montagna ma non sai con chi?", 0));
        container.add(createSection("Scorri nella sezione 'Esplora' per vedere le escursioni create dagli altri utenti", 1));
        container.add(createSection("Registrati per un'escursione esistente", 2));
        container.add(createSection("Non trovi niente che ti piace? Crea tu un'escursione!", 3));

        add(container);
    }

    private Section createSection(String text, int index) {
        Section one = new Section();
        one.addClassNames("about-view", "section");

        H4 h4 = new H4(text);

        if (index % 2 != 0) {
            one.getStyle().set("background-color", "white")
                    .set("justify-content", "end");
            h4.getStyle().set("color", "black")
                    .set("margin-right", "4em");
        }
        else {
            h4.getStyle().set("margin-left", "4em");
        }

        one.add(h4);

        return one;
    }
}
