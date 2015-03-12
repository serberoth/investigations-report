package com.darkside.judge;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.apache.commons.lang3.*;
import org.joda.time.*;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;

/**
 * Reporter is a class that uses HtmlUnit to load the investigations panel of
 * the Magic the Gathering: Judge Center and scrape the content from those pages
 * as the provided user.  Users of this class must provide valid credentials
 * in order to login and read their investigations. Those investigations are
 * then loaded into an InvestigationSet which can be processed.
 * @author DarkSide Software - Nicola DiPasquale
 * @since 1.0
 * @version 1.0
 */
@lombok.extern.slf4j.Slf4j
public class Reporter {
	
	/* The login page property file resource identifier */
	private static final String RESOURCE_LOGIN_PROPERTIES = "com/darkside/judge/login_page.properties";
	/* The login credential property file resource identifier */
	private static final String RESOURCE_CREDS_PROPERTIES = "com/darkside/judge/login_creds.properties";
	/* The investigation page property file resource identifier */
	private static final String RESOURCE_CASES_PROPERTIES = "com/darkside/judge/investigations_page.properties";
	
	/* The HtmlUnit WebClient instance used by this instance to load and scrape pages */
	private WebClient client;
	
	/**
	 * Create a new Reporter instance.
	 */
	public Reporter() {
		client = new WebClient(BrowserVersion.INTERNET_EXPLORER_11);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		// Close the WebClient instance on finalize
		if (client != null) {
			client.closeAllWindows();
			client = null;
		}
	}
	
	/**
	 * loadInvestigations is responsible for taking the provided credentials
	 * logging into the Judge Center then loading the investigations page.  Upon
	 * loading the investigations page the authenticated users investigations
	 * are then processed and loaded into an InvestigationSet which is returned
	 * by this function.
	 * @param userId The users DCI number.
	 * @param passwd The users password.
	 * @return An InvestigationSet containing the users investigations or null
	 * 		if the credentials were invalid.
	 * @throws IOException Failure exception when making web-requests.
	 */
	public InvestigationsSet loadInvestigations(String userId, char[] passwd) throws IOException {
		if (doLogin(new LoginPageProperties(RESOURCE_LOGIN_PROPERTIES), userId, passwd)) {
			log.debug("Login successful");
			
			InvestigationsSet set = loadInvestigations(new InvestigationsPageProperties(RESOURCE_CASES_PROPERTIES));
			
			for (Investigation investigation : set) {
				log.debug("Investigation: " + investigation);
			}
			
			return set;
		}
		
		return null;
	}
	
	/*
	 * doLogin is responsible for logging the user into the judge center with
	 * the provided credentials (DCI number and password).  This method returns
	 * true if the login was successful or false otherwise.
	 */
	private boolean doLogin(LoginPageProperties properties, String userId, char[] passwd) throws IOException {
		log.debug("Attempting site-login @" + properties.getPageUri());
		HtmlPage page = client.getPage(properties.getPageUri());
		log.debug("Login Page Title: " + page.getTitleText());

		// Get the login form and populate the user id and password fields.
		HtmlForm form = page.getFormByName(properties.getFormName());
		form.getInputByName(properties.getInputUserId()).setValueAttribute(userId);
		// XXX: HtmlUnit does not give any other choice but to convert the password into a string
		form.getInputByName(properties.getInputPasswd()).setValueAttribute(new String(passwd));
		log.debug("Entered user id and password into form fields: " + userId);

		// Click the login button (leave the language drop down set with English).
		HtmlPage result = form.getInputByName(properties.getInputSubmit()).click();
		log.debug("Result Page Title: " + result.getTitleText());
		log.debug("Result Page URI: " + result.getUrl());
		DomElement errorSpan = result.getElementById(properties.getInputErrors());
		if (errorSpan != null) {
			log.error(errorSpan.getTextContent());
		}

		return result.getUrl().equals(properties.getSuccessUrl());
	}
	
	/*
	 * loadInvestigations loads the authenticated users investigations into an
	 * InvestigationSet instance.  The InvestigationSet instance contains the
	 * name of the user logged in and a set of the users investigations.
	 */
	private InvestigationsSet loadInvestigations(InvestigationsPageProperties properties) throws IOException {
		// Load the investigations list page.
		HtmlPage page = client.getPage(properties.getPageUri());
		page = page.getAnchorByText(properties.getLinkTab()).click();

		// Check the current selected tab (should be '3' the 'select' tab).
		String value = page.getFormByName(properties.getFormName()).getInputByName(properties.getFormInputState()).getValueAttribute();
		log.debug("Page Selected Tab: " + value);
		
		// Pull the current logged in users name from the navigation in the upper right.
		HtmlSpan element = page.getHtmlElementById(properties.getSpanName());
		String name = element.getTextContent();
		log.debug("Name: " + name);
		
		// Parse the number of investigations from the information near the number to show drop down control.
		int numInvestigatons = -1;
		for (HtmlDivision div : HtmlUnitUtilities.<HtmlDivision>getElementsByClass(page, "div", "results")) {
			String text = div.getTextContent().trim();
			Pattern pattern = Pattern.compile("^.*(\\d+)\\.$");
			Matcher matcher = pattern.matcher(text);
			if (matcher.matches()) {
    			String match = matcher.group(1);
    			try {
        			numInvestigatons = Integer.parseUnsignedInt(match);
        			log.debug("Processing " + numInvestigatons + " exams");
    			} catch (NumberFormatException nfe) {
    				log.warn("Failed to read total number of investigations", nfe);
    			}
			}
			break;
		}
		
		/*
		// TODO: Do something if the value was not found
		if (monitor != null && numExams > 0) {
			monitor.setMaximum(monitor.getMaximum() + numExams);
		}
		 */

		// Select the number to show drop down and set it to the maximum value.
		HtmlSelect select = page.getElementByName(properties.getInputPageSize());
		page = select.setSelectedAttribute(properties.getPageSize(), true);
		
		// Switch the tab to the 'view' tab then switch back to the 'select' tab to reset the view state.
		// We do this because of the way the Judge Center processes state changes through JavaScript.
		HtmlTable table = page.getHtmlElementById(properties.getTable());
		List<HtmlTableRow> rows = table.getRows();
		page = rows.get(1).click();
		page = page.getAnchorByText(properties.getLinkTab()).click();
		
		// Load all investigations from all pages in the list.
		List<Investigation> list = new ArrayList<Investigation>();
		HtmlPage nextPage = page;
		log.debug("Loading page 1");
		InvestigationPageProperties caseProperties = new InvestigationPageProperties(RESOURCE_CASES_PROPERTIES);
		loadInvestigationsFromCurrentPage(list, nextPage, properties, caseProperties); // , monitor);
		int n = 2;
		// Click the next page link and load the investigations from the next page.
		while ((nextPage = HtmlUnitUtilities.findAndClickAnchor(nextPage, properties.getLinkNextPage())) != null) {
			log.debug("Loading page " + n++);
			loadInvestigationsFromCurrentPage(list, nextPage, properties, caseProperties); // , monitor);
		}
		
		// LOGGER.debug ("Resulting Investigation Page:\n" + page.asText ());
		log.debug("Found " + list.size() + " investigations");
		return new InvestigationsSet(name, list);
	}
	
	/*
	 * loadInvestigationsFromCurrentPage loads the investigations from the
	 * current listing page pulling all of the pertinent information from the
	 * listing table then selecting the investigation to load its 'view' tab
	 * and then loading the information for that investigation from the 'view'
	 * tab as well.
	 */
	private void loadInvestigationsFromCurrentPage(List<Investigation> investigationList, HtmlPage page, InvestigationsPageProperties listProperties, InvestigationPageProperties caseProperties) throws IOException { //, Monitor monitor) throws IOException {
		// Get the investigation list table.
		HtmlTable table = page.getHtmlElementById(listProperties.getTable());
		List<HtmlTableRow> rows = table.getRows();
		log.debug("Num Table Rows: " + rows.size());

		// Loop through each row in the table skipping the header row (row 0).
		for (int i = 1; i < rows.size(); ++i) {
			HtmlTableRow row = rows.get(i);

			// Skip any row without the appropriate class identifier.
			if (!row.getAttribute("class").equals(listProperties.getTableRowClass())) {
				log.debug("Skipping row: " + i);
				continue;
			}

			// Create an Investigation instance then load the investigation
			// information into that instance from the HTML table row.
			Investigation investigation = new Investigation();
			
			investigation.setId(cellAsNumber(row.getCell(0).getTextContent()));
			investigation.setEnteredBy(stripCellContent(row.getCell(3).getTextContent()));
			investigation.setSubject(stripCellContent(row.getCell(4).getTextContent()));
			investigation.setEventRel(stripCellContent(row.getCell(5).getTextContent()));
			investigation.setEventType(stripCellContent(row.getCell(6).getTextContent()));
			investigation.setCity(stripCellContent(row.getCell(7).getTextContent()));
			investigation.setCountry(stripCellContent(row.getCell(8).getTextContent()));
			// investigation.setInfraction(stripCellContent(row.getCell(9).getTextContent()));
			investigation.getInfractions().add(stripCellContent(row.getCell(9).getTextContent()));
			investigation.setStatus(stripCellContent(row.getCell(10).getTextContent()));
			investigation.setResolution(stripCellContent(row.getCell(11).getTextContent()));
			
			// Recreate the selection script for clicking on the table row to
			// load the current investigations 'view' tab then load the
			// information from that tab for this investigation instance.
			HtmlForm form = page.getFormByName(listProperties.getFormName());
			// Set the form values for target and argument.
			String target = MessageFormat.format(listProperties.getFormInputTargetValue(), i + 2);
			form.getInputByName(listProperties.getFormInputTarget()).setValueAttribute(target);
			String argument = listProperties.getFormInputArgumentValue();
			form.getInputByName(listProperties.getFormInputArgument()).setValueAttribute(argument);
			log.debug("Target: " + target + ", Argument: " + argument);
			// Inject a submit button into the form so that it can be submitted.
			HtmlElement button = (HtmlElement) page.createElement("button");
			button.setAttribute("type", "submit");
			form.appendChild(button);
			HtmlPage casePage = button.click();
			
			// This does not work as the first investigation is continually
			// loaded upon subsequent requests (probably due to the way the
			// JavaScript state is managed in the Judge Center).
			// HtmlPage casePage = row.click();
			
			// Select the 'view' tab.
			String value = casePage.getFormByName(listProperties.getFormName()).getInputByName(listProperties.getFormInputState()).getValueAttribute();
			log.debug("Page Selected Tab: " + value);
			
			// Load the summary information table at the head of the page.
			HtmlTable summaryTable = casePage.getHtmlElementById(caseProperties.getSummaryTable());
			List<HtmlTableRow> summaryRows = summaryTable.getRows();
			
			long casePageId = cellAsNumber(summaryRows.get(0).getCell(0).getTextContent());
			if (investigation.getId() != casePageId) {
				throw new IllegalStateException(investigation.getId() + " != " + casePageId);
			}
			
			investigation.setIncidentDate(cellAsDate(summaryRows.get(1).getCell(0).getTextContent(), listProperties));
			investigation.setEnteredDate(cellAsDate(summaryRows.get(2).getCell(0).getTextContent(), listProperties));
			
			// Load the event sanctioning number from the 'Key: Value' row content.
			String sacntionNo = stripCellContent(summaryRows.get(3).getCell(0).getTextContent()); {
    			Pattern pattern = Pattern.compile(caseProperties.getTableKeyValuePattern());
    			Matcher matcher = pattern.matcher(sacntionNo);
    			matcher.find();
    			investigation.setSanctioningNo(matcher.group(1));
			}
			
			investigation.setEnteredDciNo(cellAsNumber(summaryRows.get(5).getCell(0).getTextContent()));
			investigation.setSubjectDciNo(cellAsNumber(summaryRows.get(7).getCell(0).getTextContent()));
			
			// Load the subject role from the 'Key: Value' row content.
			String role = stripCellContent(summaryRows.get(8).getCell(0).getTextContent()); {
    			Pattern pattern = Pattern.compile(caseProperties.getTableKeyValuePattern());
    			Matcher matcher = pattern.matcher(role);
    			matcher.find();
    			investigation.setSubjectRole(matcher.group(1));
			}
			
			// Load the witness table in the middle of the page.
			HtmlTable witnessTable = casePage.getHtmlElementById(caseProperties.getWitnessTable());
			List<HtmlTableRow> witnessRows = witnessTable.getRows();
			for (int j = 1; j < witnessRows.size(); ++j) {
				HtmlTableRow witnessRow = witnessRows.get(j);
				Witness witness = new Witness();
				witness.setName(stripCellContent(witnessRow.getCell(0).getTextContent()));
				witness.setDciNo(cellAsNumber(witnessRow.getCell(1).getTextContent()));
				witness.setRole(stripCellContent(witnessRow.getCell(2).getTextContent()));
				investigation.getWitnesses().add(witness);
			}
			
			// Load the infraction table in the middle of the page.
			HtmlTable infractionTable = casePage.getHtmlElementById(caseProperties.getInfractionTable());
			List<HtmlTableRow> infractionRows = infractionTable.getRows();
			for (int j = 1; j < infractionRows.size(); ++j) {
				HtmlTableRow infractionRow = infractionRows.get(j);
				
				investigation.getInfractions().add(stripCellContent(infractionRow.getCell(0).getTextContent()));
			}
			
			// Load the statement table at the bottom of the page.
			HtmlTable statementTable = casePage.getHtmlElementById(caseProperties.getStatementTable());
			List<HtmlTableRow> statementRows = statementTable.getRows();
			for (int j = 1; j < statementRows.size(); ++j) {
				HtmlTableRow statementRow = statementRows.get(j);
				Statement statement = new Statement();
				
				// Strip and process the second cell containing the witness name
				// entered date and entered by information.
				String statementInfo = stripCellContent(statementRow.getCell(1).getTextContent());
				log.debug("Statement Info: " + statementInfo);
				String[] lines = statementInfo.split("\\s*\\r?\\n\\s*");
				statement.setWitnessName(stripCellContent(lines[0]));
				for (Witness w : investigation.getWitnesses()) {
					if (statement.getWitnessName().equals(w.getName())) {
						statement.setWitnessDciNo(w.getDciNo());
						statement.setWitness(w);
						w.setStatement(statement);
						break;
					}
				}
				// Load the entered by information from the second line as 'Key: Value' row content.
    			Pattern pattern = Pattern.compile(caseProperties.getTableKeyValuePattern());
    			Matcher matcher = pattern.matcher(lines[2]);
    			matcher.find();
				statement.setEnteredBy(stripCellContent(matcher.group(1)));
				// Load the entered date from the cell content.
				statement.setEnteredDate(cellAsDate(statementInfo, listProperties));
				
				// Load the witness statement.
				statement.setStatement(stripCellContent(statementRow.getCell(2).getTextContent()));
				
				investigation.getStatements().add(statement);
			}
			
			// Add the investigation to the list
			investigationList.add(investigation);
			
			// Cleanup the current page to reset the current window information.
			// This call might be superfluous.
			casePage.cleanUp();
			
			/*
			if (monitor != null) {
    			monitor.setProgress(monitor.getProgress() + 1);
			}
			 */
			
			log.debug("Added Investigation: " + investigation);
		}
	}
	
	private String stripCellContent(String cellContent) {
		// Remove extraneous white space characters and " characters that are
		// added around content within the Judge Center.
		if (cellContent != null) {
			return StringUtils.strip(StringUtils.replaceChars(cellContent, "%\u00a0\u2007\u202f", ""));
		}
		return "";
	}
	
	private LocalDate cellAsDate(String cellContent, InvestigationsPageProperties properties) {
		// Convert the cell content to a LocalDate.
		Matcher matcher = properties.getTableDatePattern().matcher(cellContent);
		if (matcher.find()) {
			cellContent = matcher.group(1);
		}

		cellContent = stripCellContent(cellContent);

		if (cellContent != null && !"".equals(cellContent)) {
			return properties.getTableDateFormat().parseLocalDate(cellContent);
		}
		return null;
	}
	
	private long cellAsNumber(String cellContent) {
		// Convert the cell content into a standard integral numerical value.
		cellContent = stripCellContent(cellContent);
		if (cellContent != null && !"".equals(cellContent)) {
			try {
				Pattern pattern = Pattern.compile("^[^\\d]*(\\d+).*$");
				Matcher matcher = pattern.matcher(cellContent);
				if (matcher.find()) {
					cellContent = matcher.group(1);
        			return Long.parseLong(cellContent);
				}
			} catch (NumberFormatException nfe) {
			}
		}
		return -1;
	}
	
	/**
	 * Run the reporting software to process the provided users investigations.
	 * @param args The user id and password as the first and second parameters.
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String userId;
		char[] passwd;
		
		if (args.length >= 2) {
			// Load the credentials from the command line when provided.
			userId = args[0];
			passwd = args[1].toCharArray();
		} else {
			// Load the credentials from the properties file when not provided on the command line.
			Properties properties = new Properties();
	    	properties.load(Reporter.class.getClassLoader().getResourceAsStream(RESOURCE_CREDS_PROPERTIES));
	    	
	    	userId = properties.getProperty("login.page.userid");
	    	passwd = properties.getProperty("login.page.passwd").toCharArray();
		}
		
		new Reporter().loadInvestigations(userId, passwd);
	}
	
}
