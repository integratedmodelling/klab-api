package org.integratedmodelling.klab.api;

import org.integratedmodelling.klab.api.Klab.ExportFormat;
import org.integratedmodelling.klab.exceptions.KlabIllegalArgumentException;
import org.integratedmodelling.klab.exceptions.KlabRemoteException;

public interface Estimate {

	/**
	 * If true, the observation is deemed feasible because a dataflow can be built
	 * to make it. This does not guarantee that the eventual observation will
	 * complete without error, but if false it does guarantee that it won't
	 * complete. No costs will ever be associated with observations that do not
	 * complete.
	 * 
	 * @return
	 */
	boolean isFeasible();

	/**
	 * The cost of the estimate, converted to the user currency returned by
	 * {@link #getCurrency()}.
	 * 
	 * @return
	 */
	double getCost();

	/**
	 * The currency of the estimate. If the estimate is in raw k.LAB credits, this
	 * will return "KLB".
	 * 
	 * @return
	 */
	String getCurrency();

	/**
	 * Retrieve the prospective dataflow to compute the estimated observation in the
	 * passed format. See {@link Context#getDataflow(ExportFormat)} for details on
	 * the format. If the estimate is submitted, the actual dataflow isn't
	 * guaranteed to be exactly the same, but the cost will be the same.
	 * 
	 * @param format only admits {@link ExportFormat#KDL_CODE} or
	 *               {@link ExportFormat#ELK_GRAPH_JSON}
	 * @return the dataflow code in the requested textual format.
	 * @throws KlabIllegalArgumentException if format isn't suitable to dataflow
	 *                                      output
	 * @throws KlabRemoteException          if transfer fails for any reason
	 */
	String getDataflow(ExportFormat format);

}