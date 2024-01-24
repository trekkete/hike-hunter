package it.trekkete.hikehunter.ui.views;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import elemental.json.JsonValue;
import it.trekkete.hikehunter.data.entity.Location;
import it.trekkete.hikehunter.data.entity.User;
import it.trekkete.hikehunter.data.entity.UserExtendedData;
import it.trekkete.hikehunter.security.AuthenticatedUser;
import it.trekkete.hikehunter.ui.views.general.HomeView;
import it.trekkete.hikehunter.ui.views.general.MapView;
import it.trekkete.hikehunter.ui.views.general.SearchView;
import it.trekkete.hikehunter.ui.views.logged.CreateTripView;
import it.trekkete.hikehunter.ui.views.logged.PreferencesView;
import it.trekkete.hikehunter.ui.views.logged.ProfileView;
import it.trekkete.hikehunter.ui.views.login.LoginView;
import it.trekkete.hikehunter.utils.AppEvents;
import it.trekkete.hikehunter.utils.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsModule(value = "./js/geolocation.js")
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private final Logger log = LogManager.getLogger(MainLayout.class);

    private boolean localized;
    private final List<PropertyChangeListener> listeners;
    private final PropertyChangeSupport support;

    private final Location lastKnownUserLocation;

    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;

    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {

        header.removeAll();
        header.add(clientLogo);

        if (afterNavigationEvent.getLocation().getFirstSegment().equals(RouteConfiguration.forSessionScope().getUrl(ProfileView.class))) {
            header.add(preferences);
        }
    }

    public static class MenuItemInfo extends RouterLink {

        private final Logger log = LogManager.getLogger(MenuItemInfo.class);

        private final Class<? extends Component> view;
        private final AuthenticatedUser authenticatedUser;

        public MenuItemInfo(String viewName, Icon icon, Class<? extends Component> view, AuthenticatedUser authenticatedUser) {
            this.view = view;
            this.authenticatedUser = authenticatedUser;

            addClassNames(LumoUtility.Display.FLEX,
                    LumoUtility.AlignItems.CENTER,
                    LumoUtility.JustifyContent.CENTER,
                    LumoUtility.Width.FULL,
                    LumoUtility.Padding.Horizontal.MEDIUM,
                    LumoUtility.TextColor.SECONDARY);

            setRoute(view);

            add(icon);
            getStyle().set("text-decoration", "none");
            getElement().setAttribute("aria-label", viewName);
        }

        public MenuItemInfo(Avatar avatar, Class<? extends Component> view, AuthenticatedUser authenticatedUser) {
            this.view = view;
            this.authenticatedUser = authenticatedUser;

            addClassNames(LumoUtility.Display.FLEX,
                    LumoUtility.AlignItems.CENTER,
                    LumoUtility.JustifyContent.CENTER,
                    LumoUtility.Width.FULL,
                    LumoUtility.Padding.Horizontal.MEDIUM,
                    LumoUtility.TextColor.SECONDARY);

            setRoute(view);

            add(avatar);
            getStyle().set("text-decoration", "none");
            if (avatar.getName() != null)
                getElement().setAttribute("aria-label", avatar.getName());
        }

        @Override
        public void afterNavigation(AfterNavigationEvent event) {

            if (authenticatedUser.get().isPresent())
                return;

            if (view.getSimpleName().equals(CreateTripView.class.getSimpleName())) {
                VaadinSession.getCurrent().getSession().setAttribute(AppEvents.REROUTING_NEW_TRIP, "true");
                log.trace("Saving '{}' in session", AppEvents.REROUTING_NEW_TRIP);
            }
        }

        public Class<?> getView() {
            return view;
        }

    }

    private HorizontalLayout header;
    private RouterLink preferences;
    private Image clientLogo;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;
        this.support = new PropertyChangeSupport(this);
        this.listeners = new ArrayList<>();
        this.lastKnownUserLocation = new Location();
        this.localized = false;

        createHeaderContent();

        setPrimarySection(Section.NAVBAR);
    }

    private void createHeaderContent() {

        addToNavbar(createHeader());
        addToNavbar(true, createNavigation());
    }

    private HorizontalLayout createHeader() {

        header = new HorizontalLayout();
        header.setHeight("40px");
        header.addClassNames(
                LumoUtility.Position.RELATIVE,
                LumoUtility.Width.FULL,
                LumoUtility.AlignItems.CENTER);

        clientLogo = new Image("images/default-logo.png", "hike-hunter");
        clientLogo.setWidthFull();
        clientLogo.setHeight("40px");
        clientLogo.getStyle()
                .set("object-fit", "contain");

        clientLogo.addClassNames(
                LumoUtility.Margin.Vertical.XSMALL,
                LumoUtility.Margin.Horizontal.LARGE);

        preferences = new RouterLink();
        preferences.setRoute(PreferencesView.class);
        preferences.add(FontAwesome.Solid.COG.create());
        preferences.getStyle()
                .set("text-decoration", "none")
                .set("right", "10px");
        preferences.getElement().setAttribute("aria-label", "PREFERENCES");
        preferences.addClassNames(
                LumoUtility.Position.ABSOLUTE,
                LumoUtility.Display.FLEX,
                LumoUtility.TextColor.SECONDARY);

        header.add(clientLogo);

        return header;

    }

    private HorizontalLayout createNavigation() {

        HorizontalLayout navigation = new HorizontalLayout();
        navigation.setHeight("40px");
        navigation.addClassNames(
                LumoUtility.Margin.Vertical.XSMALL,
                LumoUtility.Margin.Horizontal.LARGE,
                LumoUtility.JustifyContent.START,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.Gap.SMALL,
                LumoUtility.Width.FULL);

        for (MenuItemInfo menuItem : createMenuItems()) {
            navigation.add(menuItem);
        }

        Avatar avatar = new Avatar();
        avatar.setWidth("26px");
        avatar.setHeight("26px");
        avatar.getStyle()
                .set("border", "2px solid #5e6979")
                .set("cursor", "pointer");
        avatar.setImageResource(new StreamResource("auth-pic", () -> getClass().getResourceAsStream("/META-INF/resources/images/user.png")));

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {

            User user = maybeUser.get();
            UserExtendedData userExtendedData = new Gson().fromJson(user.getExtendedData(), UserExtendedData.class);

            if (userExtendedData != null) {
                if (userExtendedData.getName() != null) {
                    avatar.setName(userExtendedData.getName());
                }

                if (userExtendedData.getProfilePicture() != null) {
                    StreamResource resource = new StreamResource("profile-pic",
                            () -> {
                                try {
                                    return new ByteArrayInputStream(FileUtils.loadForUser(userExtendedData.getProfilePicture(), user));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                    avatar.setImageResource(resource);
                }
            }
            else {
                avatar.setName(user.getUsername());
            }

            avatar.getElement().setAttribute("tabindex", "-1");

            navigation.add(new MenuItemInfo(avatar, ProfileView.class, authenticatedUser));
        }
        else {
            navigation.add(new MenuItemInfo(avatar, LoginView.class, authenticatedUser));
        }

        return navigation;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
    }

    private MenuItemInfo[] createMenuItems() {
        return new MenuItemInfo[]{ //
                new MenuItemInfo("ESPLORA", FontAwesome.Solid.HOME.create(), HomeView.class, authenticatedUser), //
                new MenuItemInfo("MAPPA", FontAwesome.Solid.MAP_LOCATION_DOT.create(), MapView.class, authenticatedUser), //
                new MenuItemInfo("CERCA", FontAwesome.Solid.SEARCH.create(), SearchView.class, authenticatedUser), //
                new MenuItemInfo("CREA", FontAwesome.Solid.LOCATION_ARROW.create(), CreateTripView.class, authenticatedUser), //
                //new MenuItemInfo("PROFILO", VaadinIcon.USER, ProfileView.class), //
        };
    }

    public static Optional<MainLayout> getCurrentLayout() {

        if (UI.getCurrent().getChildren().anyMatch(component -> component.getClass() == MainLayout.class)) {
            return Optional.of((MainLayout) UI.getCurrent().getChildren().filter(component -> component.getClass() == MainLayout.class).findFirst().get());
        } else {
            return Optional.empty();
        }
    }

    public boolean isLocalized() {
        return localized;
    }

    public void setLocalized(boolean localized) {
        this.localized = localized;
    }

    private Optional<Location> getUserLocation() {

        if (!isLocalized()) {
            getElement().executeJs("window.trekkete.getLocation();");
        }
        else {
            return Optional.of(lastKnownUserLocation);
        }

        Location userLocation = new Location();
        getElement().executeJs("return window.trekkete.coords;")
            .then(JsonValue.class, result -> {

                log.trace("Location retrieved: {}", result);

                if (result == null) {
                    setLocalized(false);
                    return;
                }

                JsonObject object = new Gson().fromJson(result.toJson(), JsonObject.class);

                setLocalized(true);

                userLocation.setLatitude(object.get("lat").getAsDouble());
                userLocation.setLongitude(object.get("lon").getAsDouble());

                if (lastKnownUserLocation.getLatitude() != null &&
                    lastKnownUserLocation.getLongitude() != null &&
                    lastKnownUserLocation.equals(userLocation))
                    return;

                lastKnownUserLocation.setLatitude(userLocation.getLatitude());
                lastKnownUserLocation.setLongitude(userLocation.getLongitude());

                log.trace("Firing location update event for location '{}' to {} listeners", userLocation, listeners.size());

                support.firePropertyChange(AppEvents.LOCATION_UPDATE, null, userLocation);
            });

        return Optional.empty();
    }

    public void addChangeListener(PropertyChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            support.addPropertyChangeListener(listener);
        }
    }

    public void removeChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
        listeners.remove(listener);
    }

    public static Optional<Location> triggerUserLocation() {
        Optional<MainLayout> mainLayout = MainLayout.getCurrentLayout();

        if (mainLayout.isPresent()) {
            return mainLayout.get().getUserLocation();
        }

        return Optional.empty();
    }

}
