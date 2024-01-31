package it.trekkete.hikehunter.ui.window;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class ContactInfoWindow extends VerticalLayout {

    public ContactInfoWindow() {}

    private void constructUI() {

        H5 title = new H5("Perché ti chiediamo la mail?");
        title.addClassNames(LumoUtility.Margin.NONE);

        Span content = new Span("La tua mail viene utilizzata per tutte le comunicazioni importanti riguardanti " +
                "le escursioni, come le iscrizioni di altri membri alle tue escursioni, la conferma di partecipazione " +
                "e i promemoria di partenza");

        add(title, content);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        constructUI();
    }
}
