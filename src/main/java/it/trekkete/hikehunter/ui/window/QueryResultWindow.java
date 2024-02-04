package it.trekkete.hikehunter.ui.window;

import com.flowingcode.vaadin.addons.carousel.Carousel;
import com.flowingcode.vaadin.addons.carousel.Slide;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import kong.unirest.json.JSONElement;
import kong.unirest.json.JSONObject;

import java.util.Map;

public class QueryResultWindow extends VerticalLayout {

    private final Map<String, JSONElement> elements;

    public QueryResultWindow(Map<String, JSONElement> elements) {
        this.elements = elements;
    }

    private void constructUI() {

        setPadding(false);

        Button select = new Button("Seleziona");
        select.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        HorizontalLayout buttonContainer = new HorizontalLayout(select);
        buttonContainer.addClassNames(
                LumoUtility.Width.FULL,
                LumoUtility.JustifyContent.END,
                LumoUtility.AlignItems.CENTER);

        Slide[] slides = new Slide[elements.size()];

        for (int i = 0; i < elements.size(); i++) {

            JSONObject element = (JSONObject) elements.values().stream().toList().get(i);

            VerticalLayout slideContent = new VerticalLayout();

            slideContent.add(new Span(element.getString("name")));

            slides[i] = new Slide(slideContent);
        }

        Carousel carousel = new Carousel(slides);

        add(buttonContainer, carousel);

    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        constructUI();
    }
}
