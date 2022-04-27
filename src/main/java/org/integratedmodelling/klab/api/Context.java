package org.integratedmodelling.klab.api;

import java.util.concurrent.Future;

import org.integratedmodelling.klab.api.Klab.ExportFormat;
import org.integratedmodelling.klab.api.data.IGeometry;
import org.integratedmodelling.klab.common.SemanticType;
import org.integratedmodelling.klab.exceptions.KlabIllegalArgumentException;
import org.integratedmodelling.klab.exceptions.KlabRemoteException;

public interface Context extends Observation {

	/**
	 * Call with a concept and geometry to create an observation or with an estimate
	 * to submit the estimate. Will also include any further observation hierarchy
	 * created by calling the with() functions in the estimate returned.
	 * 
	 * @return
	 */
	Future<Estimate> estimate(Observable observable, Object... arguments);

	/**
	 * Call with a concept and geometry to create an observation or with an estimate
	 * to submit the estimate. Will also include any further observation hierarchy
	 * created by calling the with() functions.
	 * 
	 * @return
	 */
	Future<Observation> submit(Observable observable, Object... arguments);

	Future<Observation> submit(Estimate estimate);

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
	 *                   information stored with remote resources.
	 * @param format     only admits {@link ExportFormat#KIM_CODE} or
	 *                   {@link ExportFormat#ELK_GRAPH_JSON}. Asking for k.IM code
	 *                   currently produces an empty output.
	 * @return
	 * @throws KlabIllegalArgumentException if format isn't suitable to provenance
	 *                                      output
	 * @throws KlabRemoteException          if transfer fails for any reason
	 */
	String getProvenance(boolean simplified, ExportFormat format);

	/**
	 * Use in a fluent fashion to insert quality observations into the context at
	 * the next submit(). Does not have any effect before submit() is called.
	 * 
	 * @param concept
	 * @param value   a value appropriate for the concept
	 * @return this same context for chaining calls.
	 */
	Context with(Observable concept, Object value);

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
	Future<Context> with(Observable subject, IGeometry geometry);

}