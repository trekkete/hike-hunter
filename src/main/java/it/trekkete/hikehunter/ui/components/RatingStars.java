package it.trekkete.hikehunter.ui.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RatingStars extends Div {

    private Integer value;

    private List<Span> stars;

    public RatingStars() {
        super();

        UUID id = UUID.randomUUID();

        addClassName("rating");

        stars = new ArrayList<>();

        for (int i = 5; i >= 1; i--) {

            Input star = new Input();
            star.setValue(String.valueOf(i));
            star.setType("radio");
            star.setId(id + "-" + i);

            Label label = new Label("☆");
            label.setFor(id + "-" + i);

            int finalI = i;
            star.addValueChangeListener(event -> {
                System.out.println("Value of " + finalI + ": " + event.getValue());
            });

            /*star.addClickListener(click -> {
                value = finalI + 1;
            });

            stars.add(span);*/
            add(star, label);
        }
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
