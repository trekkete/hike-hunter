package it.trekkete.hikehunter.ui.window;

import com.flowingcode.vaadin.addons.carousel.Carousel;
import com.flowingcode.vaadin.addons.carousel.Slide;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.trekkete.hikehunter.overpass.OverpassElementKeys;
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

        removeAll();

        setPadding(false);
        setSpacing(false);
        setMinWidth("300px");
        setMaxHeight("calc(var(--paper-slide-height) + 60px)");

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
            slideContent.setMaxHeight("calc(var(--paper-slide-height) - 50px)");
            slideContent.getStyle().set("overflow-y", "scroll");

            Map<String, Object> tags = element.getJSONObject("tags").toMap();

            if (tags.containsKey(OverpassElementKeys.NAME)) {

                H4 title = new H4(String.valueOf(tags.get(OverpassElementKeys.NAME)));
                title.addClassNames(LumoUtility.Margin.NONE);

                slideContent.add(title);
            }

            slides[i] = new Slide(slideContent);
        }

        Carousel carousel = new Carousel(slides);
        carousel.setWidthFull();

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
