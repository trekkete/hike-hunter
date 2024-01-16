package it.trekkete.hikehunter.ui.views;

import com.google.gson.Gson;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;
import elemental.json.JsonValue;
import it.trekkete.hikehunter.data.entity.User;
import it.trekkete.hikehunter.data.entity.UserExtendedData;
import it.trekkete.hikehunter.security.AuthenticatedUser;
import it.trekkete.hikehunter.ui.views.general.HomeView;
import it.trekkete.hikehunter.ui.views.general.MapView;
import it.trekkete.hikehunter.ui.views.general.SearchView;
import it.trekkete.hikehunter.ui.views.logged.CreateTripView;
import it.trekkete.hikehunter.ui.views.logged.ProfileView;
import it.trekkete.hikehunter.ui.views.login.LoginView;
import it.trekkete.hikehunter.utils.FileUtils;
import org.apache.catalina.webresources.FileResource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

@JsModule(value = "./js/geolocation.js")
public class MainLayout extends AppLayout {

    private boolean localized;

    public static class MenuItemInfo extends RouterLink {

        private final Class<? extends Component> view;

        public MenuItemInfo(String viewName, VaadinIcon icon, Class<? extends Component> view) {
            this.view = view;

            addClassNames(LumoUtility.Display.FLEX,
                    LumoUtility.AlignItems.CENTER,
                    LumoUtility.JustifyContent.CENTER,
                    LumoUtility.Width.FULL,
                    LumoUtility.Padding.Horizontal.MEDIUM,
                    LumoUtility.TextColor.SECONDARY);

            setRoute(view);

            add(icon.create());
            getStyle().set("text-decoration", "none");
            getElement().setAttribute("aria-label", viewName);
        }

        public MenuItemInfo(Avatar avatar, Class<? extends Component> view) {
            this.view = view;

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

        public Class<?> getView() {
            return view;
        }

    }

    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;

    protected Image clientLogo;
    protected com.vaadin.flow.component.html.Section section;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        //UI.getCurrent().getPage().addJsModule("https://unpkg.com/leaflet-overpass-layer@2.9.0/dist/OverPassLayer.bundle.js");
        //UI.getCurrent().getPage().addStyleSheet("https://unpkg.com/leaflet-overpass-layer@2.9.0/dist/OverPassLayer.css");

        if (!isLocalized()) {
            getElement().executeJs("window.trekkete.getLocation();");
        }

        createHeaderContent();

        setPrimarySection(Section.NAVBAR);
    }

    private void createHeaderContent() {

        clientLogo = new Image("images/default-logo.png", "hike-hunter");
        clientLogo.setWidthFull();
        clientLogo.setHeight("40px");
        clientLogo.getStyle()
                .set("object-fit", "contain");

        clientLogo.addClassNames(
                LumoUtility.Margin.Vertical.XSMALL,
                LumoUtility.Margin.Horizontal.LARGE);

        addToNavbar(clientLogo);
        addToNavbar(true, createNavigation());
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
            if (accessChecker.hasAccess(menuItem.getView())) {
                navigation.add(menuItem);
            }
            else {

                Span filler = new Span();
                filler.addClassNames(
                        LumoUtility.Width.FULL,
                        LumoUtility.Padding.Horizontal.LARGE);

                navigation.add(filler);
            }
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

            navigation.add(new MenuItemInfo(avatar, ProfileView.class));
        }
        else {
            navigation.add(new MenuItemInfo(avatar, LoginView.class));
        }

        return navigation;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
    }

    private MenuItemInfo[] createMenuItems() {
        return new MenuItemInfo[]{ //
                new MenuItemInfo("ESPLORA", VaadinIcon.GLOBE, HomeView.class), //
                new MenuItemInfo("MAPPA", VaadinIcon.MAP_MARKER, MapView.class), //
                new MenuItemInfo("CERCA", VaadinIcon.SEARCH, SearchView.class), //
                new MenuItemInfo("CREA", VaadinIcon.LOCATION_ARROW_CIRCLE_O, CreateTripView.class), //
                //new MenuItemInfo("PROFILO", VaadinIcon.USER, ProfileView.class), //
        };
    }

    public static MainLayout getCurrentLayout() {

        if (UI.getCurrent().getChildren().anyMatch(component -> component.getClass() == MainLayout.class)) {
            return (MainLayout) UI.getCurrent().getChildren().filter(component -> component.getClass() == MainLayout.class).findFirst().get();
        } else {
            return null;
        }
    }

    public boolean isLocalized() {

        getElement().executeJs("return window.trekkete.coords;")
                .then(JsonValue.class, result -> {
                    setLocalized(result != null);
                });

        return localized;
    }

    public void setLocalized(boolean localized) {
        this.localized = localized;
    }
}
