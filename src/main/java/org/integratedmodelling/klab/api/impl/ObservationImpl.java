package org.integratedmodelling.klab.api.impl;

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
import org.integratedmodelling.klab.api.Context;
import org.integratedmodelling.klab.api.Klab.ExportFormat;
import org.integratedmodelling.klab.api.Observable;
import org.integratedmodelling.klab.api.Observation;
import org.integratedmodelling.klab.exceptions.KlabIOException;
import org.integratedmodelling.klab.exceptions.KlabIllegalArgumentException;
import org.integratedmodelling.klab.exceptions.KlabIllegalStateException;
import org.integratedmodelling.klab.exceptions.KlabRemoteException;
import org.integratedmodelling.klab.rest.ObservationReference;
import org.integratedmodelling.klab.rest.ObservationReference.ObservationType;
import org.integratedmodelling.klab.utils.Range;

public class ObservationImpl implements Observation {

	protected ObservationReference reference;
	protected Map<String, String> catalogIds = new HashMap<>();
	protected Map<String, ObservationImpl> catalog = new HashMap<>();
	protected Engine engine;

	public ObservationImpl(ObservationReference reference, Engine engine) {
		this.reference = reference;
		this.engine = engine;
	}

	@Override
	public Set<IKimConcept.Type> getSemantics() {
		return reference.getSemantics();
	}

	@Override
	public Observable getObservable() {
		return new Observable(reference.getObservable());
	}

	@Override
	public boolean is(Object type) {
		// TODO
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

	@Override
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

	@Override
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

	@Override
	public boolean export(Export target, ExportFormat format, OutputStream output, Object... parameters) {
		if (!format.isExportAllowed(target)) {
			throw new KlabIllegalArgumentException("export format is incompatible with target");
		}
		return engine.streamExport(this.reference.getId(), target, format, output, parameters);
	}

	@Override
	public Observation getObservation(String name) {
		String id = catalogIds.get(name);
		if (id != null) {
			ObservationImpl ret = catalog.get(id);
			if (ret == null) {
				ObservationReference ref = engine.getObservation(id);
				if (ref != null && ref.getId() != null) {
					ret = new ObservationImpl(ref, engine);
					catalog.put(id, ret);
				} else {
					throw new KlabRemoteException("server error retrieving observation " + id);
				}
			}
			return ret;
		}
		return null;
	}

	@Override
	public Context promote() {
		return null;
	}

	@Override
	public Range getDataRange() {
		if (this.reference == null || this.reference.getObservationType() != ObservationType.STATE) {
			throw new KlabIllegalStateException("getDataRange called on a non-state or null observation");
		}
		return Range.create(this.reference.getDataSummary().getMinValue(),
				this.reference.getDataSummary().getMaxValue());
	}

	@Override
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

    @Override
    public Object getAggregatedValue() {
        if (this.reference == null || this.reference.getObservationType() != ObservationType.STATE) {
            throw new KlabIllegalStateException("getDataRange called on a non-state or null observation");
        }
        // FIXME this is NOT the correct result
        return this.reference.getDataSummary().getMean();
    }

}
