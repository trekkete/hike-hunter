package it.trekkete.hikehunter.ui.views;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import elemental.json.JsonValue;
import it.trekkete.hikehunter.data.entity.User;
import it.trekkete.hikehunter.data.entity.UserExtendedData;
import it.trekkete.hikehunter.security.AuthenticatedUser;
import it.trekkete.hikehunter.ui.views.general.AboutView;
import it.trekkete.hikehunter.ui.views.general.HomeView;
import it.trekkete.hikehunter.ui.views.logged.CreateTripView;
import it.trekkete.hikehunter.ui.views.logged.ProfileView;

import java.io.ByteArrayInputStream;
import java.util.Optional;

@JsModule(value = "./js/geolocation.js")
public class MainLayout extends AppLayout {

    private boolean localized;
    private H2 viewTitle;

    public static class MenuItemInfo extends ListItem {

        private final Class<? extends Component> view;

        public MenuItemInfo(String menuTitle, String iconClass, Class<? extends Component> view) {
            this.view = view;
            RouterLink link = new RouterLink();
            // Use Lumo classnames for various styling
            link.addClassNames("flex", "gap-xs", "items-center", "px-s", "text-body");
            link.getStyle().set("margin", "0em 1em").set("align-items", "baseline");
            link.setRoute(view);

            Span text = new Span(menuTitle);
            // Use Lumo classnames for various styling
            text.addClassNames("whitespace-nowrap");

            link.add(new LineAwesomeIcon(iconClass), text);
            add(link);
        }

        public Class<?> getView() {
            return view;
        }

        @NpmPackage(value = "line-awesome", version = "1.3.0")
        public static class LineAwesomeIcon extends Span {
            public LineAwesomeIcon(String lineawesomeClassnames) {
                // Use Lumo classnames for suitable font styling
                addClassNames("text-secondary");
                if (!lineawesomeClassnames.isEmpty()) {
                    addClassNames(lineawesomeClassnames);
                }
            }
        }

    }

    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        createHeaderContent();
        createDrawerContent();

        //getElement().setAttribute("theme", "hike-hunter");
        setPrimarySection(Section.DRAWER);
    }

    private void createHeaderContent() {

        DrawerToggle toggle = new DrawerToggle();

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void createDrawerContent() {

        Scroller scroller = new Scroller();
        if (!isLocalized()) {
            getElement().executeJs("window.trekkete.getLocation();");
        }

        H2 appName = new H2("hike-hunter");
        appName.getStyle().set("white-space", "nowrap");
        appName.addClassNames("my-m", "me-auto");

        Header header = new Header(appName);
        header.getStyle().set("text-align", "center");

        Nav nav = new Nav();

        // Wrap the links in a list; improves accessibility
        UnorderedList list = new UnorderedList();
        list.addClassNames("gap-s", "list-none", "m-0", "p-0");
        nav.add(list);

        for (MenuItemInfo menuItem : createMenuItems()) {
            if (accessChecker.hasAccess(menuItem.getView())) {
                list.add(menuItem);
            }
        }

        scroller.setContent(nav);
        scroller.setHeightFull();

        addToDrawer(header, scroller, createFooter());
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

            avatar.setThemeName("xsmall");
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
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }

    private MenuItemInfo[] createMenuItems() {
        return new MenuItemInfo[]{ //
                new MenuItemInfo("esplora", "la la-globe", HomeView.class), //
                new MenuItemInfo("crea un'escursione", "la la-map-marker", CreateTripView.class), //
                new MenuItemInfo("come funziona?", "la la-question", AboutView.class), //
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
