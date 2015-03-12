package com.darkside.judge;

import java.util.*;
import lombok.*;
import org.joda.time.*;

/**
 * Investigation is a class that represents a single investigation report from
 * the users entry information from within the Judge Center.  This class
 * contains all of the information that is displayed on the Judge Center
 * investigation list 'select' view as well as the information from the 'view'
 * tab. 
 * @author DarkSide Software - Nicola DiPasquale
 * @version 1.0
 * @since 1.0
 */
@lombok.Data
public class Investigation {
	
	/** The investigation identifier. */
	private long id;
	/** The investigation incident date. */
	private LocalDate incidentDate;
	/** The investigation entered date. */
	private LocalDate enteredDate;
	/** The investigation event sanctioning number. */
	private String sanctioningNo;
	/** The name of the user who entered the investigation. */
	private String enteredBy;
	/** The DCI number of the user who entered the investigation. */
	private long enteredDciNo;
	/** The name of the subject under investigation. */
	private String subject;
	/** The DCI number of the user who is under investigation. */
	private long subjectDciNo;
	/** The role of the subject at the event. */
	private String subjectRole;
	/** The REL of the event. */
	private String eventRel;
	/** The type of the event. */
	private String eventType;
	/** The city where the event occurred. */
	private String city;
	/** The country where the event occurred. */
	private String country;
	// private String infraction;
	/** The status of the investigation. */
	private String status;
	/** The resolution of the investigation. */
	private String resolution;
	
	/** The set of infractions under investigation. */
	@lombok.Setter(AccessLevel.NONE)
	private Set<String> infractions = new LinkedHashSet<String>();
	
	/** The list of witnesses to the infractions. */
	@lombok.Setter(AccessLevel.NONE)
	private List<Witness> witnesses = new ArrayList<Witness>();
	
	/** The list of statements from the witnesses. */
	@lombok.Setter(AccessLevel.NONE)
	private List<Statement> statements = new ArrayList<Statement>();
	
}
