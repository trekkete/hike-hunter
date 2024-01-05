package it.trekkete.hikehunter.ui.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.UUID;

public class ShowMore extends VerticalLayout {

    private Paragraph content;

    public ShowMore(String text) {

        addClassName("show-more-container");
        setPadding(false);
        setSpacing(false);

        String contentId = "show-more-" + UUID.randomUUID();

        content = new Paragraph(text);
        content.addClassName("show-more-content");
        content.setId(contentId);

        Button show = new Button(new Icon(VaadinIcon.CHEVRON_DOWN));
        show.addClassName("show-more-button");
        show.addClickListener(click -> {

            if (content.hasClassName("open")) {
                content.removeClassName("open");

                show.setIcon(new Icon(VaadinIcon.CHEVRON_DOWN));
            }
            else {
                content.addClassName("open");

                show.setIcon(new Icon(VaadinIcon.CHEVRON_UP));
            }
        });

        HorizontalLayout showContainer = new HorizontalLayout(show);
        showContainer.addClassName("show-more-button-container");

        getElement().executeJs("return document.getElementById($0).scrollHeight > document.getElementById($0).clientHeight", contentId, contentId)
                .then(Boolean.class, showContainer::setVisible);

        add(content, showContainer);
    }

    public void setContent(String text) {
        content.setText(text);
    }

    public void setBaseHeight(String height) {
        content.setMaxHeight(height);
    }
}
