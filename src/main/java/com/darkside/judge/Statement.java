package com.darkside.judge;

import org.joda.time.*;

/**
 * Statement is a class that contains all of the information for a particular
 * witness statement for a given investigation incident.
 * @author DarkSide Software - Nicola DiPasquale
 * @version 1.0
 * @since 1.0
 */
@lombok.Data @lombok.ToString(exclude="witness")
public class Statement {
	
	/** The name of the witness. */
	private String witnessName;
	/** The DCI number of the witness. */
	private long witnessDciNo;
	/** The date the statement was entered. */
	private LocalDate enteredDate;
	/** The name of the user who entered the statement. */
	private String enteredBy;
	/** The witness statement. */
	private String statement;
	
	/** The Witness information, if any. */
	private Witness witness;

}
