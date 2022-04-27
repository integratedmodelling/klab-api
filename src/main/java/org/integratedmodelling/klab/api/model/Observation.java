package org.integratedmodelling.klab.api.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.integratedmodelling.kim.api.IKimConcept;
import org.integratedmodelling.klab.api.API.PUBLIC.Export;
import org.integratedmodelling.klab.api.Klab.DataRepresentation;
import org.integratedmodelling.klab.api.Klab.ExportFormat;
import org.integratedmodelling.klab.api.Klab.SpatialRepresentation;
import org.integratedmodelling.klab.api.Klab.TemporalRepresentation;
import org.integratedmodelling.klab.api.utils.Engine;
import org.integratedmodelling.klab.exceptions.KlabIOException;
import org.integratedmodelling.klab.exceptions.KlabIllegalArgumentException;
import org.integratedmodelling.klab.exceptions.KlabIllegalStateException;
import org.integratedmodelling.klab.exceptions.KlabRemoteException;
import org.integratedmodelling.klab.rest.ObservationReference;
import org.integratedmodelling.klab.rest.ObservationReference.ObservationType;
import org.integratedmodelling.klab.utils.Range;

public class Observation {

	protected ObservationReference reference;
	protected Map<String, String> catalogIds = new HashMap<>();
	protected Map<String, Observation> catalog = new HashMap<>();
	protected Engine engine;

	public Observation(ObservationReference reference, Engine engine) {
		this.reference = reference;
		this.engine = engine;
	}

	/**
	 * Return the set of fundamental semantic types for this observation. These
	 * don't get the observable semantics (returned in string form by
	 * {@link #getObservable()} which requires a connected engine/reasoner service
	 * to interpret, but are sufficient for basic inference and type checking.
	 * 
	 * @return a set of fundamental semantic types, one for the main observable with
	 *         potential qualifiers.
	 */
	public Set<IKimConcept.Type> getSemantics() {
		return reference.getSemantics();
	}

	/**
	 * Return the string representation of the full observation semantics.
	 * 
	 * @return
	 */
	public Observable getObservable() {
		return new Observable(reference.getObservable());
	}

	/**
	 * A general type checking method that can take a parameter of one of several types:
	 * <ul>
	 * <li>{@link DataRepresentation} to check the data type;</li>
	 * <li>{@link SpatialRepresentation} to check the type of spatial semantics;</li>
	 * <li>{@link TemporalRepresentation} to check the temporal extension;</li>
	 * <li>{@link IKimConcept.Type} to check the fundamental semantics;</li>
	 * </ul>
	 * @param a type to compare the observation with
	 * @return true if the type describes the observation
	 */
	public boolean is(Object type) {
		return false;
	}
	
	public void notifyObservation(String id) {
		for (String name : this.reference.getChildIds().keySet()) {
			if (id.equals(this.reference.getChildIds().get(name))) {
				catalogIds.put(name, id);
				getObservation(name);
				break;
			}
		}
	}

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
	public boolean export(Export target, ExportFormat format, File file, Object... parameters) {
		boolean ret = false;
		try (OutputStream stream = new FileOutputStream(file)) {
			ret = export(target, format, stream, parameters);
		} catch (FileNotFoundException e) {
			throw new KlabIllegalStateException(e.getMessage());
		} catch (IOException e) {
			throw new KlabIOException(e.getMessage());
		}
		return ret;
	}

	/**
	 * Export a target to a UTF-8 string. Only available if the format is a textual
	 * one (json, csv or any of the languages).
	 * 
	 * @param target
	 * @param format
	 * @return the string value, or null if anything has failed.
	 */
	public String export(Export target, ExportFormat format) {
		if (!format.isText()) {
			throw new KlabIllegalArgumentException(
					"illegal export format " + format + " for string export of " + target);
		}
		try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			boolean ok = export(target, format, output);
			if (ok) {
				return new String(output.toByteArray(), StandardCharsets.UTF_8);
			}
		} catch (IOException e) {
			// just return null
		}
		return null;
	}

	/**
	 * Export a target to an output stream, expected open and not closed at exit.
	 * 
	 * @param target
	 * @param format
	 * @param output
	 * @param parameters
	 * @return
	 */
	public boolean export(Export target, ExportFormat format, OutputStream output, Object... parameters) {
		if (!format.isExportAllowed(target)) {
			throw new KlabIllegalArgumentException("export format is incompatible with target");
		}
		return engine.streamExport(this.reference.getId(), target, format, output, parameters);
	}

	/**
	 * Locate or retrieve the descriptor of an observation that has been made
	 * previously in the context.
	 * 
	 * @param name the name for the observed result. That corresponds to the formal
	 *             name of the observable requested.
	 * @return
	 */
	public Observation getObservation(String name) {
		String id = catalogIds.get(name);
		if (id != null) {
			Observation ret = catalog.get(id);
			if (ret == null) {
				ObservationReference ref = engine.getObservation(id);
				if (ref != null && ref.getId() != null) {
					ret = new Observation(ref, engine);
					catalog.put(id, ret);
				} else {
					throw new KlabRemoteException("server error retrieving observation " + id);
				}
			}
			return ret;
		}
		return null;
	}

	/**
	 * If this observation is suitable to become a context (i.e. it is a direct
	 * observation: subject, event or relationship), promote it to one so that
	 * submit() can be called on it to make observations in its context.
	 * 
	 * @return a context built on this observation
	 * @throws KlabIllegalStateException if the observation can't be a context for
	 *                                   further observations.
	 */
	public Context promote() {
		return null;
	}

	/**
	 * The range of the data in a state observation. If the observation is not a
	 * state, an exception is thrown. If it's a state but not numeric, the result is
	 * undefined.
	 * 
	 * @return
	 */
	public Range getDataRange() {
		if (this.reference == null || this.reference.getObservationType() != ObservationType.STATE) {
			throw new KlabIllegalStateException("getDataRange called on a non-state or null observation");
		}
		return Range.create(this.reference.getDataSummary().getMinValue(),
				this.reference.getDataSummary().getMaxValue());
	}

	/**
	 * If the observation can be represented by a single scalar value, return it,
	 * otherwise return null.
	 * 
	 * @return
	 */
	public Object getScalarValue() {
		String literalValue = reference.getOverallValue();
		if (literalValue != null) {
			switch (reference.getValueType()) {
			case BOOLEAN:
				return Boolean.parseBoolean(literalValue);
			case NUMBER:
				return Double.parseDouble(literalValue);
			// TODO handle categories
			default:
				break;
			}
		}
		return literalValue;
	}

}
