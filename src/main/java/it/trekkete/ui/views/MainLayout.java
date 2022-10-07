package it.trekkete.ui.views;

import com.google.gson.Gson;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import it.trekkete.data.entity.User;
import it.trekkete.data.entity.UserExtendedData;
import it.trekkete.security.AuthenticatedUser;
import it.trekkete.ui.views.esplora.ComeFunzionaView;
import it.trekkete.ui.views.esplora.EsploraView;
import it.trekkete.ui.views.parti.PartiView;
import it.trekkete.ui.views.parti.YourTripView;

import java.io.ByteArrayInputStream;
import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    /**
     * A simple navigation item component, based on ListItem element.
     */
    public static class MenuItemInfo extends ListItem {

        private final Class<? extends Component> view;

        public MenuItemInfo(String menuTitle, String iconClass, Class<? extends Component> view) {
            this.view = view;
            RouterLink link = new RouterLink();
            // Use Lumo classnames for various styling
            link.addClassNames("flex", "gap-xs", "h-m", "items-center", "px-s", "text-body");
            link.getStyle().set("margin", "1em").set("align-items", "baseline");
            link.setRoute(view);

            Span text = new Span(menuTitle);
            // Use Lumo classnames for various styling
            text.addClassNames("whitespace-nowrap");
            text.getStyle().set("font-size", "1.5em");

            link.add(new LineAwesomeIcon(iconClass), text);
            add(link);
        }

        public Class<?> getView() {
            return view;
        }

        /**
         * Simple wrapper to create icons using LineAwesome iconset. See
         * https://icons8.com/line-awesome
         */
        @NpmPackage(value = "line-awesome", version = "1.3.0")
        public static class LineAwesomeIcon extends Span {
            public LineAwesomeIcon(String lineawesomeClassnames) {
                // Use Lumo classnames for suitable font styling
                addClassNames("text-l", "text-secondary");
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

        addToNavbar(createHeaderContent());

        //getElement().setAttribute("theme", "hike-hunter");
    }

    private Component createHeaderContent() {
        Header header = new Header();
        header.addClassNames("box-border", "flex", "flex-row", "w-full");
        header.getStyle().set("box-shadow", "black 0 0 2em -0.5em");
        header.getStyle().set("align-items", "center");

        Div layout = new Div();
        layout.addClassNames("flex", "items-center", "px-l");

        H1 appName = new H1("hike-hunter");
        appName.getStyle().set("white-space", "nowrap");
        appName.addClassNames("my-m", "me-auto");
        layout.getStyle().set("margin", "0em 1em");
        layout.add(appName);

        Nav nav = new Nav();
        nav.addClassNames("flex", "overflow-auto", "px-m", "py-xs", "w-full");

        // Wrap the links in a list; improves accessibility
        UnorderedList list = new UnorderedList();
        list.addClassNames("flex", "gap-s", "list-none", "m-0", "p-0");
        nav.add(list);

        for (MenuItemInfo menuItem : createMenuItems()) {
            if (accessChecker.hasAccess(menuItem.getView())) {
                list.add(menuItem);
            }

        }

        header.add(layout, nav);

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            UserExtendedData userExtendedData = new Gson().fromJson(user.getExtendedData(), UserExtendedData.class);

            Avatar avatar = new Avatar(userExtendedData.getName());
            if (userExtendedData.getProfilePicture() != null) {
                StreamResource resource = new StreamResource("profile-pic",
                        () -> new ByteArrayInputStream(userExtendedData.getProfilePicture()));
                avatar.setImageResource(resource);
            }
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(userExtendedData.getName());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("Il tuo profilo", e -> {
                UI.getCurrent().navigate(YourTripView.class);
            });
            userName.getSubMenu().addItem("Sign out", e -> {
                authenticatedUser.logout();
            });


            userMenu.getStyle().set("margin", "0em 1em");
            header.add(userMenu);
        } else {
            FlexLayout authDiv = new FlexLayout();
            authDiv.getStyle().set("margin", "0em 1em").set("gap", "1em");
            authDiv.setJustifyContentMode(FlexComponent.JustifyContentMode.EVENLY);
            Anchor registerLink = new Anchor("register", "Registrati");
            Anchor loginLink = new Anchor("login", "Accedi");
            authDiv.add(registerLink, loginLink);

            header.add(authDiv);
        }

        return header;
    }

    private MenuItemInfo[] createMenuItems() {
        return new MenuItemInfo[]{ //
                new MenuItemInfo("esplora", "la la-globe", EsploraView.class), //
                new MenuItemInfo("crea un'escursione", "la la-map-marker", PartiView.class), //
                new MenuItemInfo("come funziona?", "la la-question", ComeFunzionaView.class), //
        };
    }

}
