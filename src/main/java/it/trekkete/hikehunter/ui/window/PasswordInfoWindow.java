package it.trekkete.hikehunter.ui.window;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class PasswordInfoWindow extends VerticalLayout {

    public PasswordInfoWindow() {}

    private void constructUI() {

        H5 title = new H5("Come deve essere la tua password?");
        title.addClassNames(LumoUtility.Margin.NONE);

        Span content = new Span("Per motivi di sicurezza, la tua password deve rispettare i seguenti vincoli:");
        ListItem[] items = new ListItem[] {
                new ListItem("Deve essere almeno di 8 caratteri"),
                new ListItem("Deve contenere almeno una lettera maiuscola, minuscola, un numero e un carattere speciale tra @ $ ! % * ? &")
        };

        add(title, content);
        add(items);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        constructUI();
    }
}
