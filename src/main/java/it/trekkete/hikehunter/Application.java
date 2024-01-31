package it.trekkete.hikehunter;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication(scanBasePackages = "it.trekkete.hikehunter")
@NpmPackage(value = "@fontsource/nunito", version = "4.5.0")
@Theme(value = "hike-hunter", variant = Lumo.LIGHT)
@PWA(name = "hike-hunter", shortName = "hike-hunter", offlineResources = {})
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
