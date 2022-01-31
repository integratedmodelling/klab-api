package org.integratedmodelling.klab.api;

import java.util.concurrent.Future;

import org.integratedmodelling.klab.api.model.Observation;

public class Context extends Observation {
	
	/**
	 * @return
	 */
	public Future<Estimate> estimate(Object... arguments) {
		return null;
	}

	/** 
	 * @return
	 */
	public Future<Observation> submit(Estimate estimate) {
		return null;
	}
}
