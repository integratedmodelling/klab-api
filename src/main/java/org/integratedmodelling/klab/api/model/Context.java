package org.integratedmodelling.klab.api.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.integratedmodelling.klab.api.Klab.ExportFormat;
import org.integratedmodelling.klab.api.TicketHandler;
import org.integratedmodelling.klab.api.data.IGeometry;
import org.integratedmodelling.klab.api.runtime.ITicket.Type;
import org.integratedmodelling.klab.api.utils.Engine;
import org.integratedmodelling.klab.common.SemanticType;
import org.integratedmodelling.klab.exceptions.KlabIllegalArgumentException;
import org.integratedmodelling.klab.exceptions.KlabIllegalStateException;
import org.integratedmodelling.klab.rest.ObservationReference;
import org.integratedmodelling.klab.rest.ObservationRequest;
import org.integratedmodelling.klab.utils.Pair;

public class Context extends Observation {

	/*
	 * if defined, these are submitted at the next submit() before the main
	 * observable is queried, then cleared.
	 */
	List<Pair<Observable, Object>> injectedStates = new ArrayList<>();

	/*
	 * if not null, the next two define a sub-object which is observed and becomes
	 * the context BEFORE submit() is called. Defining these creates a subclass of
	 * Context targeted to the object. The observable must be a subject or other
	 * direct observable.
	 */
	IGeometry injectedGeometry;
	Observable injectedDirectObservable;

	public Context(ObservationReference bean, Engine engine) {
		super(bean, engine);
	}

	/**
	 * Call with a concept and geometry to create an observation or with an estimate
	 * to submit the estimate. Will also include any further observation hierarchy
	 * created by calling the with() functions in the estimate returned.
	 * 
	 * @return
	 */
	public Future<Estimate> estimate(Observable observable, Object... arguments) {

		ObservationRequest request = new ObservationRequest();
		request.setContextId(this.reference.getId());
		request.setEstimate(true);
		request.setUrn(observable.toString());
		for (Object o : arguments) {
			if (o instanceof String) {
				request.getScenarios().add((String) o);
			}
		}

		String ticket = engine.submitObservation(request);
		if (ticket != null) {
			return new TicketHandler<Estimate>(engine, ticket, this);
		}

		throw new KlabIllegalArgumentException(
				"Cannot build estimate request from arguments: " + Arrays.toString(arguments));
	}

	/**
	 * Call with a concept and geometry to create an observation or with an estimate
	 * to submit the estimate. Will also include any further observation hierarchy
	 * created by calling the with() functions.
	 * 
	 * @return
	 */
	public Future<Observation> submit(Observable observable, Object... arguments) {

		ObservationRequest request = new ObservationRequest();
		request.setContextId(this.reference.getId());
		request.setEstimate(false);
		request.setUrn(observable.toString());
		for (Object o : arguments) {
			if (o instanceof String) {
				request.getScenarios().add((String) o);
			}
		}

		String ticket = engine.submitObservation(request);
		if (ticket != null) {
			// TODO updates the context bean when observation arrives!
			return new TicketHandler<Observation>(engine, ticket, this);
		}

		throw new KlabIllegalArgumentException(
				"Cannot build observation request from arguments: " + Arrays.toString(arguments));
	}

	public Future<Observation> submit(Estimate estimate) {
		if (estimate.getTicketType() != Type.ObservationEstimate) {
			throw new KlabIllegalArgumentException("the estimate passed is not a context estimate");
		}
		String ticket = engine.submitEstimate(estimate.getEstimateId());
		if (ticket != null) {
			// the handler updates the context catalog when the observation arrives
			return new TicketHandler<Observation>(engine, ticket, this);
		}

		throw new KlabIllegalStateException("estimate cannot be used");

	}

	/**
	 * Retrieve the current dataflow for the context in the passed format. The ELK
	 * graph can be visualized through viewers that understand it, such as Sprotty.
	 * The k.DL format is readable by humans and by k.LAB, and is the basis of k.DL
	 * exported resources.
	 * 
	 * @param format only admits {@link ExportFormat#KDL_CODE} or
	 *               {@link ExportFormat#ELK_GRAPH_JSON}
	 * @return the dataflow code in the requested format.
	 * @throws KlabIllegalArgumentException if format isn't suitable to dataflow
	 *                                      output
	 * @throws KlabRemoteException          if transfer fails for any reason
	 */
	public String getDataflow(ExportFormat format) {
		// TODO
		return null;
	}

	/**
	 * Retrieve the current provenance graph for the context in the passed format.
	 * The ELK graph can be visualized through viewers that understand it, such as
	 * Sprotty. The k.IM provenance records define entities with prov-o vocabulary
	 * that can be added to resources and reused, or exported to RDF (forthcoming as
	 * an option).
	 * 
	 * @param simplified if true, output will only contain artifacts and isDerivedBy
	 *                   relationships. Otherwise all the agents, processes and
	 *                   plans will be returned, including any provenance
	 *                   information stored with remote resources.
	 * @param format     only admits {@link ExportFormat#KIM_CODE} or
	 *                   {@link ExportFormat#ELK_GRAPH_JSON}. Asking for k.IM code
	 *                   currently produces an empty output.
	 * @return
	 * @throws KlabIllegalArgumentException if format isn't suitable to provenance
	 *                                      output
	 * @throws KlabRemoteException          if transfer fails for any reason
	 */
	public String getProvenance(boolean simplified, ExportFormat format) {
		// TODO
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
		// TODO
		return this;
	}

	/**
	 * Create a future for a sub-object. Any submit called on the resulting context
	 * will apply any {@link #with(SemanticType, Object)} or
	 * {@link #with(SemanticType, IGeometry)} to the resulting object. The resulting
	 * context is detached from this, holding any injected states and will operate
	 * independently.
	 * 
	 * @param subject
	 * @param geometry
	 * @return
	 */
	public Future<Context> with(Observable subject, IGeometry geometry) {
		// TODO
		return null;
	}

	/*
	 * Called after an observation to update the context data and ensure the context
	 * has the new observation in its catalog.
	 * 
	 * @Non-API should be package private
	 * 
	 * @param ret
	 */
	public void updateWith(Observation ret) {
		this.reference = engine.getObservation(reference.getId());
		for (String name : this.reference.getChildIds().keySet()) {
			if (ret.reference.getId().equals(this.reference.getChildIds().get(name))) {
				catalogIds.put(name, ret.reference.getId());
				catalog.put(ret.reference.getId(), ret);
				break;
			}
		}

	}

}
