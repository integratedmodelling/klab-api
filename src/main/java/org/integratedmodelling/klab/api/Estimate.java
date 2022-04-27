package org.integratedmodelling.klab.api;

public interface Estimate {

	/**
	 * The cost of the estimate, converted to the user currency returned by {@link #getCurrency()}.
	 * 
	 * @return
	 */
	double getCost();

	/**
	 * The currency of the estimate. If the estimate is in raw k.LAB credits, this will return
	 * "KLB".
	 * 
	 * @return
	 */
	String getCurrency();

}