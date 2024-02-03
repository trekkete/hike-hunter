package it.trekkete.hikehunter.ui.views.logged.preferences;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.google.gson.Gson;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.trekkete.hikehunter.data.entity.User;
import it.trekkete.hikehunter.data.entity.UserExtendedData;
import it.trekkete.hikehunter.security.AuthenticatedUser;
import it.trekkete.hikehunter.ui.views.MainLayout;
import it.trekkete.hikehunter.ui.window.ContactInfoWindow;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.PermitAll;

@PageTitle("Modifica profilo")
@Route(value = "prefs/edit-profile", layout = MainLayout.class)
@PermitAll
public class EditProfileView extends VerticalLayout {

    private final AuthenticatedUser authenticatedUser;
    private final User user;

    public EditProfileView(@Autowired AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;

        user = authenticatedUser.get().get();
    }

    private void constructUI() {

        setPadding(false);
        setSizeFull();
        setAlignItems(Alignment.CENTER);

        VerticalLayout container = new VerticalLayout();
        container.setPadding(false);
        container.setSpacing(false);
        container.setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0px", 1),
                new FormLayout.ResponsiveStep("600px", 2));

        UserExtendedData userExtendedData;
        if (user.getExtendedData() != null)
            userExtendedData = new Gson().fromJson(user.getExtendedData(), UserExtendedData.class);
        else
            userExtendedData = new UserExtendedData();

        TextField firstName = new TextField("Nome");
        if (userExtendedData.getName() != null)
            firstName.setValue(userExtendedData.getName());

        TextField lastName = new TextField("Cognome");
        if (userExtendedData.getSurname() != null)
            lastName.setValue(userExtendedData.getSurname());

        TextField username = new TextField("Username");
        username.setRequired(true);
        username.setPattern("^[A-Za-z][A-Za-z0-9_.]{4,29}$");
        if (user.getUsername() != null)
            username.setValue(user.getUsername());

        EmailField email = new EmailField("Email");
        email.setRequiredIndicatorVisible(true);
        if (userExtendedData.getEmail() != null)
            email.setValue(userExtendedData.getEmail());

        TextField phoneNumber = new TextField("Numero di telefono");
        phoneNumber.setPattern("^([\\+][0-9]{1,3})?[0-9]{10}$");
        phoneNumber.setHelperText("Formato: +39XXXXXXXXXX");
        if (userExtendedData.getPhoneNumber() != null)
            phoneNumber.setValue(userExtendedData.getPhoneNumber());

        formLayout.add(username, firstName, lastName, email, phoneNumber);

        container.add(formLayout);

        add(container);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        constructUI();
    }
}
