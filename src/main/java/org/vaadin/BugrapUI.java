package org.vaadin;

import javax.servlet.annotation.WebServlet;

import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.Reporter;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

/**
 * This UI is the application entry point. A UI may either represent a browser
 * window (or tab) or some part of a html page where a Vaadin application is
 * embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is
 * intended to be overridden to add component to the user interface and
 * initialize non-component functionality.
 */
@Theme("bugrap")
@Widgetset("org.vaadin.MyAppWidgetset")
public class BugrapUI extends UI {
	public static final String USER_REGISTER_VIEW = "userRegister";
	public static final String OVERVIEW_VIEW = "Overview";
	public static final String LOGIN_VIEW = "";

	/**
	 * Backend access point
	 */
	private BugrapRepository repository = new BugrapRepository("bugrappooria");
	// ^^^ You probably would like to use "/var/tmp/bugrap" on OSX/Linux ;)

	// navigation in views
	Navigator navigator;

	@Override
	protected void init(VaadinRequest request) {
		// TODO implement BugRap. Good luck :)
		
		// initialize backend
		// repo.populateWithTestData();

		// Create a navigator to control the views
		navigator = new Navigator(this, this);

		// View registration
		navigator.addView(LOGIN_VIEW, new UserLogin(this));
		navigator.addView(USER_REGISTER_VIEW, new UserRegister(this));
		navigator.addView(OVERVIEW_VIEW,new Overview(this));


		// Create the Error View
		navigator.setErrorView(new ErrorView("There is no such address."));
	}

	protected BugrapRepository getRepository(){
		return repository;
	}
	
	@WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = BugrapUI.class, productionMode = false)
	public static class BugrapServlet extends VaadinServlet {
	}
}
