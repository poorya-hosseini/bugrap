package org.vaadin;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView.Content;
import com.vaadin.ui.VerticalLayout;

public class ErrorView extends VerticalLayout implements View {
	private String err;
	
	
	public ErrorView(String err) {
		this.err = err;
		init();
	}

	private void init(){
		Component label = new Label(err);
		label.setSizeFull();
		this.addComponent(label);
	}

	@Override
	public void enter(ViewChangeEvent event) {

	}

}
