package org.integratedmodelling.klab.api;

import java.util.concurrent.Future;

import org.integratedmodelling.klab.api.Klab.ExportFormat;
import org.integratedmodelling.klab.exceptions.KlabIllegalArgumentException;
import org.integratedmodelling.klab.exceptions.KlabRemoteException;

public interface Context extends Observation {

	/**
	 * Call with a concept (and geometry if the observable is not a quality) to
	 * create an estimate of the cost associated with the observation. Will also
	 * include any further observation hierarchy created by calling the with()
	 * functions in the estimate returned. Computing the estimate may involve
	 * observations and inference, so a future is returned. If a direct observable
	 * (subject, event or relationship) is used, the result will be a group (created
	 * by resolving an instantiator) unless and a geometry is passed. In the latter
	 * case, the observation will be of the object built and will contain the result
	 * of any resolution (potentially none).
	 * 
	 * @param observable the observable for the observation desired
	 * @param arguments  other observables (to make more than one observation in one
	 *                   call, limited to qualities), a geometry if the observable
	 *                   is a direct observation (must include suitable time if the
	 *                   observable is an event), or any strings which will be
	 *                   interpreted as scenario URNs to affect the resolution at
	 *                   the engine side. If a relationship is built, two subject
	 *                   observations must also be passed, interpreted as source and
	 *                   destination in order of call.
	 * 
	 * @return the future observation being computed on the backend.
	 */
	Future<Estimate> estimate(Observable observable, Object... arguments);

	/**
	 * Call with a concept (and geometry if the observable is not a quality) to
	 * create an observation in this context. Will also include any further
	 * observation hierarchy created by calling the with() functions. If a direct
	 * observable (subject, event or relationship) is used, the result will be a
	 * group (created by resolving an instantiator) unless and a geometry is passed.
	 * In the latter case, the observation will be of the object built and will
	 * contain the result of any resolution (potentially none).
	 * 
	 * @param observable the observable for the observation desired
	 * @param arguments  other observables (to make more than one observation in one
	 *                   call, limited to qualities), a geometry if the observable
	 *                   is a direct observation (must include suitable time if the
	 *                   observable is an event), or any strings which will be
	 *                   interpreted as scenario URNs to affect the resolution at
	 *                   the engine side. If a relationship is built, two subject
	 *                   observations must also be passed, interpreted as source and
	 *                   destination in order of call.
	 * 
	 * @return the future observation being computed on the backend.
	 */
	Future<Observation> submit(Observable observable, Object... arguments);

	/**
	 * Submit a previously computed estimate, implicitly accepting any costs
	 * involved.
	 * 
	 * @param estimate
	 * @return the future observation being computed on the backend.
	 */
	Future<Observation> submit(Estimate estimate);

	/**
	 * Retrieve the current dataflow for the context in the passed format. The ELK
	 * graph can be visualized through viewers that understand it, such as Sprotty.
	 * The k.DL format is readable by humans and by k.LAB, and is the basis of k.DL
	 * exported resources.
	 * 
	 * @param format only admits {@link ExportFormat#KDL_CODE} or
	 *               {@link ExportFormat#ELK_GRAPH_JSON}
	 * @return the dataflow code in the requested textual format.
	 * @throws KlabIllegalArgumentException if format isn't suitable to dataflow
	 *                                      output
	 * @throws KlabRemoteException          if transfer fails for any reason
	 */
	String getDataflow(ExportFormat format);

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
	 *                   information stored with the resources used in the
	 *                   computation.
	 * @param format     only admits {@link ExportFormat#KIM_CODE} or
	 *                   {@link ExportFormat#ELK_GRAPH_JSON}. Asking for k.IM code
	 *                   currently produces an empty output.
	 * @return the provenance graph in the requested textual format.
	 * @throws KlabIllegalArgumentException if format isn't suitable to provenance
	 *                                      output
	 * @throws KlabRemoteException          if transfer fails for any reason
	 */
	String getProvenance(boolean simplified, ExportFormat format);

	/**
	 * Use in a fluent fashion to insert quality observations or objects into the
	 * context at the next submit(). Does not send anything to the server until
	 * submit() is called. Should only be used to insert observations with known,
	 * scalar values, which will be known to the engine before the main submit()
	 * observation is made. Estimates should be made including the chain of
	 * observations, and it is illegal to submit an estimate after with() is called.
	 * 
	 * @param concept an observable
	 * @param value   a value appropriate for the concept. If the concept is a
	 *                direct observable, the observable must be named and the value
	 *                must be a geometry (any errors are notified only by the server
	 *                after submit).
	 * @return this same context for chaining calls.
	 */
	Context with(Observable concept, Object value);

}