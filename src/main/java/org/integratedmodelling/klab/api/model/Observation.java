package org.integratedmodelling.klab.api.model;

import org.integratedmodelling.klab.rest.ObservationReference;

public class Observation  {
		
	private ObservationReference reference;
	
	protected Observation(ObservationReference reference) {
		this.reference = reference;
	}
	
}
