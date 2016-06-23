package org.vaadin;

import java.util.regex.Pattern;

import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.FacadeUtilTest;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Notification;

public class UserRegister extends UserRegisterDesign implements View {
	private BugrapUI bugrapUI;
	private BugrapRepository repository;

	public UserRegister(BugrapUI bugrapUI) {
		// TODO Auto-generated constructor stub
		this.bugrapUI = bugrapUI;
		this.repository = bugrapUI.getRepository();
		email.selectAll();
		register.setClickShortcut(KeyCode.ENTER);
		register.addClickListener(e -> this.registerUser());
	}

	private void registerUser() {
		if (!Pattern.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$",
				email.getValue())) {
			Notification.show("Entered email is wrong!");
		} else {
			//There is no method in API to check the existence of the user
			
			Notification.show("User " + email.getValue() + " registered succesfully");
			email.clear();
			//navigator.removeView(BugrapUI.USER_REGISTER_VIEW);
			getUI().getNavigator().navigateTo("");
		}
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub
	}

}
