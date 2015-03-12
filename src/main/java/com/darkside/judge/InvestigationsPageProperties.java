/*
 * Copyright (c) 2015 DarkMatter Software - Nicola DiPasquale
 */
package com.darkside.judge;

import java.util.*;
import java.util.regex.*;
import org.joda.time.format.*;

/**
 * InvestigationsPageProperties is a property wrapper class that upon construction
 * loads the provided property resource and reads all values associated with the
 * Judge Center investigation list page.
 * @author DarkSide Software - Nicola DiPasquale
 * @version 1.0
 * @since 1.0
 */
@lombok.Getter
public final class InvestigationsPageProperties {

	/** The investigations page URI. */
	private final String pageUri;
	
	/** The investigations page form HTML element name. */
	private final String formName;
	/** The form input state element name. */
	private final String formInputState;
	/** The form input target element name. */
	private final String formInputTarget;
	/** The form input pre-formatted target value. */ 
	private final String formInputTargetValue;
	/** The form input argument element name. */
	private final String formInputArgument;
	/** The form input argument value. */
	private final String formInputArgumentValue;
	
	/** The investigations page span element name. */
	private final String spanName;
	
	/** The investigations page number of investigations div CSS class. */
	private final String numInvestigationsDivClass;
	
	/** The form input page size element name. */
	private final String inputPageSize;
	/** The form input page size desired selection value. */
	private final String pageSize;
	
	/** The form link tab element identifier. */
	private final String linkTab;
	/** The next page link element identifier. */
	private final String linkNextPage;
	
	/** The form view tab element identifier. */
	private final String viewTab;
	
	/** The investigation table element name. */
	private final String table;
	/** The investigation row CSS class style name. */
	private final String tableRowClass;
	/** The investigation table date regular expression pattern. */
	private final Pattern tableDatePattern;
	/** The investigation table date formatter instance. */
	private final DateTimeFormatter tableDateFormat;
	
	/**
	 * Construct a new InvestigationsPageProperties instance from the provided
	 * resource identifier.
	 * @param resourceId The resource identifier of the properties file to load.
	 * @throws java.io.IOException
	 */
	public InvestigationsPageProperties(String resourceId) throws java.io.IOException {
		Properties properties = new Properties();
		properties.load(getClass().getClassLoader().getResourceAsStream(resourceId));

		pageUri = properties.getProperty("investigations.page.uri");
		
		formName = properties.getProperty("investigations.page.form");
		formInputState = properties.getProperty("investigations.page.form.state");
		formInputTarget = properties.getProperty("investigations.page.form.target");
		formInputTargetValue = properties.getProperty("investigations.page.form.target.value");
		formInputArgument = properties.getProperty("investigations.page.form.argument");
		formInputArgumentValue = properties.getProperty("investigations.page.form.argument.value");

		spanName = properties.getProperty("investigations.page.span.name");

		numInvestigationsDivClass = properties.getProperty("investigations.page.div.num_investigations");

		inputPageSize = properties.getProperty("investigations.page.input.page_size");
		pageSize = properties.getProperty("investigations.page.input.page_size.value");

		linkTab = properties.getProperty("investigations.page.link.tab");
		linkNextPage = properties.getProperty("investigations.page.link.next_page");
		
		viewTab = properties.getProperty("investigations.page.view.tab");

		table = properties.getProperty("investigations.page.table");
		tableRowClass = properties.getProperty("investigations.page.table.row.class");
		tableDatePattern = Pattern.compile(properties.getProperty("investigations.page.table.date_regex"));
		tableDateFormat = DateTimeFormat.forPattern(properties.getProperty("investigations.page.table.date_format")).withLocale(Locale.US);
	}
	
}
