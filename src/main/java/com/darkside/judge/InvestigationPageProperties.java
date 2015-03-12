package com.darkside.judge;

import java.util.*;

/**
 * InvestigationPageProperties is a property wrapper class that upon construction
 * loads the provided property resource and reads all values associated with the
 * Judge Center investigation page.
 * @author DarkSide Software - Nicola DiPasquale
 * @version 1.0
 * @since 1.0
 */
@lombok.Getter
public final class InvestigationPageProperties {

	/** The summary table HTML identifier. */
	private final String summaryTable;
	/** THe summary table table datum element class. */
	private final String summaryDatumClass;
	
	/** The witness table HTML identifier. */
	private final String witnessTable;
	/** The witness table datum element identifier. */
	private final String witnessName;
	/** The witness table datum role identifier. */
	private final String witnessRole;
	
	/** The infraction table HTML identifier. */
	private final String infractionTable;
	
	/** The table 'Key: Value' regular expression pattern. */
	private final String tableKeyValuePattern;
	
	/** The statement table HTML identifier. */
	private final String statementTable;
	
	/**
	 * Construct a new InvestigationPageProperties instance from the provided
	 * resource identifier.
	 * @param resourceId The resource identifier of the properties file to load.
	 * @throws java.io.IOException
	 */
	public InvestigationPageProperties(String resourceId) throws java.io.IOException {
    	Properties properties = new Properties();
    	properties.load (getClass().getClassLoader ().getResourceAsStream (resourceId));
    	
    	summaryTable = properties.getProperty("investigation.page.summary.table");
    	summaryDatumClass = properties.getProperty("investigation.page.summary.table.datum.class");
    	
    	witnessTable = properties.getProperty("investigation.page.witness.table");
    	witnessName = properties.getProperty("investigation.page.witness.row.name");
    	witnessRole = properties.getProperty("investigation.page.witness.row.role");
    	
    	infractionTable = properties.getProperty("investigation.page.infraction.table");
    	
    	tableKeyValuePattern = properties.getProperty("investigation.page.table.row.key_value.pattern");
    	
    	statementTable = properties.getProperty("investigation.page.statement.table");
	}
	
}
