package org.integratedmodelling.klab.api.model;

import org.integratedmodelling.klab.common.SemanticType;
import org.integratedmodelling.klab.exceptions.KlabIllegalStateException;
import org.integratedmodelling.klab.utils.Range;

/**
 * Textual peer of a true observable with fluent API and minimal validation.
 * Used to discriminate observables in inputs of the observation functions.
 * 
 * @author Ferd
 *
 */
public class Observable extends SemanticType {

	private static final long serialVersionUID = -1660052679419582750L;

	private String name = null;
	private String unit = null;
	private Object value = null;
	private Range range = null;
	
	public Observable(String s) {
		super(s);
	}

	public static Observable create(String s) {
		return new Observable(s);
	}

	public Observable named(String name) {
		if (name != null) {
			throw new KlabIllegalStateException("cannot add modifiers more than once");
		}
		this.name = name;
		return this;
	}

	public Observable range(Range range) {
		if (unit != null || range != null) {
			throw new KlabIllegalStateException("cannot add modifiers more than once");
		}
		this.range = range;
		return this;
	}
	
	public Observable value(Object value) {
		if (value != null) {
			throw new KlabIllegalStateException("cannot add modifiers more than once");
		}
		this.value = value;
		return this;
	}
	
	/**
	 * Pass a valid unit or currency. No validation is done.
	 * 
	 * @param unit
	 * @return this observable
	 */
	public Observable in(String unit) {
		if (unit != null || range != null) {
			throw new KlabIllegalStateException("cannot add modifiers more than once");
		}
		this.unit = unit;
		return this;
	}

	@Override
	public String toString() {
		return(value == null ? "" : (value + " as ")) 
			+ super.toString() 
			+ (range == null ? "" : (range.getLowerBound() + " to " + range.getUpperBound())) 
			+ (unit == null ? "" : (" in " + unit))
			+ (name == null ? "" : (" named " + name));
			
	}
	
}
