/*
 * Copyright (c) 2015 DarkMatter Software - Nicola DiPasquale
 */
package com.darkside.judge;

import java.io.*;
import java.util.*;
// import org.apache.commons.logging.*;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;

/**
 * HtmlUnitUtilities is a utilities class that provides convenience methods for
 * processing HTML elements using HtmlUnit given various DOM elements.
 * @author DarkSide Software - Nicola DiPasquale
 * @version 1.0
 * @since 1.0
 */
public final class HtmlUnitUtilities {
	
	/* This class cannot be instantiated. */
	private HtmlUnitUtilities() { throw new UnsupportedOperationException(); }
	
	/**
	 * getElementsByClass loads a list of elements from the provided HtmlPage
	 * instance that all have the specified CSS class style listed.
	 * @param page The HtmlPage instance to search.
	 * @param tagName The HtmlElement tag name for which to search.
	 * @param className The CSS style class for which to search.
	 * @return A List of HtmlElement instances found.
	 */
	public static <T extends HtmlElement> List<T> getElementsByClass(HtmlPage page, String tagName, String className) {
		DomNodeList<DomElement> nodeList = page.getElementsByTagName(tagName);
		List<T> list = new ArrayList<T>();
		for (DomElement element : nodeList) {
			if (element.getAttribute("class").equals(className)) {
				@SuppressWarnings("unchecked")
				T t = (T) element;
				list.add (t);
			}
		}
		return list;
	}
	
	/**
	 * findAndClickAnchor is a utility method that finds the first instance of
	 * an HtmlAnchor instance with the specified textual content then clicks
	 * that anchor instance and returns the resulting HtmlPage instance.
	 * @param page The current HtmlPage instance.
	 * @param anchorText The anchor text content for which to search.
	 * @return The resultant HtmlPage instance.
	 * @throws IOException
	 */
	public static HtmlPage findAndClickAnchor(HtmlPage page, String anchorText) throws IOException {
		try {
			HtmlAnchor anchor = page.getHtmlElementById(anchorText);
    		return anchor.click();
		} catch (ElementNotFoundException enfe) {
			return null;
		}
	}
	
}
