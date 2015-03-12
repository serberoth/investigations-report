/*
 * Copyright (c) 2015 DarkMatter Software - Nicola DiPasquale
 */
package com.darkside.judge;

import java.net.*;
import java.util.*;

/**
 * LoginPageProperties is a property wrapper class that upon construction loads
 * the provided property resource and reads all values associated with the
 * Judge Center login page.
 * @author DarkSide Software - Nicola DiPasquale
 * @version 1.0
 * @since 1.0
 */
@lombok.Getter
public final class LoginPageProperties {

	/** The login page URI. */
	private final String pageUri;
	/** The login form element name. */
	private final String formName;
	/** The login form user identifier input element name. */
	private final String inputUserId;
	/** The login form password input element name. */
	private final String inputPasswd;
	/** The login form submit button element name. */
	private final String inputSubmit;
	/** The login form error element name. */
	private final String inputErrors;
	
	/** The login success URL. */
	private final URL successUrl;
	
	/**
	 * Construct a new LoginPageProperties instance from the provided resource
	 * identifier.
	 * @param resourceId The resource identifier of the properties file to load.
	 * @throws java.io.IOException
	 */
	public LoginPageProperties(String resourceId) throws java.io.IOException {
    	Properties properties = new Properties();
    	properties.load (getClass().getClassLoader ().getResourceAsStream (resourceId));
    	
    	pageUri = properties.getProperty ("login.page.uri");
    	formName = properties.getProperty ("login.page.formname");
    	inputUserId = properties.getProperty ("login.page.input.userid");
    	inputPasswd = properties.getProperty ("login.page.input.passwd");
    	inputSubmit = properties.getProperty ("login.page.input.submit");
    	inputErrors = properties.getProperty ("login.page.input.errors");
    	
    	successUrl = new URL(properties.getProperty ("login.page.success.url"));
	}
	
}
