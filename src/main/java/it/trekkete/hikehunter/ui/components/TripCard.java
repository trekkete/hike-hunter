package it.trekkete.hikehunter.ui.components;

import com.google.gson.Gson;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouteParameters;
import it.trekkete.hikehunter.data.entity.*;
import it.trekkete.hikehunter.data.service.LocationRepository;
import it.trekkete.hikehunter.data.service.TripLocationRepository;
import it.trekkete.hikehunter.data.service.TripParticipantsRepository;
import it.trekkete.hikehunter.data.service.UserRepository;
import it.trekkete.hikehunter.security.AuthenticatedUser;
import it.trekkete.hikehunter.ui.views.logged.JoinView;
import org.apache.lucene.util.SloppyMath;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TripCard extends ListItem {

    private String[] randoms = new String[] {
            "https://images.unsplash.com/photo-1519681393784-d120267933ba?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=750&q=80",
            "https://images.unsplash.com/photo-1512273222628-4daea6e55abb?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=750&q=80",
            "https://images.unsplash.com/photo-1536048810607-3dc7f86981cb?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=375&q=80",
            "https://images.unsplash.com/photo-1515705576963-95cad62945b6?ixlib=rb-1.2.1&ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&auto=format&fit=crop&w=750&q=80",
            "https://images.unsplash.com/photo-1513147122760-ad1d5bf68cdb?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1562832135-14a35d25edef?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=815&q=80"
    };

    public TripCard(Trip trip,
                    AuthenticatedUser authenticatedUser,
                    UserRepository userRepository,
                    TripParticipantsRepository tripParticipantsRepository,
                    TripLocationRepository tripLocationRepository,
                    LocationRepository locationRepository,
                    boolean isLocalized,
                    Location userLocation) {

        addClassNames("bg-contrast-5", "flex", "flex-col", "items-start");
        getStyle().set("cursor", "pointer").set("font-size", "0.8em")
                        .set("box-shadow", "0px 0px 8px -2px rgba(0,0,0,0.5)");
        setMaxWidth("180px");
        setMinWidth("180px");
        setMaxHeight("250px");
        setMinHeight("250px");

        Div div = new Div();
        div.addClassNames("bg-contrast", "flex items-center", "justify-center", "overflow-hidden", "w-full");
        div.setHeight("160px");
        div.getStyle().set("position", "relative");

        Image image = new Image();
        image.setWidth("100%");

        Div shadow = new Div();
        shadow.setSizeFull();
        shadow.getStyle()
                .set("position", "absolute")
                .set("bottom", "0")
                .set("left", "0")
                .set("background-image", "linear-gradient(to top, rgb(0, 0, 0), rgba(0,0,0,0))")
                .set("z-index", "1");

        Span duration = new Span();
        duration.getStyle()
                .set("color", "white")
                .set("position", "absolute")
                .set("bottom", "2px")
                .set("left", "5px")
                .set("z-index", "1");
        duration.setText(formatDuration(trip.getStartDate(), trip.getEndDate()));

        div.add(shadow, duration);

        String url = getRandomUrl();
        if(url == null) {
            image.setSrc(getRandomUrl());
        } else {
            image.setSrc(url);
        }
        image.setAlt(trip.getTitle());

        div.add(image);

        Span header = new Span();
        header.addClassNames("font-semibold");
        header.setText(trip.getTitle());

        Span subtitle = new Span();
        subtitle.addClassNames("text-secondary");
        subtitle.add(getCreatorInfo(trip.getCreator(), userRepository));

        addClickListener(click -> {
           UI.getCurrent().navigate(JoinView.class, new RouteParameters("tripId", String.valueOf(trip.getId())));
        });

        VerticalLayout content = new VerticalLayout(header, subtitle);
        content.setSpacing(false);
        content.setPadding(false);
        content.getStyle().set("padding", "0.5em");

        Span locations = new Span();
        List<TripLocation> tripLocations = tripLocationRepository.findAllByTripOrderByIndex(trip.getId());
        if (tripLocations.size() > 0) {
            locations.setText(tripLocations.size() + " itinerari" + (tripLocations.size() == 1 ? "o" : ""));
        }
        content.add(locations);

        Location first = locationRepository.findLocationById(tripLocations.get(0).getLocation());

        if (isLocalized) {
            double distance = SloppyMath.haversinMeters(userLocation.getLatitude(), userLocation.getLongitude(), first.getLatitude(), first.getLongitude());
            Span kms = new Span(((int) distance/1000) + " km da te");

            content.add(kms);
        }

        content.addAndExpand(new Span());

        HorizontalLayout footer = new HorizontalLayout(generateDifficultyBadge(trip.getRating()));
        footer.setSpacing(false);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footer.setAlignItems(FlexComponent.Alignment.BASELINE);

        boolean isAuthenticated = authenticatedUser.get().isPresent();

        if (isAuthenticated && tripParticipantsRepository.findByTripAndUser(trip.getId(), authenticatedUser.get().get().getId()) != null) {
            Icon icon = new Icon(VaadinIcon.CHECK_CIRCLE);
            icon.setColor("var(--lumo-primary-color)");

            Span span = new Span(icon);
            footer.add(span);
        }
        else {
            if (trip.getMaxParticipants() != null && trip.getMaxParticipants() > 0) {
                Span remaining = new Span((trip.getMaxParticipants() - tripParticipantsRepository.countAllByTrip(trip.getId())) + " posti");
                footer.add(remaining);
            }
        }

        content.add(footer);

        add(div, content);

    }

    private String getRandomUrl() {
        return randoms[new Random().nextInt(randoms.length)];
    }

    private String formatDuration(long start, long end) {

        ZonedDateTime startTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(start), ZoneId.systemDefault());
        ZonedDateTime endTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(end), ZoneId.systemDefault());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLL yyyy");

        if (ChronoUnit.DAYS.between(startTime, endTime) == 0) {
            return formatter.format(startTime) + " - in giornata";
        }
        else {
            return formatter.format(startTime) + " - " + (ChronoUnit.DAYS.between(startTime, endTime) + 1) + " giorni";
        }
    }

    private Component generateDifficultyBadge(Integer rating) {

        Span badge = new Span();
        badge.getElement().setAttribute("theme", "badge");
        badge.setText(Trip.formatRating(rating));

        switch (rating) {
            case 3 -> badge.getStyle().set("background-color", "hsla(47, 100%, 35%, 0.1)").set("color", "hsl(47, 100%, 35%)");
            case 4, 5 -> badge.getStyle().set("background-color", "hsla(0, 100%, 35%, 0.1)").set("color", "hsl(0, 100%, 35%)");
        }

        return badge;
    }

    private Component getCreatorInfo(UUID creatorId, UserRepository userRepository) {

        User user = userRepository.getReferenceById(creatorId);

        UserExtendedData extendedData = new Gson().fromJson(user.getExtendedData(), UserExtendedData.class);

        if (extendedData != null) {
            return new Span("Proposta da: " + extendedData.getName() + " " + extendedData.getSurname());
        }
        else {
            return new Span("Proposta da: " + user.getUsername());
        }
    }
}
