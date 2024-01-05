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
import it.trekkete.hikehunter.ui.views.general.SearchView;
import it.trekkete.hikehunter.ui.views.logged.CreateTripView;
import it.trekkete.hikehunter.ui.views.logged.ProfileView;
import org.apache.catalina.webresources.FileResource;

import java.io.ByteArrayInputStream;
import java.io.File;
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
                    LumoUtility.Padding.Horizontal.LARGE,
                    LumoUtility.TextColor.SECONDARY);

            setRoute(view);

            add(icon.create());
            getStyle().set("text-decoration", "none");
            getElement().setAttribute("aria-label", viewName);
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
                .set("object-fit", "contain")
                .set("margin", "var(--lumo-space-m) var(--lumo-space-l)");

        addToNavbar(clientLogo);
        addToNavbar(true, createNavigation());
    }

    private HorizontalLayout createNavigation() {

        HorizontalLayout navigation = new HorizontalLayout();
        navigation.addClassNames(
                LumoUtility.JustifyContent.EVENLY,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.Gap.SMALL,
                LumoUtility.Height.LARGE,
                LumoUtility.Width.FULL);

        for (MenuItemInfo menuItem : createMenuItems()) {
            if (accessChecker.hasAccess(menuItem.getView())) {
                navigation.add(menuItem);
            }
        }

        return navigation;
    }

    private Footer createFooter() {

        Footer footer = new Footer();

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            UserExtendedData userExtendedData = new Gson().fromJson(user.getExtendedData(), UserExtendedData.class);

            Avatar avatar = new Avatar();

            if (userExtendedData != null) {
                if (userExtendedData.getName() != null) {
                    avatar.setName(userExtendedData.getName());
                }

                if (userExtendedData.getProfilePicture() != null) {
                    StreamResource resource = new StreamResource("profile-pic",
                            () -> new ByteArrayInputStream(userExtendedData.getProfilePicture()));
                    avatar.setImageResource(resource);
                }
            }
            else {
                avatar.setName(user.getUsername());
            }

            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            if (userExtendedData != null && userExtendedData.getName() != null)
                div.add(userExtendedData.getName());
            else
                div.add(user.getUsername());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("Il tuo profilo", e -> {
                UI.getCurrent().navigate(ProfileView.class);
            });
            userName.getSubMenu().addItem("Sign out", e -> {
                authenticatedUser.logout();
            });

            userMenu.getStyle().set("margin", "0em 1em");
            footer.add(userMenu);
        } else {
            FlexLayout authDiv = new FlexLayout();
            authDiv.getStyle().set("margin", "0em 1em").set("gap", "1em");
            authDiv.setAlignItems(FlexComponent.Alignment.CENTER);

            Avatar avatar = new Avatar();
            avatar.getElement().setAttribute("tabindex", "-1");

            Anchor loginLink = new Anchor("login", "Accedi");
            authDiv.add(avatar, loginLink);

            footer.add(authDiv);
        }

        return footer;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
    }

    private MenuItemInfo[] createMenuItems() {
        return new MenuItemInfo[]{ //
                new MenuItemInfo("ESPLORA", VaadinIcon.GLOBE, HomeView.class), //
                new MenuItemInfo("CREA", VaadinIcon.LOCATION_ARROW_CIRCLE_O, CreateTripView.class), //
                new MenuItemInfo("CERCA", VaadinIcon.SEARCH, SearchView.class), //
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
