package it.trekkete.ui.views.parti;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.trekkete.data.entity.Location;
import it.trekkete.data.entity.Trip;
import it.trekkete.data.entity.TripParticipants;
import it.trekkete.data.service.LocationRepository;
import it.trekkete.data.service.TripParticipantsRepository;
import it.trekkete.data.service.TripRepository;
import it.trekkete.security.AuthenticatedUser;
import it.trekkete.ui.views.MainLayout;
import it.trekkete.ui.views.esplora.EsploraView;
import it.trekkete.ui.views.esplora.EsploraViewCard;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.PermitAll;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@PageTitle("Parti")
@Route(value = "new-trip", layout = MainLayout.class)
@PermitAll
public class PartiView extends VerticalLayout {

    public enum TripMode {
        GIORNATA,
        MULTI
    }

    private TextField title;
    private TextField description;
    private DatePicker startDate;
    private DatePicker endDate;
    private RadioButtonGroup<TripMode> tripMode;
    private Select<Integer> rating;
    private TextField maxNumber;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    public PartiView(@Autowired AuthenticatedUser authenticatedUser,
                     @Autowired TripRepository tripRepository,
                     @Autowired TripParticipantsRepository tripParticipantsRepository) {
        addClassName("parti-view");
        getStyle()
                .set("background-image", "url('https://images.unsplash.com/photo-1502751106709-b3812c57da19?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1332&q=80')")
                .set("background-repeat", "no-repeat")
                .set("background-size", "cover");

        setHeightFull();

        VerticalLayout container = new VerticalLayout();
        container.addClassNames("main-container");

        container.add(createTitle());
        container.add(createFormLayout());
        container.add(createButtonLayout());

        save.addClickListener(click -> {
            Trip newTrip = new Trip();
            newTrip.setTitle(title.getValue());
            newTrip.setDescription(description.getValue());

            newTrip.setCreationTs(ZonedDateTime.now().toEpochSecond());

            newTrip.setRating(rating.getValue());
            newTrip.setMaxParticipants(Integer.parseInt(maxNumber.getValue()));

            UUID creatorId = authenticatedUser.get().get().getId();

            newTrip.setCreator(creatorId);

            newTrip.setStartDate(startDate.getValue().atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond());
            newTrip.setEndDate(endDate.getValue().atTime(23, 59).atZone(ZoneId.systemDefault()).toEpochSecond());

            tripRepository.save(newTrip);

            TripParticipants tp = new TripParticipants();
            tp.setTrip(newTrip.getId());
            tp.setUser(creatorId);

            tripParticipantsRepository.save(tp);

            clearForm();
        });

        cancel.addClickListener(click -> UI.getCurrent().navigate(EsploraView.class));

        add(container);
    }

    private Component createTitle() {
        return new H3("Crea un'escursione");
    }

    private Component createFormLayout() {
        FormLayout formLayout = new FormLayout();

        title = new TextField("Titolo");
        description = new TextField("Descrizione");

        startDate = new DatePicker("Partenza");
        startDate.setMin(LocalDate.now());
        startDate.setWidth("49%");
        endDate = new DatePicker("Ritorno");
        endDate.setMin(LocalDate.now());

        tripMode = new RadioButtonGroup<>("Durata");
        tripMode.setItemLabelGenerator(mode -> {
            switch (mode)  {
                case GIORNATA -> {
                    return "In giornata";
                }
                case MULTI -> {
                    return "Più giorni";
                }
            }

            return "?";
        });
        tripMode.setItems(TripMode.values());
        tripMode.setValue(TripMode.GIORNATA);

        HorizontalLayout dates = new HorizontalLayout(startDate);

        formLayout.add(title, description, tripMode, dates);

        tripMode.addValueChangeListener(event -> {

            if (event.getValue().equals(TripMode.GIORNATA)) {
                dates.remove(endDate);
                startDate.setWidth("49%");
            }
            else {
                dates.add(endDate);
                startDate.setWidth("100%");
                endDate.setWidth("100%");
            }
        });

        rating = new Select<>();
        rating.setLabel("Difficoltà prevista");
        rating.setItems(1, 2, 3, 4, 5);
        rating.setItemLabelGenerator(Trip::formatRating);

        maxNumber = new TextField("Numero massimo di partecipanti");

        formLayout.add(rating, maxNumber);

        H4 itinerari = new H4("Aggiungi itinerari");

        formLayout.add(itinerari, 2);

        return formLayout;
    }

    private Component createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addClassName("button-layout");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save);
        buttonLayout.add(cancel);
        return buttonLayout;
    }

    private void clearForm() {
       title.clear();
       description.clear();
       startDate.clear();
       endDate.clear();
       tripMode.setValue(TripMode.GIORNATA);
       rating.clear();
       maxNumber.clear();
    }

}
