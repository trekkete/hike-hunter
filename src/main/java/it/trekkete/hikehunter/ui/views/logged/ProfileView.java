package it.trekkete.hikehunter.ui.views.logged;

import com.google.gson.Gson;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.StreamResource;
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

        Long xp = 0L;

        //getStyle().set("background-image", "url('images/background.png')");
        getStyle().set("background-color", "#00680082");
        setMinHeight("100%");

        VerticalLayout container = new VerticalLayout();
        container.addClassNames("esplora-view", "main-container");

        add(container);

        List<Trip> trips = tripParticipantsRepository.findAllByUser(user.getId()).stream().map(tripParticipants -> tripRepository.findTripById(tripParticipants.getTrip())).toList();
        List<Trip> tripsWithValue = tripParticipantsRepository.findAllByUserAndStatus(user.getId(), TripParticipants.Status.OK).stream().map(tripParticipants -> tripRepository.findTripById(tripParticipants.getTrip())).toList();

        for(Trip value : tripsWithValue) {
            xp += value.getXp();
        }

        HorizontalLayout infos = new HorizontalLayout();
        infos.setWidthFull();

        VerticalLayout image = new VerticalLayout();
        image.setWidth("30%");

        HorizontalLayout nameAndLevel = new HorizontalLayout();
        nameAndLevel.setWidthFull();
        nameAndLevel.setJustifyContentMode(JustifyContentMode.AROUND);

        HorizontalLayout tripsAndRatings = new HorizontalLayout();
        tripsAndRatings.setWidthFull();

        UserExtendedData userExtendedData = new Gson().fromJson(user.getExtendedData(), UserExtendedData.class);

        H3 nameLabel = new H3("Nome");
        nameLabel.getStyle().set("color", "gray");
        H1 name = new H1(userExtendedData.getName() + " " + userExtendedData.getSurname());

        H3 levelLabel = new H3("Livello");
        levelLabel.getStyle().set("color", "gray");
        levelLabel.getStyle().set("margin", "0");
        H1 level = new H1(String.valueOf(user.getLevel(xp)));
        level.getStyle().set("margin", "0");

        HorizontalLayout nameLayout = new HorizontalLayout(nameLabel, name);
        nameLayout.setAlignItems(Alignment.BASELINE);
        nameLayout.setWidthFull();

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
        progressBarLayout.setWidth("40%");
        progressBarLayout.setAlignItems(Alignment.CENTER);

        VerticalLayout levelBarLayout = new VerticalLayout(levelLayout, progressBarLayout);
        levelBarLayout.setWidthFull();
        levelBarLayout.setPadding(false);
        levelBarLayout.setAlignItems(Alignment.CENTER);
        levelBarLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        nameAndLevel.add(nameLayout, levelBarLayout);

        H3 totalTripsLabel = new H3("Viaggi");
        totalTripsLabel.getStyle().set("color", "gray");
        H1 totalTrips = new H1(String.valueOf(trips.size()));

        HorizontalLayout totalTripsLayout = new HorizontalLayout(totalTripsLabel, totalTrips);
        totalTripsLayout.setAlignItems(Alignment.BASELINE);

        tripsAndRatings.add(totalTripsLayout);

        Image src = new Image();

        if (userExtendedData.getProfilePicture() != null) {
            src.setSrc(new StreamResource("profile-picture", () -> new ByteArrayInputStream(userExtendedData.getProfilePicture())));
        }
        else {
            src.setSrc("images/user.png");
            src.setWidthFull();
        }

        image.add(src);

        infos.add(image, new VerticalLayout(nameAndLevel, tripsAndRatings));

        container.add(infos);

        VerticalLayout tripContainer = new VerticalLayout();
        tripContainer.addClassNames("esplora-view", "main-container");

        for (Trip trip : trips) {
            tripContainer.add(createTripLayout(trip));
        }

        add(tripContainer);
    }

    private Component createTripLayout(Trip trip) {

        HorizontalLayout container = new HorizontalLayout();
        container.setWidthFull();
        container.setAlignItems(Alignment.BASELINE);
        container.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H4 title = new H4(trip.getTitle());
        title.addClassNames("m-0");

        container.add(title);

        Button chat = new Button(new Icon(VaadinIcon.CHAT));
        chat.addClickListener(click -> {
            UI.getCurrent().navigate(ChatView.class, new RouteParameters("tripId", String.valueOf(trip.getId())));
        });

        Button review = new Button("Lascia una recensione");
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
}
