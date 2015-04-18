package com.guarascio.gwtsample.captions.impl;

import java.io.Serializable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Translator implements Serializable {
	
	private final String BASE_CAPTION_FILE = "com.guarascio.gwtsample.captions";
	
	private transient ResourceBundle bundle;
	private Locale locale;
	
	public void init(Locale locale) {
		this.locale = locale;
		if (locale == null) {
			bundle = ResourceBundle.getBundle(BASE_CAPTION_FILE);
		} else {
			try {
				bundle = ResourceBundle.getBundle(BASE_CAPTION_FILE, locale);
			} catch(MissingResourceException e) {
				// Fallback su lingua di default
				init(null);
			}
		}
	}
	
	private ResourceBundle getBundle() {
		if (bundle == null) {
			init(locale);
		}
		return bundle;
	}
	
	public String getCaption(String key) {
		try {
			return getBundle().getString(key);
		} catch (MissingResourceException e) {
			// Fallback
			return key;
		}
	}
}
