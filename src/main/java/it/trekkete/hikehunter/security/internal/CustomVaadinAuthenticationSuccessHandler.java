package it.trekkete.hikehunter.security.internal;

import com.vaadin.flow.spring.security.VaadinSavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
public class CustomVaadinAuthenticationSuccessHandler extends VaadinSavedRequestAwareAuthenticationSuccessHandler {

    public static final String REDIRECTION_TARGET_ATTRIBUTE = "target-redirection";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session.getAttribute(REDIRECTION_TARGET_ATTRIBUTE) != null) {
            getRedirectStrategy().sendRedirect(request, response, (String) session.getAttribute(REDIRECTION_TARGET_ATTRIBUTE));
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}