package org.integratedmodelling.klab.api;

import java.util.concurrent.Future;

import org.integratedmodelling.klab.api.data.IGeometry;
import org.integratedmodelling.klab.api.model.Observation;
import org.integratedmodelling.klab.api.utils.Engine;
import org.integratedmodelling.klab.common.SemanticType;
import org.integratedmodelling.klab.rest.ObservationReference;

public class Context extends Observation {

	public Context(ObservationReference bean, Engine engine, String session) {
		super(bean, session, engine);
	}
	
	/**
	 * Call with a concept and geometry to create an observation or with an estimate
	 * to submit the estimate. Will also include any further observation hierarchy
	 * created by calling the with() functions in the estimate returned.
	 * 
	 * @return
	 */
	public Future<Estimate> estimate(Object... arguments) {
		return null;
	}

	/**
	 * Call with a concept and geometry to create an observation or with an estimate
	 * to submit the estimate. Will also include any further observation hierarchy
	 * created by calling the with() functions.
	 * 
	 * @return
	 */
	public Future<Observation> submit(Object... arguments) {
		return null;
	}

	/**
	 * Use in a fluent fashion to insert quality observations into the context at
	 * the next submit(). Does not have any effect before submit() is called.
	 * 
	 * @param concept
	 * @param value   a value appropriate for the concept
	 * @return this same context for chaining calls.
	 */
	public Context with(Observable concept, Object value) {
		return this;
	}

	/**
	 * Create a sub-object for the next submit. Any
	 * {@link #with(SemanticType, Object)} or {@link #with(SemanticType, IGeometry)}
	 * on the resulting context will apply to the object.
	 * 
	 * @param subject
	 * @param geometry
	 * @return
	 */
	public Context with(Observable subject, IGeometry geometry) {
		return null;
	}

}
