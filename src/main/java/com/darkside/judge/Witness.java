/*
 * Copyright (c) 2015 DarkMatter Software - Nicola DiPasquale
 */
package com.darkside.judge;

/**
 * Witness is a class that contains the information for a particular witness to
 * an investigation incident.
 * @author DarkSide Software - Nicola DiPasquale
 * @version 1.0
 * @since 1.0
 */
@lombok.Data @lombok.ToString(exclude="statement")
public class Witness {
	
	/** The name of the witness. */
	private String name;
	/** The DCI number of the witness. */
	private long dciNo;
	/** The role of the witness. */
	private String role;
	
	/** The witness Statement instance; if any. */
	private Statement statement;

}
