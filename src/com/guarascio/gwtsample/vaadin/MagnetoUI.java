package com.guarascio.gwtsample.vaadin;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.guarascio.gwtsample.EMF;
import com.guarascio.gwtsample.captions.impl.Translator;
import com.guarascio.gwtsample.dao.Greeting;
import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.provider.CachingMutableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("magneto")
public class MagnetoUI extends UI implements Button.ClickListener {
	
	private EntityManager em = new EntityManagerProxy();
	private TextField text = null;
	private Grid grid;
	private JPAContainer<Greeting> container;
	
	private Translator translator = new Translator();
	
	@Override
	protected void init(VaadinRequest request) {
		
		String lang = request.getParameter("lang");		
		translator.init(lang != null ? Locale.forLanguageTag(lang) : null);
		
		final VerticalLayout layout = new VerticalLayout();
		
		layout.setMargin(true);
		setContent(layout);
		
		UserService userService = UserServiceFactory.getUserService();		
		String logoutURL = userService.createLogoutURL(request.getParameter("v-loc"));		
		GridLayout l = new GridLayout(1, 1);				
		Link link = new Link(translator.getCaption("logout"), new ExternalResource(logoutURL));		
		l.addComponent(link);
		l.setComponentAlignment(link, Alignment.TOP_RIGHT);
		layout.addComponent(l);
		
		HorizontalLayout buttons = new HorizontalLayout();
		final Button add = new Button(translator.getCaption("crud.add"), new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				
				if(!grid.isEditorEnabled() || grid.isEditorActive()) {
					return;
				}
				
				UserService userService = UserServiceFactory.getUserService();
				User user = userService.getCurrentUser();
				
				String userName = user.getNickname();				
				Object id = grid.addRow("", userName, new Date());
				grid.editItem(id);				
			}
		});
		buttons.addComponent(add);

		Button cancel = new Button(translator.getCaption("crud.cancel"), new Button.ClickListener() {			
			@Override
			public void buttonClick(ClickEvent event) {
				grid.cancelEditor();
			}
		});		
		buttons.addComponent(cancel);
		
		layout.addComponent(buttons);
		
		grid = new Grid();
		grid.setEditorSaveCaption(translator.getCaption("crud.save"));
		grid.setEditorCancelCaption(translator.getCaption("crud.cancel"));
		grid.setSelectionMode(SelectionMode.MULTI);
		
		container = new JPAContainer<Greeting>(Greeting.class);		
//		MutableLocalEntityProvider<Greeting> entityProvider = new MutableLocalEntityProvider<Greeting>(Greeting.class, em);
		CachingMutableLocalEntityProvider<Greeting> entityProvider =
			    new CachingMutableLocalEntityProvider<Greeting>(Greeting.class, em);
		
		container.setEntityProvider(entityProvider);
//		final Container.Indexed filteredColumnsContainer = container;
		final GeneratedPropertyContainer filteredColumnsContainer = new GeneratedPropertyContainer(container);				
		for (Object propId : filteredColumnsContainer.getContainerPropertyIds()) {
			filteredColumnsContainer.removeContainerProperty(propId);
		} 
		filteredColumnsContainer.addContainerProperty("date", Date.class, null);
		filteredColumnsContainer.addContainerProperty("userName", String.class, "");
		filteredColumnsContainer.addContainerProperty("message", String.class, "");		
		Button delete = new Button(translator.getCaption("crud.delete"), new Button.ClickListener() {			
			@Override
			public void buttonClick(ClickEvent event) {
				for(Object id : grid.getSelectedRows()) {
					if (id != null) {
						grid.deselect(id);
						filteredColumnsContainer.removeItem(id);
					}
				}
			}			
		});		
		buttons.addComponent(delete);

		
		grid.setContainerDataSource(filteredColumnsContainer);
		grid.setSizeFull();
		grid.setEditorEnabled(true);
		layout.addComponent(grid);
//		grid.setWidth("100%");
//		grid.setHeight("400px");
//		grid.sort("date");
//		grid.setEditorEnabled(true);
//
//		text = new TextField("Add a comment");
//		
//		layout.addComponent(text);
//		Button button = new Button("Submit");
//		button.addClickListener(this);
//		layout.addComponent(button);
	}
	
	public void buttonClick(ClickEvent event) {
		
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		
		Greeting greeting = new Greeting();
		greeting.setUserName(user.getNickname());
		
		greeting.setMessage(text.getValue());
		
		try {			
			em.getTransaction().begin();
			em.persist(greeting);
			em.refresh(greeting);	
			em.getTransaction().commit();
			
	    } finally {			    	
	        //em.close();			
	        container.refresh();
	        grid.scrollToEnd();
	    }		
	}
	
	private String getList() {
		StringBuffer sb = new StringBuffer();
		EntityManager em = EMF.get().createEntityManager();
		try {
	        // ... do stuff with em ...
			Query query = em.createQuery("SELECT e FROM Greeting e");
			Collection<Greeting> elems =  (Collection<Greeting>) query.getResultList();
			
			for (Greeting g : elems) {
				sb.append(g).append("\n");
			}
	    } finally {
	        em.close();				    
	    }
		return sb.toString();
	}
	
	private static class EntityManagerProxy implements EntityManager, Serializable {
		private transient EntityManager em = null;
		
		private EntityManager getEM() {
			if (em == null) {
				em = EMF.get().createEntityManager();
			}
			return em;	
		}

		public void clear() {
			getEM().clear();
		}

		public void close() {
			getEM().close();
			em = null;
		}

		public boolean contains(Object arg0) {
			return getEM().contains(arg0);
		}

		public <T> TypedQuery<T> createNamedQuery(String arg0, Class<T> arg1) {
			return getEM().createNamedQuery(arg0, arg1);
		}

		public Query createNamedQuery(String arg0) {
			return getEM().createNamedQuery(arg0);
		}

		public Query createNativeQuery(String arg0, Class arg1) {
			return getEM().createNativeQuery(arg0, arg1);
		}

		public Query createNativeQuery(String arg0, String arg1) {
			return getEM().createNativeQuery(arg0, arg1);
		}

		public Query createNativeQuery(String arg0) {
			return getEM().createNativeQuery(arg0);
		}

		public <T> TypedQuery<T> createQuery(CriteriaQuery<T> arg0) {
			return getEM().createQuery(arg0);
		}

		public <T> TypedQuery<T> createQuery(String arg0, Class<T> arg1) {
			return getEM().createQuery(arg0, arg1);
		}

		public Query createQuery(String arg0) {
			return getEM().createQuery(arg0);
		}

		public void detach(Object arg0) {
			getEM().detach(arg0);
		}

		public <T> T find(Class<T> arg0, Object arg1, LockModeType arg2,
				Map<String, Object> arg3) {
			return getEM().find(arg0, arg1, arg2, arg3);
		}

		public <T> T find(Class<T> arg0, Object arg1, LockModeType arg2) {
			return getEM().find(arg0, arg1, arg2);
		}

		public <T> T find(Class<T> arg0, Object arg1, Map<String, Object> arg2) {
			return getEM().find(arg0, arg1, arg2);
		}

		public <T> T find(Class<T> arg0, Object arg1) {
			return getEM().find(arg0, arg1);
		}

		public void flush() {
			getEM().flush();
		}

		public CriteriaBuilder getCriteriaBuilder() {
			return getEM().getCriteriaBuilder();
		}

		public Object getDelegate() {
			return getEM().getDelegate();
		}

		public EntityManagerFactory getEntityManagerFactory() {
			return getEM().getEntityManagerFactory();
		}

		public FlushModeType getFlushMode() {
			return getEM().getFlushMode();
		}

		public LockModeType getLockMode(Object arg0) {
			return getEM().getLockMode(arg0);
		}

		public Metamodel getMetamodel() {
			return getEM().getMetamodel();
		}

		public Map<String, Object> getProperties() {
			return getEM().getProperties();
		}

		public <T> T getReference(Class<T> arg0, Object arg1) {
			return getEM().getReference(arg0, arg1);
		}

		public EntityTransaction getTransaction() {
			return getEM().getTransaction();
		}

		public boolean isOpen() {
			return getEM().isOpen();
		}

		public void joinTransaction() {
			getEM().joinTransaction();
		}

		public void lock(Object arg0, LockModeType arg1,
				Map<String, Object> arg2) {
			getEM().lock(arg0, arg1, arg2);
		}

		public void lock(Object arg0, LockModeType arg1) {
			getEM().lock(arg0, arg1);
		}

		public <T> T merge(T arg0) {
			return getEM().merge(arg0);
		}

		public void persist(Object arg0) {
			getEM().persist(arg0);
		}

		public void refresh(Object arg0, LockModeType arg1,
				Map<String, Object> arg2) {
			getEM().refresh(arg0, arg1, arg2);
		}

		public void refresh(Object arg0, LockModeType arg1) {
			getEM().refresh(arg0, arg1);
		}

		public void refresh(Object arg0, Map<String, Object> arg1) {
			getEM().refresh(arg0, arg1);
		}

		public void refresh(Object arg0) {
			getEM().refresh(arg0);
		}

		public void remove(Object arg0) {
			getEM().remove(arg0);
		}

		public void setFlushMode(FlushModeType arg0) {
			getEM().setFlushMode(arg0);
		}

		public void setProperty(String arg0, Object arg1) {
			getEM().setProperty(arg0, arg1);
		}

		public <T> T unwrap(Class<T> arg0) {
			return getEM().unwrap(arg0);
		}
		
		
		
	}

}