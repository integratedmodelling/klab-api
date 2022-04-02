package org.integratedmodelling.klab.api.model;

import java.util.Arrays;
import java.util.concurrent.Future;

import org.integratedmodelling.klab.api.TicketHandler;
import org.integratedmodelling.klab.api.data.IGeometry;
import org.integratedmodelling.klab.api.runtime.ITicket.Type;
import org.integratedmodelling.klab.api.utils.Engine;
import org.integratedmodelling.klab.common.SemanticType;
import org.integratedmodelling.klab.exceptions.KlabIllegalArgumentException;
import org.integratedmodelling.klab.exceptions.KlabIllegalStateException;
import org.integratedmodelling.klab.rest.ObservationReference;
import org.integratedmodelling.klab.rest.ObservationRequest;

public class Context extends Observation {

    public Context(ObservationReference bean, Engine engine, String session) {
        super(bean, session, engine);
    }

    /**
     * Call with a concept and geometry to create an observation or with an estimate to submit the
     * estimate. Will also include any further observation hierarchy created by calling the with()
     * functions in the estimate returned.
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

        String ticket = engine.submitObservation(request, this.session);
        if (ticket != null) {
            return new TicketHandler<Estimate>(engine, session, ticket);
        }

        throw new KlabIllegalArgumentException(
                "Cannot build estimate request from arguments: " + Arrays.toString(arguments));
    }

    /**
     * Call with a concept and geometry to create an observation or with an estimate to submit the
     * estimate. Will also include any further observation hierarchy created by calling the with()
     * functions.
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

        String ticket = engine.submitObservation(request, this.session);
        if (ticket != null) {
            // TODO must update context bean when observation arrives!
            return new TicketHandler<Observation>(engine, session, ticket);
        }

        throw new KlabIllegalArgumentException(
                "Cannot build observation request from arguments: " + Arrays.toString(arguments));
    }

    public Future<Observation> submit(Estimate estimate) {
        if (estimate.getTicketType() != Type.ObservationEstimate) {
            throw new KlabIllegalArgumentException("the estimate passed is not a context estimate");
        }
        String ticket = engine.submitEstimate(estimate.getEstimateId(), this.session);
        if (ticket != null) {
            // TODO the handler must update the context catalog when the observation arrives
            return new TicketHandler<Observation>(engine, session, ticket);
        }

        throw new KlabIllegalStateException("estimate cannot be used");

    }

    /**
     * Use in a fluent fashion to insert quality observations into the context at the next submit().
     * Does not have any effect before submit() is called.
     * 
     * @param concept
     * @param value a value appropriate for the concept
     * @return this same context for chaining calls.
     */
    public Context with(Observable concept, Object value) {
        // TODO
        return this;
    }

    /**
     * Create a sub-object for the next submit. Any {@link #with(SemanticType, Object)} or
     * {@link #with(SemanticType, IGeometry)} on the resulting context will apply to the object.
     * 
     * @param subject
     * @param geometry
     * @return
     */
    public Context with(Observable subject, IGeometry geometry) {
        // TODO
        return null;
    }

}
