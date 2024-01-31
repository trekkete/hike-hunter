package it.trekkete.hikehunter.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import it.trekkete.hikehunter.ui.views.login.LoginView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletResponse;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    public final Logger log = LogManager.getLogger(SecurityConfiguration.class);

    public static final String LOGOUT_URL = "/";
    public static final String LOGIN_URL = "/signin";
    public static final String LOGIN_PROCESSING_URL = "/profile";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        super.configure(http);
        setLoginView(http, LoginView.class, LOGOUT_URL);

        http.formLogin()
                .loginPage(LOGIN_URL).permitAll()
                .loginProcessingUrl(LOGIN_PROCESSING_URL)
                .failureHandler((request, response, exception) -> {
                    if (exception instanceof BadCredentialsException) {
                        final String username = request.getParameter("username");
                        log.warn("User insert bad credential for username: \"{}\", unable to log in.", username);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.sendRedirect("./signin?errorMessage=credentialserror");
                    } else {
                        log.info(exception.getMessage());
                    }
                });
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
        web.ignoring().antMatchers("/images/*.png");
    }
}
