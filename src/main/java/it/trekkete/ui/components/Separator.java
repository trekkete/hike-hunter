package it.trekkete.ui.components;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class Separator extends VerticalLayout {

    public enum Orientation {
        HORIZONTAL, VERTICAL
    }

    private final VerticalLayout content;
    private final Orientation orientation;

    public Separator(Orientation orientation) {
        this.orientation = orientation;

        content = new VerticalLayout();
        content.setSpacing(false);
        content.setMargin(false);
        content.setPadding(false);

        setPadding(false);
        setSpacing(false);
        if (orientation.equals(Orientation.VERTICAL)) {

            setWidth("var(--separator-size)");
            getStyle().set("margin-right", "var(--separator-size)");
            setHeightFull();

            content.getStyle().set("border-right", "1px solid var(--lumo-contrast-20pct)");
            content.setWidthFull();
            content.setHeight("50%");
        }
        else {

            setHeight("var(--separator-size)");
            getStyle().set("margin-bottom", "var(--separator-size)");
            setWidthFull();

            content.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-20pct)");
            content.setWidth("95%");
            content.setHeightFull();
        }

        content.getStyle().set("margin", "auto");
        add(content);
    }

    public void setColor(String color) {
        if (orientation.equals(Orientation.VERTICAL))
            content.getStyle().set("border-right", "1px solid " + color);
        else
            content.getStyle().set("border-bottom", "1px solid " + color);
    }
}
