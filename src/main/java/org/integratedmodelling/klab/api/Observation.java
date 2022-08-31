package org.integratedmodelling.klab.api;

import java.io.File;
import java.io.OutputStream;
import java.util.Set;

import org.integratedmodelling.kim.api.IKimConcept;
import org.integratedmodelling.klab.api.API.PUBLIC.Export;
import org.integratedmodelling.klab.api.Klab.DataRepresentation;
import org.integratedmodelling.klab.api.Klab.ExportFormat;
import org.integratedmodelling.klab.api.Klab.SpatialRepresentation;
import org.integratedmodelling.klab.api.Klab.TemporalRepresentation;
import org.integratedmodelling.klab.exceptions.KlabIllegalStateException;
import org.integratedmodelling.klab.utils.Range;

public interface Observation {

	/**
	 * Return the set of fundamental semantic types for this observation. These
	 * don't get the full observable semantics (returned in string form by
	 * {@link #getObservable()}) which requires a connected engine/reasoner service
	 * to interpret, but is sufficient for basic inference and type checking.
	 * 
	 * @return a set of fundamental semantic types, one for the main observable with
	 *         potential qualifiers.
	 */
	Set<IKimConcept.Type> getSemantics();

	/**
	 * Return an {@link Observable} wrapping the string representation of the full
	 * observation semantics.
	 * 
	 * @return
	 */
	Observable getObservable();

	/**
	 * A general type checking method that can take a parameter of one of several
	 * enum types:
	 * <ul>
	 * <li>{@link DataRepresentation} to check the data type;</li>
	 * <li>{@link SpatialRepresentation} to check the type of spatial extent;</li>
	 * <li>{@link TemporalRepresentation} to check the temporal extent;</li>
	 * <li>{@link IKimConcept.Type} to check the fundamental semantics.</li>
	 * </ul>
	 * 
	 * @param type a type to compare the observation with
	 * @return true if the type describes the observation
	 */
	boolean is(Object type);

	/**
	 * Export a target to a file, which will be overwritten without warning if it
	 * exists.
	 * 
	 * @param target
	 * @param format
	 * @param file
	 * @param parameters
	 * @return
	 */
	boolean export(Export target, ExportFormat format, File file, Object... parameters);

	/**
	 * Export a target to a UTF-8 string. Only available if the target/format
	 * combination results in a textual output (json, csv or any of the k.LAB
	 * languages).
	 * 
	 * @param target
	 * @param format
	 * @return the string value, or null if anything has failed.
	 */
	String export(Export target, ExportFormat format);

	/**
	 * Export a target to an output stream, expected to be open and not closed on
	 * exit.
	 * 
	 * @param target
	 * @param format
	 * @param output
	 * @param parameters
	 * @return
	 */
	boolean export(Export target, ExportFormat format, OutputStream output, Object... parameters);

	/**
	 * Locate or retrieve the descriptor of an observation that has been made
	 * previously in the context.
	 * 
	 * @param name the name for the observed result. That corresponds to the formal
	 *             name of the observable requested.
	 * @return
	 */
	Observation getObservation(String name);

	/**
	 * If this observation is suitable to become a context (i.e. it is a direct
	 * observation: subject, event or relationship), promote it to one so that
	 * submit() can be called on it to make observations in its context.
	 * 
	 * @return a context built on this observation
	 * @throws KlabIllegalStateException if the observation can't be a context for
	 *                                   further observations.
	 */
	Context promote();

	/**
	 * The range of the data in a state observation. If the observation is not a
	 * numeric state, an exception is thrown. If it's a state but not numeric or
	 * number-backed , the result is undefined.
	 * 
	 * @return
	 */
	Range getDataRange();

	/**
	 * If the observation can be represented by a single scalar value, return it,
	 * otherwise return null. Observation that return non-null will either
	 * have multiplicity == 1 (no distribution in time or space) or variance == 0
	 * across all the distributed values.
	 * 
	 * @return
	 */
	Object getScalarValue();

	/**
	 * Return a single value that best aggregates the contents of the observation.
	 * Returns non-null only when the observation is a state. The specific
	 * aggregation depends on the semantics and reflects the context and unit of the
	 * observable, so for example a spatially distributed quality with extensive
	 * semantics (e.g. a mass) will return the sum unless the unit is a density
	 * (e.g. kg/m^2), in which case it will return the mean like always with
	 * intensive semantics. Categorical qualities will return the modal value.
	 * 
	 * @return
	 */
	Object getAggregatedValue();

	boolean isEmpty();

}