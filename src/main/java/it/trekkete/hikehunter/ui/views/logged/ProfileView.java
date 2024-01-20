package it.trekkete.hikehunter.ui.views.logged;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.google.gson.Gson;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.jfancy.StarsRating;
import it.trekkete.hikehunter.data.entity.*;
import it.trekkete.hikehunter.data.service.*;
import it.trekkete.hikehunter.security.AuthenticatedUser;
import it.trekkete.hikehunter.ui.views.MainLayout;
import it.trekkete.hikehunter.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.PermitAll;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
    private final UserRepository userRepository;

    private VerticalLayout tabContent;

    private ProgressBar levelProgressBar;
    private H3 level;
    private H5 userLevelLabel;

    public ProfileView(@Autowired AuthenticatedUser authenticatedUser,
                       @Autowired TripRepository tripRepository,
                       @Autowired TripParticipantsRepository tripParticipantsRepository,
                       @Autowired TripLocationRepository tripLocationRepository,
                       @Autowired LocationRepository locationRepository,
                       @Autowired UserRatingRepository userRatingRepository,
                       @Autowired UserRepository userRepository) {
        this.authenticatedUser = authenticatedUser;
        this.tripRepository = tripRepository;
        this.tripParticipantsRepository = tripParticipantsRepository;
        this.tripLocationRepository = tripLocationRepository;
        this.locationRepository = locationRepository;
        this.userRatingRepository = userRatingRepository;
        this.userRepository = userRepository;

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

        List<Trip> bookedTrips = new ArrayList<>();
        List<Trip> completedTrips = new ArrayList<>();
        List<Trip> confirmedTrips = new ArrayList<>();

        for (Trip trip : trips) {
            if (trip.getStartDate() > System.currentTimeMillis() / 1000) {
                bookedTrips.add(trip);
            }
            else if (trip.getEndDate() < System.currentTimeMillis() / 1000) {
                List<UserRating> ratings = userRatingRepository.findAllByAboutAndTrip(user.getId(), trip.getId());

                if (ratings != null && !ratings.isEmpty()) {
                    confirmedTrips.add(trip);
                }
                else {
                    completedTrips.add(trip);
                }
            }
        }

        for(Trip value : confirmedTrips) {
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
        if (!validExtendedData)
            userExtendedData = new UserExtendedData();

        H3 name = new H3(user.getUsername());
        if (validExtendedData && userExtendedData.getName() != null && userExtendedData.getSurname() != null){
            name.setText(userExtendedData.getName() + " " + userExtendedData.getSurname());
        }

        name.getStyle().set("margin", "0");

        H5 levelLabel = new H5("Livello");
        levelLabel.getStyle().set("color", "gray");
        levelLabel.getStyle().set("margin", "0");
        Integer currentLevel = user.getLevel(xp);
        level = new H3(String.valueOf(currentLevel));
        level.getStyle().set("margin", "0");

        HorizontalLayout nameLayout = new HorizontalLayout(name);
        nameLayout.addClassNames(
                LumoUtility.Width.FULL,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.JustifyContent.CENTER
        );

        HorizontalLayout levelLayout = new HorizontalLayout(levelLabel, level);
        levelLayout.setAlignItems(Alignment.BASELINE);

        userLevelLabel = new H5(user.getLevelLabel(currentLevel));
        userLevelLabel.getStyle().set("color", "white").set("top", "10px").set("text-shadow", "0px 0px 0.5em gray");
        userLevelLabel.addClassNames(
                LumoUtility.Position.ABSOLUTE,
                LumoUtility.Margin.NONE);

        levelProgressBar = new ProgressBar();
        levelProgressBar.setMin(currentLevel == 1 ? 0 : user.getMaxXp(currentLevel - 1));
        levelProgressBar.setMax(user.getMaxXp(currentLevel));
        levelProgressBar.setValue(xp);
        levelProgressBar.addThemeVariants(ProgressBarVariant.LUMO_SUCCESS);
        levelProgressBar.setHeight("1.5em");

        VerticalLayout progressBarLayout = new VerticalLayout(userLevelLabel, levelProgressBar);
        progressBarLayout.setSpacing(false);
        progressBarLayout.setPadding(false);
        progressBarLayout.setWidth("90%");
        progressBarLayout.setAlignItems(Alignment.CENTER);
        progressBarLayout.addClassNames(LumoUtility.Position.RELATIVE);

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
            UserExtendedData finalUserExtendedData = userExtendedData;
            src.setSrc(new StreamResource("profile-picture", () -> {
                try {
                    return new ByteArrayInputStream(FileUtils.loadForUser(finalUserExtendedData.getProfilePicture(), user));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        else {
            src.setSrc("images/user.png");
        }

        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg","image/jpg", "image/png", "image/gif");
        upload.setDropAllowed(false);
        upload.setUploadButton(src);
        upload.setWidth("70px");
        upload.setHeight("70px");
        upload.getStyle().set("border-radius", "50%");
        upload.addClassNames(
                LumoUtility.Display.FLEX,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.JustifyContent.CENTER,
                LumoUtility.Overflow.HIDDEN
        );

        image.add(upload);

        UserExtendedData finalUserExtendedData1 = userExtendedData;
        upload.addSucceededListener(event -> {
            String attachmentName = event.getFileName();
            try {
                // The image can be jpg png or gif, but we store it always as png file in this example
                BufferedImage inputImage = ImageIO.read(buffer.getInputStream(attachmentName));
                ByteArrayOutputStream pngContent = new ByteArrayOutputStream();
                ImageIO.write(inputImage, "png", pngContent);

                src.setSrc(new StreamResource("team", () -> new ByteArrayInputStream(pngContent.toByteArray())));

                String filename = FileUtils.saveForUser(pngContent.toByteArray(), ".png", user, finalUserExtendedData1.getProfilePicture());

                finalUserExtendedData1.setProfilePicture(filename);

                user.setExtendedData(new Gson().toJson(finalUserExtendedData1));
                userRepository.save(user);

                upload.clearFileList();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        container.add(header);

        tabContent = new VerticalLayout();
        tabContent.setWidth("97%");
        tabContent.getStyle()
                .set("background", "linear-gradient(#fff, #fff) 50% 50%/calc(100% - 2px) calc(100% - 2px) no-repeat, linear-gradient(0deg, transparent 0%, #eee 100%)")
                .set("margin-top", "-1em");

        Tab completedTab = new Tab(new Span(FontAwesome.Solid.HOURGLASS_HALF.create()), createBadge(completedTrips.size()));
        Tab confirmedTab = new Tab(new Span(FontAwesome.Solid.CHECK.create()), createBadge(confirmedTrips.size()));
        Tab bookedTab = new Tab(new Span(FontAwesome.Solid.CALENDAR_DAY.create()), createBadge(bookedTrips.size()));
        bookedTab.getStyle()
                .set("border-right", "1px solid #eee")
                .set("justify-content", "space-evenly");
        completedTab.getStyle()
                .set("border-right", "1px solid #eee")
                .set("justify-content", "space-evenly");
        confirmedTab.getStyle()
                .set("justify-content", "space-evenly");

        Tabs tabs = new Tabs(bookedTab, completedTab, confirmedTab);
        tabs.addThemeVariants(TabsVariant.LUMO_EQUAL_WIDTH_TABS, TabsVariant.LUMO_SMALL);
        tabs.setWidth("97%");
        tabs.getStyle()
                .set("border", "1px solid #eee")
                .set("border-bottom", "0")
                .set("border-radius", "var(--lumo-border-radius) var(--lumo-border-radius) 0 0")
                .set("box-sizing", "border-box");

        tabs.addSelectedChangeListener(event -> {
            tabContent.removeAll();

            if (event.getSelectedTab().equals(completedTab)) {
                for (Trip completed : completedTrips) {
                    tabContent.add(createTripLayout(completed, true));
                }
            }
            else if (event.getSelectedTab().equals(confirmedTab)) {
                for (Trip confirmed : confirmedTrips) {
                    tabContent.add(createTripLayout(confirmed, true));
                }
            }
            else {
                for (Trip booked : bookedTrips) {
                    tabContent.add(createTripLayout(booked, false));
                }
            }
        });

        for (Trip booked : bookedTrips) {
            tabContent.add(createTripLayout(booked, false));
        }

        add(tabs);
        add(tabContent);
    }

    private Component createTripLayout(Trip trip, boolean showRating) {

        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setPadding(false);
        container.setSpacing(false);
        container.setAlignItems(Alignment.CENTER);
        container.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H4 title = new H4(trip.getTitle());
        title.addClassNames(LumoUtility.Margin.NONE);

        container.add(title);

        Button chat = new Button(FontAwesome.Solid.MESSAGE.create());
        chat.addClickListener(click -> {
            UI.getCurrent().navigate(ChatView.class, new RouteParameters("tripId", String.valueOf(trip.getId())));
        });

        Button review = new Button(FontAwesome.Solid.RANKING_STAR.create());
        review.addClickListener(click -> {

            UUID from = authenticatedUser.get().get().getId();

            Dialog dialog = new Dialog();

            dialog.setHeaderTitle("Lascia una recensione dei tuoi compagni");

            H5 preps = new H5("Preparazione");

            StarsRating prepsStar = new StarsRating();
            prepsStar.setNumstars(5);

            H5 skill = new H5("Abilità");

            StarsRating skillStar = new StarsRating();
            skillStar.setNumstars(5);

            H5 sociability = new H5("Socialità");

            StarsRating socStar = new StarsRating();
            socStar.setNumstars(5);

            List<UserRating> past = userRatingRepository.findAllByFromAndTrip(from, trip.getId());
            if (past != null && !past.isEmpty()) {
                UserRating rating = past.get(0);
                prepsStar.setValue(rating.getPreparation());
                skillStar.setValue(rating.getSkill());
                socStar.setValue(rating.getSociability());
            }

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

        HorizontalLayout buttons = new HorizontalLayout(chat);
        if (showRating)
            buttons.add(review);

        container.add(buttons);

        return container;
    }

    private Span createBadge(int value) {
        Span badge = new Span(String.valueOf(value));
        badge.getElement().getThemeList().add("badge small contrast");
        badge.getStyle().set("margin-inline-start", "var(--lumo-space-xs)");
        return badge;
    }
}
