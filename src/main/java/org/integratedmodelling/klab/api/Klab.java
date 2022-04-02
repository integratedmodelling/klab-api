package org.integratedmodelling.klab.api;

import java.util.Arrays;
import java.util.concurrent.Future;

import org.integratedmodelling.klab.api.data.IGeometry;
import org.integratedmodelling.klab.api.model.Context;
import org.integratedmodelling.klab.api.model.Estimate;
import org.integratedmodelling.klab.api.model.Observable;
import org.integratedmodelling.klab.api.runtime.ITicket.Type;
import org.integratedmodelling.klab.api.services.IConfigurationService;
import org.integratedmodelling.klab.api.utils.Engine;
import org.integratedmodelling.klab.common.GeometryBuilder;
import org.integratedmodelling.klab.exceptions.KlabIllegalArgumentException;
import org.integratedmodelling.klab.exceptions.KlabIllegalStateException;
import org.integratedmodelling.klab.rest.ContextRequest;

/**
 * Main k.LAB client. Instantiate one with your certificate/engine URL (or defaults) to start using
 * k.LAB within a Java application.
 * 
 * 
 * 
 * @author Ferd
 *
 */
public class Klab {

    Engine engine;
    String session;

    public static long POLLING_INTERVAL_MS = 2000l;

    private Klab(String engineUrl) {
        this.engine = new Engine(engineUrl);
        this.session = this.engine.authenticate();
    }

    private Klab(String engineUrl, String username, String password) {
        this.engine = new Engine(engineUrl);
        this.session = this.engine.authenticate(username, password);
    }

    public void disconnect() {
        if (this.engine.isOnline()) {
            this.engine.deauthenticate();
        }

    }

    /**
     * Authenticate with the hub in a certificate and open a session with the engine passed. Use the
     * certificate from the default location ($HOME/.klab/im.cert or the value of the
     * {@link IConfigurationService#KLAB_ENGINE_CERTIFICATE} system property.
     * 
     * @param engineUrl
     * @return
     */
    public static Klab create(String localEngineUrl, String username, String password) {
        return new Klab(localEngineUrl, username, password);
    }

    /**
     * Authenticate with a local engine in a certificate and open a session with the engine passed.
     * Won't require authentication but only works if the engine is local and authenticated.
     * 
     * @param engineUrl
     * @return
     */
    public static Klab create(String localEngineUrl) {
        return new Klab(localEngineUrl);
    }

    /**
     * Connect to local server with the default URL.
     * 
     * @return
     */
    public static Klab create() {
        return new Klab("http://127.0.0.1:8283/modeler");
    }

    public boolean isOnline() {
        return engine.isOnline();
    }

    /**
     * Call with a concept and geometry to retrieve an estimate of the cost of making the context
     * observation described. Add any observations to be made in the context as semantic types or
     * options.
     * 
     * @param contextType the type of the context. The Explorer sets that as earth:Region by
     *        default.
     * @param geometry the geometry for the context. Use {@link GeometryBuilder} to create fluently.
     * @param arguments pass observables for further observations to be made in the context (if
     *        passed, the task will finish after all have been computed). Strings will be
     *        interpreted as scenario names.
     * @return an estimate future; call get() to wait until the estimate is ready and retrieve it.
     */
    public Future<Estimate> estimate(Observable contextType, IGeometry geometry, Object... arguments) {

        ContextRequest request = new ContextRequest();
        request.setContextType(contextType.toString());
        request.setGeometry(geometry.encode());
        request.setEstimate(true);
        for (Object o : arguments) {
            if (o instanceof Observable) {
                request.getObservables().add(((Observable) o).toString());
            } else if (o instanceof String) {
                request.getScenarios().add((String) o);
            }
        }

        if (request.getGeometry() != null && request.getContextType() != null) {
            String ticket = engine.submitContext(request, this.session);
            if (ticket != null) {
                return new TicketHandler<Estimate>(engine, session, ticket);
            }
        }

        throw new KlabIllegalArgumentException(
                "Cannot build estimate request from arguments: " + Arrays.toString(arguments));
    }

    /**
     * Accept a previously obtained estimate and retrieve the correspondent context.
     * 
     * @param estimate
     * @return
     */
    public Future<Context> submit(Estimate estimate) {
        if (estimate.getTicketType() != Type.ContextEstimate) {
            throw new KlabIllegalArgumentException("the estimate passed is not a context estimate");
        }
        String ticket = engine.submitEstimate(estimate.getEstimateId(), this.session);
        if (ticket != null) {
            return new TicketHandler<Context>(engine, session, ticket);
        }

        throw new KlabIllegalStateException("estimate cannot be used");
    }

    /**
     * Call with a concept and geometry to create the context observation (accepting all costs) and
     * optionally further observations as semantic types or options.
     * 
     * @param contextType the type of the context. The Explorer sets that as earth:Region by
     *        default.
     * @param geometry the geometry for the context. Use {@link GeometryBuilder} to create fluently.
     * @param arguments pass semantic types for further observations to be made in the context (if
     *        passed, the task will finish after all have been computed). Strings will be
     *        interpreted as scenario names.
     * @return
     */
    public Future<Context> submit(Observable contextType, IGeometry geometry, Object... arguments) {
        ContextRequest request = new ContextRequest();
        request.setContextType(contextType.toString());
        request.setGeometry(geometry.encode());
        request.setEstimate(false);
        for (Object o : arguments) {
            if (o instanceof Observable) {
                request.getObservables().add(((Observable) o).toString());
            } else if (o instanceof String) {
                request.getScenarios().add((String) o);
            }
        }

        if (request.getGeometry() != null && request.getContextType() != null) {
            String ticket = engine.submitContext(request, this.session);
            if (ticket != null) {
                return new TicketHandler<Context>(engine, session, ticket);
            }
        }

        throw new KlabIllegalArgumentException(
                "Cannot build estimate request from arguments: " + Arrays.toString(arguments));
    }
}
