package org.integratedmodelling.klab.api.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.integratedmodelling.klab.api.API.PUBLIC.Export;
import org.integratedmodelling.klab.api.Klab.ExportFormat;
import org.integratedmodelling.klab.api.utils.Engine;
import org.integratedmodelling.klab.exceptions.KlabIOException;
import org.integratedmodelling.klab.exceptions.KlabIllegalStateException;
import org.integratedmodelling.klab.rest.ObservationReference;
import org.integratedmodelling.klab.rest.ObservationReference.ObservationType;
import org.integratedmodelling.klab.utils.Range;

public class Observation {

	protected ObservationReference reference;
	protected Map<String, String> catalogIds = new HashMap<>();
	protected Map<String, Observation> catalog = new HashMap<>();
	protected String session;
	protected Engine engine;

	public Observation(ObservationReference reference, String session, Engine engine) {
		this.reference = reference;
		this.session = session;
		this.engine = engine;
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

	public boolean export(Export target, ExportFormat format, File file) {
		boolean ret = false;
		try (OutputStream stream = new FileOutputStream(file)) {
			ret = export(target, format, stream);
		} catch (FileNotFoundException e) {
			throw new KlabIllegalStateException(e.getMessage());
		} catch (IOException e) {
			throw new KlabIOException(e.getMessage());
		}
		return ret;
	}

	public boolean export(Export target, ExportFormat format, OutputStream output) {
		return false;
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
				ObservationReference ref = engine.getObservation(id, session);
				if (ref != null && ref.getId() != null) {
					ret = new Observation(ref, session, engine);
					catalog.put(id, ret);
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

	public Object getScalarValue() {
		String literalValue = reference.getLiteralValue();
		if (literalValue != null) {
			switch (reference.getValueType()) {
			case BOOLEAN:
				return Boolean.parseBoolean(literalValue);
			case NUMBER:
				return Double.parseDouble(literalValue);
			default:
				break;
			}
		}
		return literalValue;
	}

}
