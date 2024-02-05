package it.trekkete.hikehunter.ui.window;

import com.flowingcode.vaadin.addons.carousel.Carousel;
import com.flowingcode.vaadin.addons.carousel.Slide;
import com.google.gson.Gson;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
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
    private final Button selectButton;

    public QueryResultWindow(Map<String, JSONElement> elements) {
        this.elements = elements;
        this.selectButton = new Button("Seleziona");
    }

    private void constructUI() {

        setPadding(false);
        setMinWidth("300px");

        selectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        HorizontalLayout buttonContainer = new HorizontalLayout(selectButton);
        buttonContainer.addClassNames(
                LumoUtility.Width.FULL,
                LumoUtility.JustifyContent.END,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.Padding.Horizontal.SMALL);

        Slide[] slides = new Slide[elements.size()];

        for (int i = 0; i < elements.size(); i++) {

            JSONObject element = (JSONObject) elements.values().stream().toList().get(i);

            VerticalLayout slideContent = new VerticalLayout();

            JSONObject tags = element.getJSONObject("tags");

            tags.toMap().forEach((k, v) -> {
                slideContent.add(new Span(k + ": " + v));
            });

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

    public void addCloseListener(ComponentEventListener<ClickEvent<Button>> listener) {
        this.selectButton.addClickListener(listener);
    }
}
