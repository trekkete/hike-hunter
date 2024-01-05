package it.trekkete.hikehunter.ui.views.logged;

import com.google.gson.Gson;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.trekkete.hikehunter.data.entity.*;
import it.trekkete.hikehunter.data.service.*;
import it.trekkete.hikehunter.security.AuthenticatedUser;
import it.trekkete.hikehunter.ui.components.RatingStars;
import it.trekkete.hikehunter.ui.views.MainLayout;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.PermitAll;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

@PageTitle("Profilo")
@Route(value = "profile", layout = MainLayout.class)
@PermitAll
public class ProfileView extends VerticalLayout {

    private final TripRepository tripRepository;
    private final TripParticipantsRepository tripParticipantsRepository;
    private final AuthenticatedUser authenticatedUser;
    private final TripLocationRepository tripLocationRepository;
    private final LocationRepository locationRepository;
    private final UserRatingRepository userRatingRepository;

    private VerticalLayout tabContent;

    public ProfileView(@Autowired AuthenticatedUser authenticatedUser,
                       @Autowired TripRepository tripRepository,
                       @Autowired TripParticipantsRepository tripParticipantsRepository,
                       @Autowired TripLocationRepository tripLocationRepository,
                       @Autowired LocationRepository locationRepository,
                       @Autowired UserRatingRepository userRatingRepository) {
        this.authenticatedUser = authenticatedUser;
        this.tripRepository = tripRepository;
        this.tripParticipantsRepository = tripParticipantsRepository;
        this.tripLocationRepository = tripLocationRepository;
        this.locationRepository = locationRepository;
        this.userRatingRepository = userRatingRepository;

        constructUI();
    }

    public void constructUI() {

        User user = authenticatedUser.get().get();

        setPadding(false);
        setAlignItems(Alignment.CENTER);

        Long xp = 0L;

        VerticalLayout container = new VerticalLayout();
        container.setPadding(false);

        add(container);

        List<Trip> trips = tripParticipantsRepository.findAllByUser(user.getId()).stream().map(tripParticipants -> tripRepository.findTripById(tripParticipants.getTrip())).toList();
        List<Trip> tripsWithValue = tripParticipantsRepository.findAllByUserAndStatus(user.getId(), TripParticipants.Status.OK).stream().map(tripParticipants -> tripRepository.findTripById(tripParticipants.getTrip())).toList();

        for(Trip value : tripsWithValue) {
            xp += value.getXp();
        }

        FormLayout header = new FormLayout();
        header.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("300px", 2)
                );

        VerticalLayout image = new VerticalLayout();
        image.addClassNames(
                LumoUtility.JustifyContent.CENTER,
                LumoUtility.AlignItems.CENTER);

        HorizontalLayout nameAndLevel = new HorizontalLayout();
        nameAndLevel.setWidthFull();
        nameAndLevel.setJustifyContentMode(JustifyContentMode.AROUND);

        HorizontalLayout tripsAndRatings = new HorizontalLayout();
        tripsAndRatings.setWidthFull();

        UserExtendedData userExtendedData = new Gson().fromJson(user.getExtendedData(), UserExtendedData.class);

        boolean validExtendedData = userExtendedData != null;

        H3 name = new H3(validExtendedData ? (userExtendedData.getName() + " " + userExtendedData.getSurname()) : user.getUsername());
        name.getStyle().set("margin", "0");

        H5 levelLabel = new H5("Livello");
        levelLabel.getStyle().set("color", "gray");
        levelLabel.getStyle().set("margin", "0");
        H3 level = new H3(String.valueOf(user.getLevel(xp)));
        level.getStyle().set("margin", "0");

        HorizontalLayout nameLayout = new HorizontalLayout(name);
        nameLayout.addClassNames(
                LumoUtility.Width.FULL,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.JustifyContent.CENTER
        );

        HorizontalLayout levelLayout = new HorizontalLayout(levelLabel, level);
        levelLayout.setAlignItems(Alignment.BASELINE);

        Div progressBarLabel = new Div();
        progressBarLabel.setText(xp + "/" + user.getMaxXp(xp));

        ProgressBar progressBar = new ProgressBar();
        progressBar.setMin(0);
        progressBar.setMax(user.getMaxXp(xp));
        progressBar.setValue(xp);
        progressBar.addThemeVariants(ProgressBarVariant.LUMO_SUCCESS);

        VerticalLayout progressBarLayout = new VerticalLayout(progressBarLabel, progressBar);
        progressBarLayout.setSpacing(false);
        progressBarLayout.setPadding(false);
        progressBarLayout.setWidth("90%");
        progressBarLayout.setAlignItems(Alignment.CENTER);

        VerticalLayout levelBarLayout = new VerticalLayout(levelLayout, progressBarLayout);
        levelBarLayout.setWidthFull();
        levelBarLayout.setPadding(false);
        levelBarLayout.setSpacing(false);
        levelBarLayout.setAlignItems(Alignment.CENTER);
        levelBarLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        header.add(image, 2);
        header.add(nameLayout, 2);
        header.add(levelBarLayout, 2);

        Image src = new Image();
        src.setWidth("70px");

        if (validExtendedData && userExtendedData.getProfilePicture() != null) {
            src.setSrc(new StreamResource("profile-picture", () -> new ByteArrayInputStream(userExtendedData.getProfilePicture())));
        }
        else {
            src.setSrc("images/user.png");
        }

        image.add(src);

        container.add(header);

        int completedTrips = 0;
        int bookedTrips = 0;

        completedTrips = tripsWithValue.size();
        bookedTrips = trips.size() - completedTrips;

        tabContent = new VerticalLayout();
        tabContent.setWidth("97%");
        tabContent.getStyle()
                .set("background", "linear-gradient(#fff, #fff) 50% 50%/calc(100% - 2px) calc(100% - 2px) no-repeat, linear-gradient(0deg, transparent 0%, #eee 100%)")
                .set("margin-top", "-1em");

        Tab completedTab = new Tab(new Span("Completati"), createBadge(completedTrips));
        Tab bookedTab = new Tab(new Span("Prenotati"), createBadge(bookedTrips));
        bookedTab.getStyle().set("border-right", "1px solid #eee");

        Tabs tabs = new Tabs(bookedTab, completedTab);
        tabs.addThemeVariants(TabsVariant.LUMO_EQUAL_WIDTH_TABS);
        tabs.setWidth("97%");
        tabs.getStyle()
                .set("border", "1px solid #eee")
                .set("border-bottom", "0")
                .set("border-radius", "var(--lumo-border-radius) var(--lumo-border-radius) 0 0")
                .set("box-sizing", "border-box");

        tabs.addSelectedChangeListener(event -> {
            tabContent.removeAll();

            if (event.getSelectedTab().equals(completedTab)) {
                for (Trip completed : tripsWithValue) {
                    tabContent.add(createTripLayout(completed));
                }
            }
            else {
                for (Trip booked : trips) {
                    tabContent.add(createTripLayout(booked));
                }
            }
        });

        for (Trip booked : trips) {
            tabContent.add(createTripLayout(booked));
        }

        add(tabs);
        add(tabContent);
    }

    private Component createTripLayout(Trip trip) {

        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setPadding(false);
        container.setSpacing(false);
        container.setAlignItems(Alignment.CENTER);
        container.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H4 title = new H4(trip.getTitle());
        title.addClassNames(LumoUtility.Margin.NONE);

        container.add(title);

        Button chat = new Button(VaadinIcon.CHAT.create());
        chat.addClickListener(click -> {
            UI.getCurrent().navigate(ChatView.class, new RouteParameters("tripId", String.valueOf(trip.getId())));
        });

        Button review = new Button(VaadinIcon.USER_STAR.create());
        review.addClickListener(click -> {

            Dialog dialog = new Dialog();

            dialog.setHeaderTitle("Lascia una recensione dei tuoi compagni");

            H5 preps = new H5("Preparazione");

            RatingStars prepsStar = new RatingStars();

            H5 skill = new H5("Abilità");

            RatingStars skillStar = new RatingStars();

            H5 sociability = new H5("Socialità");

            RatingStars socStar = new RatingStars();

            Button closeButton = new Button(new Icon("lumo", "cross"), (e) -> dialog.close());
            closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            dialog.getHeader().add(closeButton);

            Button cancelButton = new Button("Annulla", (e) -> dialog.close());
            cancelButton.getStyle().set("margin-right", "auto");
            dialog.getFooter().add(cancelButton);

            Button confirmButton = new Button("Salva", (e) -> {

                List<TripParticipants> participants = tripParticipantsRepository.findAllByTrip(trip.getId());

                participants.forEach( tripParticipants -> {

                    UUID about = tripParticipants.getUser();

                    UUID from = authenticatedUser.get().get().getId();

                    if (from.equals(about))
                        return;

                    UserRating rating = new UserRating();
                    rating.setTrip(trip.getId());
                    rating.setFrom(from);
                    rating.setAbout(about);

                    rating.setPreparation(prepsStar.getValue());
                    rating.setSkill(skillStar.getValue());
                    rating.setSociability(socStar.getValue());

                    userRatingRepository.save(rating);

                });

                dialog.close();
            });
            confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            dialog.add(preps, prepsStar, skill, skillStar, sociability, socStar);

            dialog.getFooter().add(cancelButton, confirmButton);

            dialog.open();
        });

        container.add(new HorizontalLayout(chat, review));

        return container;
    }

    private Span createBadge(int value) {
        Span badge = new Span(String.valueOf(value));
        badge.getElement().getThemeList().add("badge small contrast");
        badge.getStyle().set("margin-inline-start", "var(--lumo-space-xs)");
        return badge;
    }
}
