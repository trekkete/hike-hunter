package it.trekkete.hikehunter.security.internal;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.*;
import it.trekkete.hikehunter.security.SecurityConfiguration;
import it.trekkete.hikehunter.ui.views.login.LoginView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import static it.trekkete.hikehunter.security.internal.CustomVaadinAuthenticationSuccessHandler.REDIRECTION_TARGET_ATTRIBUTE;

/**
 * Configures Vaadin to work properly with sessions.
 */
@Component
class VaadinSessionConfiguration implements VaadinServiceInitListener, SystemMessagesProvider, SessionDestroyListener {

    private static final Logger logger = LoggerFactory.getLogger(VaadinSessionConfiguration.class);
    private final String relativeSessionExpiredUrl;

    VaadinSessionConfiguration(ServerProperties serverProperties) {
        relativeSessionExpiredUrl = UriComponentsBuilder.fromPath(serverProperties.getServlet().getContextPath()).path("session-expired").build().toUriString();
    }

    @Override
    public SystemMessages getSystemMessages(SystemMessagesInfo systemMessagesInfo) {
        var messages = new CustomizedSystemMessages();
        // Redirect to a specific screen when the session expires. In this particular case we don't want to logout
        // just yet. If you would like the user to be completely logged out when the session expires, this URL
        // should the logout URL.
        messages.setSessionExpiredURL(SecurityConfiguration.LOGOUT_URL);
        return messages;
    }

    @Override
    public void sessionDestroy(SessionDestroyEvent event) {
        try {
            event.getSession().getSession().invalidate();
        } catch (Exception ignore) {}
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            final UI ui = uiEvent.getUI();
            ui.addBeforeEnterListener(this::beforeEnter); // (2)
        });
    }

    private void beforeEnter(BeforeEnterEvent event) {
        if (!LoginView.class.equals(event.getNavigationTarget()) && !SecurityUtils.isUserLoggedIn()) {
            UI.getCurrent().getSession().getSession().setAttribute(REDIRECTION_TARGET_ATTRIBUTE, "/" + event.getLocation().getPathWithQueryParameters());
            //event.rerouteTo(LoginView.class);
        }
    }
}