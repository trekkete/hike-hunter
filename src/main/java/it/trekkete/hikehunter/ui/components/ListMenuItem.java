package it.trekkete.hikehunter.ui.components;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class ListMenuItem extends Button {

    public ListMenuItem(String text, ComponentEventListener<ClickEvent<Button>> clickListener) {
        this(text, null, clickListener);
    }

    public ListMenuItem(String text, Component icon, ComponentEventListener<ClickEvent<Button>> clickListener) {
        super(text, icon, clickListener);

        addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        addClassNames(
                LumoUtility.Margin.NONE,
                LumoUtility.Padding.Vertical.LARGE,
                LumoUtility.Width.FULL,
                LumoUtility.Border.BOTTOM,
                LumoUtility.BorderRadius.NONE,
                LumoUtility.BorderColor.CONTRAST_10,
                LumoUtility.TextColor.BODY);
        addThemeNames("left-align");
    }
}
