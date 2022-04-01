package org.integratedmodelling.klab.api;

import org.integratedmodelling.klab.common.SemanticType;

/**
 * Textual peer of a true observable with fluent API and minimal validation.
 * Used to discriminate observables in inputs of the observation functions.
 * 
 * @author Ferd
 *
 */
public class Observable extends SemanticType {

	private static final long serialVersionUID = -1660052679419582750L;

	public Observable(String s) {
		super(s);
	}

	public static Observable create(String s) {
		return new Observable(s);
	}

	public Observable in(String string) {
		// TODO Auto-generated method stub
		return this;
	}

}
