package com.darkside.judge;

import java.util.*;

/**
 * InvestigationSet is a class that represents the set of investigations from
 * the Judge Center for the specified user.
 * @author DarkSide Software - Nicola DiPasquale
 * @version 1.0
 * @since 1.0
 */
@lombok.AllArgsConstructor
class InvestigationsSet implements Iterable<Investigation> {
	
	/** The display name of the user. */
	@lombok.Getter
	private final String name;
	/** The list of investigation instances. */
	private final List<Investigation> investigations;

	/**
	 * Get the size of this InvestigationSet.
	 * @return The size of this InvestigationSet.
	 */
	public int size() {
		return investigations.size();
	}
	
	/**
	 * Get the Investigation instance for the specified index.
	 * @param index The index of the desired Investigation instance.
	 * @return The Investigation instance at the specified index.
	 */
	public Investigation get(int index) {
		return investigations.get(index);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<Investigation> iterator() {
		return investigations.iterator();
	}
	
}
