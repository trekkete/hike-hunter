package it.trekkete.hikehunter.ui.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.UUID;

public class ShowMore extends VerticalLayout {

    private final Paragraph content;

    public ShowMore(String text) {

        String contentId = "show-more-" + UUID.randomUUID();

        content = new Paragraph(text);
        content.addClassName("show-more-content");
        content.setId(contentId);

    }

    private void constructUI() {

        addClassName("show-more-container");
        setPadding(false);
        setSpacing(false);

        Button show = new Button(FontAwesome.Solid.CHEVRON_DOWN.create());
        show.addClassName("show-more-button");
        show.addClickListener(click -> {

            if (content.hasClassName("open")) {
                content.removeClassName("open");

                show.setIcon(FontAwesome.Solid.CHEVRON_DOWN.create());
            }
            else {
                content.addClassName("open");

                show.setIcon(FontAwesome.Solid.CHEVRON_UP.create());
            }
        });

        HorizontalLayout showContainer = new HorizontalLayout(show);
        showContainer.addClassName("show-more-button-container");

        String contentId = content.getId().get();

        getElement().executeJs("return document.getElementById($0).scrollHeight > document.getElementById($0).clientHeight", contentId, contentId)
                .then(Boolean.class, showContainer::setVisible);

        add(content, showContainer);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        constructUI();
    }

    public void setContent(String text) {
        content.setText(text);
    }

    public void setBaseHeight(String height) {
        content.setMaxHeight(height);
    }
}
