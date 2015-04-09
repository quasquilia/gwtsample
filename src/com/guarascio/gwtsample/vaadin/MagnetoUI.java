package com.guarascio.gwtsample.vaadin;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.guarascio.gwtsample.dao.Greeting;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("valo")
public class MagnetoUI extends UI {

	@Override
	protected void init(VaadinRequest request) {
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		setContent(layout);
				
		final TextField text = new TextField("Add a comment");
		layout.addComponent(text);
		Button button = new Button("Submit");
		button.addClickListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				UserService userService = UserServiceFactory.getUserService();
				User user = userService.getCurrentUser();
				
				Greeting greeting = new Greeting();
				greeting.setUserName(user.getNickname());
				
				greeting.setMessage(text.getValue());
				
//
//				String msg = "Thank you for clicking ";
//				if (user == null) {
//					msg += "<anonymous>";
//				} else {
//					msg += user.getUserId() + " - mail:" + user.getEmail() + ", domain:" + user.getAuthDomain() + ", fed.identity:" + user.getFederatedIdentity() + ", nickname:" + user.getNickname();
//				}
//				
//				layout.addComponent(new Label(msg));
			}
		});
		layout.addComponent(button);
	}

}