package org.integratedmodelling.klab.api;

import java.util.concurrent.Future;

import org.integratedmodelling.klab.api.Klab.ExportFormat;
import org.integratedmodelling.klab.api.data.IGeometry;
import org.integratedmodelling.klab.exceptions.KlabIllegalArgumentException;
import org.integratedmodelling.klab.exceptions.KlabRemoteException;

public interface Context extends Observation {

	/**
	 * Call with a concept (and geometry if the observable is not a quality) to
	 * create an estimate of the cost associated with the observation. Will also
	 * include any further observation hierarchy created by calling the with()
	 * functions in the estimate returned. Computing the estimate may involve
	 * observations and inference, so a future is returned.
	 * 
	 * @return the future observation being computed on the backend.
	 */
	Future<Estimate> estimate(Observable observable, Object... arguments);

	/**
	 * Call with a concept (and geometry if the observable is not a quality) to
	 * create an observation in this context. Will also include any further
	 * observation hierarchy created by calling the with() functions.
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
	 * Use in a fluent fashion to insert quality observations into the context at
	 * the next submit(). Does not send anything to the server until submit() is
	 * called. Should only be used to insert observations with known, scalar values,
	 * which will be known to the engine before the main submit() observation is
	 * made. Estimates should be made including the chain of observations, and it is
	 * illegal to submit an estimate after with() is called.
	 * 
	 * @param concept
	 * @param value   a value appropriate for the concept
	 * @return this same context for chaining calls.
	 */
	Context with(Observable concept, Object value);

	/**
	 * Create a future sub-object which can be used as an inner-level context.
	 * Observations made on the resulting context will only concern the result
	 * object, which will appear as a child of the main context.
	 * <p>
	 * If an observation made through an instantiation needs to be used as a context
	 * for further observation, extract the observation and use
	 * {@link Observation#promote()} to turn it into a context.
	 * 
	 * @param subject  the observable for the sub-object. It must be explicitly
	 *                 named and be of a direct observable (i.e. not a quality or
	 *                 process), usually a subject.
	 * @param geometry a geometry for the resulting object. It does not need to
	 *                 agree in any way with the geometry of the main context.
	 * @return the future context.
	 */
	Future<Context> with(Observable subject, IGeometry geometry);

}