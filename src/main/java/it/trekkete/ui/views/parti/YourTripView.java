package it.trekkete.ui.views.parti;

import com.google.gson.Gson;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import it.trekkete.data.entity.Trip;
import it.trekkete.data.entity.TripParticipants;
import it.trekkete.data.entity.User;
import it.trekkete.data.entity.UserExtendedData;
import it.trekkete.data.service.*;
import it.trekkete.security.AuthenticatedUser;
import it.trekkete.ui.views.MainLayout;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.PermitAll;
import java.io.ByteArrayInputStream;
import java.util.List;

@PageTitle("Profilo")
@Route(value = "profile", layout = MainLayout.class)
@PermitAll
public class YourTripView extends VerticalLayout {

    private final TripRepository tripRepository;
    private final TripParticipantsRepository tripParticipantsRepository;
    private final AuthenticatedUser authenticatedUser;
    private final TripLocationRepository tripLocationRepository;
    private final LocationRepository locationRepository;
    private final UserRatingRepository userRatingRepository;

    public YourTripView(@Autowired AuthenticatedUser authenticatedUser,
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

        Button review = new Button("Lascia una recensione");
        review.addClickListener(click -> {


        });

        container.add(review);

        return container;
    }
}
