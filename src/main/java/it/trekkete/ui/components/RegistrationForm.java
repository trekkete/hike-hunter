package it.trekkete.ui.components;

import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import java.util.stream.Stream;

public class RegistrationForm extends FormLayout {

    private H3 title;

    private TextField firstName;
    private TextField lastName;

    private EmailField email;
    private TextField phoneNumber;

    private PasswordField password;
    private PasswordField passwordConfirm;

    private Span errorMessageField;

    private Button submitButton;


    public RegistrationForm() {
        title = new H3("Form di registrazione");
        firstName = new TextField("Nome");
        lastName = new TextField("Cognome");
        email = new EmailField("Email");
        phoneNumber = new TextField("Telefono");

        password = new PasswordField("Password");
        passwordConfirm = new PasswordField("Conferma password");

        setRequiredIndicatorVisible(firstName, lastName, email, phoneNumber, password,
                passwordConfirm);

        errorMessageField = new Span();

        submitButton = new Button("Inizia l'avventura!");
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.getStyle().set("margin-top", "1.5em");

        add(title, firstName, lastName, email, phoneNumber, password,
                passwordConfirm, errorMessageField,
                submitButton);

        setResponsiveSteps(
                new ResponsiveStep("0", 1, ResponsiveStep.LabelsPosition.TOP),
                new ResponsiveStep("490px", 2, ResponsiveStep.LabelsPosition.TOP));

        // These components always take full width
        setColspan(title, 2);
        setColspan(email, 2);
        setColspan(phoneNumber, 2);
        setColspan(errorMessageField, 2);
        setColspan(submitButton, 2);
    }

    public H3 getTitle() {
        return title;
    }

    public TextField getFirstName() {
        return firstName;
    }

    public TextField getLastName() {
        return lastName;
    }

    public EmailField getEmail() {
        return email;
    }

    public TextField getPhoneNumber() {
        return phoneNumber;
    }

    public PasswordField getPasswordField() { return password; }

    public PasswordField getPasswordConfirmField() { return passwordConfirm; }

    public Span getErrorMessageField() { return errorMessageField; }

    public Button getSubmitButton() { return submitButton; }

    private void setRequiredIndicatorVisible(HasValueAndElement<?, ?>... components) {
        Stream.of(components).forEach(comp -> comp.setRequiredIndicatorVisible(true));
    }

}