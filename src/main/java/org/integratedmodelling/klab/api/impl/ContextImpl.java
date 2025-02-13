package org.integratedmodelling.klab.api.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.integratedmodelling.klab.api.API.PUBLIC.Export;
import org.integratedmodelling.klab.api.Context;
import org.integratedmodelling.klab.api.Estimate;
import org.integratedmodelling.klab.api.Klab.ExportFormat;
import org.integratedmodelling.klab.api.Observable;
import org.integratedmodelling.klab.api.Observation;
import org.integratedmodelling.klab.api.data.IGeometry;
import org.integratedmodelling.klab.api.runtime.ITicket.Type;
import org.integratedmodelling.klab.exceptions.KlabIOException;
import org.integratedmodelling.klab.exceptions.KlabIllegalArgumentException;
import org.integratedmodelling.klab.exceptions.KlabIllegalStateException;
import org.integratedmodelling.klab.rest.ObservationReference;
import org.integratedmodelling.klab.rest.ObservationRequest;
import org.integratedmodelling.klab.utils.Pair;

public class ContextImpl extends ObservationImpl implements Context {

    /*
     * if defined, these are submitted at the next submit() before the main
     * observable is queried, then cleared.
     */
    List<Pair<Observable, Object>> injectedStates = new ArrayList<>();
    List<Pair<Observable, IGeometry>> injectedObjects = new ArrayList<>();

    public ContextImpl(ObservationReference bean, Engine engine) {
        super(bean, engine);
    }

    @Override
    public Future<Estimate> estimate(Observable observable, Object... arguments) {

        ObservationRequest request = new ObservationRequest();
        request.setContextId(this.reference.getId());
        request.setEstimate(true);
        request.setUrn(observable.toString());

        for(Pair<Observable, Object> state : injectedStates) {
            request.getStates().put(state.getFirst().toString(), state.getSecond().toString());
        }
        for(Pair<Observable, IGeometry> object : injectedObjects) {
            request.getStates().put(object.getFirst().toString(), object.getSecond().encode());
        }

        injectedStates.clear();
        injectedObjects.clear();

        for(Object o : arguments) {
            if (o instanceof String) {
                request.getScenarios().add((String) o);
            }
        }

        String ticket = engine.submitObservation(request);
        if (ticket != null) {
            return new TicketHandler<Estimate>(engine, ticket, this);
        }

        throw new KlabIllegalArgumentException("Cannot build estimate request from arguments: " + Arrays.toString(arguments));
    }

    @Override
    public Future<Observation> submit(Observable observable, Object... arguments) {

        ObservationRequest request = new ObservationRequest();
        request.setContextId(this.reference.getId());
        request.setEstimate(false);
        request.setUrn(observable.toString());

        for(Pair<Observable, Object> state : injectedStates) {
            request.getStates().put(state.getFirst().toString(), state.getSecond().toString());
        }
        for(Pair<Observable, IGeometry> object : injectedObjects) {
            request.getStates().put(object.getFirst().toString(), object.getSecond().encode());
        }

        injectedStates.clear();
        injectedObjects.clear();

        for(Object o : arguments) {
            if (o instanceof String) {
                request.getScenarios().add((String) o);
            }
        }

        String ticket = engine.submitObservation(request);
        if (ticket != null) {
            // TODO updates the context bean when observation arrives!
            return new TicketHandler<Observation>(engine, ticket, this);
        }

        throw new KlabIllegalArgumentException("Cannot build observation request from arguments: " + Arrays.toString(arguments));
    }

    @Override
    public Future<Observation> submit(Estimate estimate) {

        if (((EstimateImpl) estimate).getTicketType() != Type.ObservationEstimate) {
            throw new KlabIllegalArgumentException("the estimate passed is not a context estimate");
        }
        String ticket = engine.submitEstimate(((EstimateImpl) estimate).getEstimateId());
        if (ticket != null) {
            // the handler updates the context catalog when the observation arrives
            return new TicketHandler<Observation>(engine, ticket, this);
        }

        throw new KlabIllegalStateException("estimate cannot be used");

    }

    @Override
    public String getDataflow(ExportFormat format) {

        if (format != ExportFormat.ELK_GRAPH_JSON && format != ExportFormat.KDL_CODE) {
            throw new KlabIllegalArgumentException("cannot export a dataflow to " + format);
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (engine.streamExport(reference.getId(), Export.DATAFLOW, format, output)) {
                return output.toString();
            }
        } catch (IOException e) {
            throw new KlabIOException(e);
        }

        return null;
    }

    @Override
    public String getProvenance(boolean simplified, ExportFormat format) {

        if (format != ExportFormat.ELK_GRAPH_JSON && format != ExportFormat.KIM_CODE) {
            throw new KlabIllegalArgumentException("cannot export the provenance graph to " + format);
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (engine.streamExport(reference.getId(), simplified ? Export.PROVENANCE_SIMPLIFIED : Export.PROVENANCE_FULL, format,
                    output)) {
                return output.toString();
            }
        } catch (IOException e) {
            throw new KlabIOException(e);
        }

        return null;
    }

    @Override
    public Context with(Observable concept, Object value) {

        if (value instanceof IGeometry) {
            if (concept.getName() == null) {
                throw new KlabIllegalStateException("observables that create objects must be given an explicit name");
            }
            this.injectedObjects.add(new Pair<>(concept, (IGeometry) value));
        } else {
            this.injectedStates.add(new Pair<>(concept, value));
        }

        return this;
    }

    /*
     * Called after an observation to update the context data and ensure the context
     * has the new observation in its catalog.
     * 
     * @Non-API should be package private
     * 
     * @param ret
     */
    public void updateWith(ObservationImpl ret) {
        this.reference = engine.getObservation(reference.getId());
        for(String name : this.reference.getChildIds().keySet()) {
            if (ret.reference.getId().equals(this.reference.getChildIds().get(name))) {
                catalogIds.put(name, ret.reference.getId());
                catalog.put(ret.reference.getId(), ret);
                break;
            }
        }

    }

}
