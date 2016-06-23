package org.vaadin;

import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.Reporter;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Notification;

public class UserLogin extends LoginFormDesign implements View {
	private BugrapUI bugrapUI;

	public UserLogin(BugrapUI bugrapUI) {

		// TODO Auto-generated constructor stub
		this.bugrapUI = bugrapUI;
		usernameField.focus();
		singInButton.setClickShortcut(KeyCode.ENTER);
		singInButton.addClickListener(e -> authenticate());
		registerButton.addClickListener(e -> registerUser());
		// registerLink.setResource(new
		// ExternalResource("http://localhost:8080/#!userRegister"));
		// recoverPasswordLink.addClickListener(e -> this.passRecover());
	}

	private void authenticate() {

		if (usernameField.getValue() == null || passwordField.getValue() == null) {
			Notification.show("Enter username and password.");
			this.passwordField.setValue(null);
            this.passwordField.focus();
		} else {
			Reporter user = bugrapUI.getRepository().authenticate(usernameField.getValue(), passwordField.getValue());
			if (user == null) {
				Notification.show("username or password is wrong!");

			} else {
				Notification.show("Logon successful.");
				getSession().setAttribute("user", user);
				getUI().getNavigator().navigateTo(BugrapUI.OVERVIEW_VIEW);
				usernameField.clear();
				passwordField.clear();
			}
		}
	}

	private void registerUser() {
		getUI().getNavigator().navigateTo(BugrapUI.USER_REGISTER_VIEW);
	}

	private void passRecover() {
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub\
	}

}
