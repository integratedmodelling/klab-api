package org.integratedmodelling.klab.api;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Future;

import org.integratedmodelling.klab.api.API.PUBLIC.Export;
import org.integratedmodelling.klab.api.data.IGeometry;
import org.integratedmodelling.klab.api.impl.Engine;
import org.integratedmodelling.klab.api.impl.EstimateImpl;
import org.integratedmodelling.klab.api.impl.TicketHandler;
import org.integratedmodelling.klab.api.runtime.ITicket.Type;
import org.integratedmodelling.klab.common.GeometryBuilder;
import org.integratedmodelling.klab.exceptions.KlabIllegalArgumentException;
import org.integratedmodelling.klab.exceptions.KlabIllegalStateException;
import org.integratedmodelling.klab.rest.ContextRequest;

/**
 * The main k.LAB client class. A client object represents a user session in a local or remote
 * engine.
 * <p>
 * Instantiate a k.LAB client using {@link #create(String, String, String)} using your engine URL
 * and credentials, or use {@link #create()} or {@link #create(String)} to connect to a
 * <a href= "https://docs.integratedmodelling.org/klab/get_started/index.html">local engine</a>.
 * After creation, {@link #isOnline()} should always be called to ensure the connection was
 * successful. Then call ({@link #submit(Observable, IGeometry, Object...)}) or
 * ({@link #estimate(Observable, IGeometry, Object...)}) to create a context for further
 * observations, which are done by invoking similar methods directly on the resulting context. Using
 * <code>estimate</code> provides a pattern to obtain a cost estimate for each operation, which
 * depends on the size of the job and the user agreement.
 * <p>
 * In the case of a remote engine, the user must be explicitly authorized to the usage of k.LAB via
 * API: the regular self-certified user must request authorization through the k.LAB hub.
 * 
 * @author Ferdinando Villa, BC3/Ikerbasque
 *
 */
public class Klab implements Closeable {

    Engine engine;
    String session;

    /**
     * Each export format carries the actual media type for content negotiation and the export items
     * it's admitted for. Exporting may still generate errors if the specific observation it's
     * requested for does not admit the requested view.
     * 
     * @author Ferd
     *
     */
    public static enum ExportFormat {

        PNG_IMAGE("image/png", Export.DATA, Export.LEGEND, Export.VIEW), //
        GEOTIFF_RASTER("image/tiff", Export.DATA), //
        GEOJSON_FEATURES("application/json", Export.DATA), //
        JSON_CODE("application/json", Export.LEGEND, Export.STRUCTURE), //
        KDL_CODE("text/plain", Export.DATAFLOW), //
        KIM_CODE("text/plain", Export.PROVENANCE_FULL, Export.PROVENANCE_SIMPLIFIED), //
        ELK_GRAPH_JSON("application/json", Export.DATAFLOW, Export.PROVENANCE_FULL, Export.PROVENANCE_SIMPLIFIED), //
        CSV_TABLE("text/csv", Export.VIEW), //
        PDF_DOCUMENT("application/pdf", Export.REPORT), //
        EXCEL_TABLE("application/vnd.ms-excel", Export.VIEW), //
        WORD_DOCUMENT("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                Export.REPORT), BYTESTREAM("application/octet-stream", Export.DATA);

        String mediaType;
        Set<Export> allowedExports = EnumSet.noneOf(Export.class);

        ExportFormat(String mediaType, Export... allowed) {
            this.mediaType = mediaType;
            if (allowed != null) {
                for(Export export : allowed) {
                    allowedExports.add(export);
                }
            }
        }

        public String getMediaType() {
            return mediaType;
        }

        public boolean isExportAllowed(Export export) {
            return this.allowedExports.contains(export);
        }

        public boolean isText() {
            return "text/plain".equals(this.mediaType) || "application/json".equals(this.mediaType)
                    || "text/csv".equals(this.mediaType);
        }
    }

    /**
     * The type of data that can be extracted from an observation. Used with states; any other
     * observation will return NONE. This is the low-level data representation for bridging to other
     * APIs; the basic semantic types hold more information (accessible through getSemantics())
     * 
     * @author Ferd
     *
     */
    public static enum DataRepresentation {
        VOID, BOOLEAN, NUMERIC, CATEGORICAL
    }

    /**
     * The overall representation of space in an observation.
     * 
     * @author Ferd
     *
     */
    public static enum SpatialRepresentation {
        NONE, SHAPE, GRID, FEATURES
    }

    /**
     * The overall representation of time in an observation.
     * 
     * @author Ferd
     *
     */
    public static enum TemporalRepresentation {
        NONE, PERIOD, TIMESERIES
    }

    public static long POLLING_INTERVAL_MS = 2000l;

    private Klab(String engineUrl) {
        this.engine = new Engine(engineUrl);
        this.session = this.engine.authenticate();
    }

    private Klab(String engineUrl, String username, String password) {
        this.engine = new Engine(engineUrl);
        this.session = this.engine.authenticate(username, password);
    }

    /**
     * Authenticate with a remote engine and open a new user session. Call {@link #close()} to free
     * remote resources, or create the client in a try-with-resource block.
     * 
     * @param remoteEngineUrl
     * @param username
     * @param password
     * @return
     */
    public static Klab create(String remoteEngineUrl, String username, String password) {
        return new Klab(remoteEngineUrl, username, password);
    }

    /**
     * Authenticate with a local engine and open the default session. This does not require
     * authentication but only works if the engine is running on the local network and is properly
     * authenticated through a certificate. Calling {@link #close()} is good practice but won't
     * change the state of the session, which can be reconnected to if wished.
     * 
     * @param localEngineUrl
     * @return
     */
    public static Klab create(String localEngineUrl) {
        return new Klab(localEngineUrl);
    }

    /**
     * Connect to local server. Equivalent to {@link #create(String)} using the default local server
     * URL on port 8283.
     * 
     * @return
     */
    public static Klab create() {
        return new Klab("http://127.0.0.1:8283/modeler");
    }

    /**
     * Should always be called after creation to check on the engine status.
     * 
     * @return
     */
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
     *        interpreted as scenario URNs.
     * 
     * @return an estimate future; call get() to wait until the estimate is ready and retrieve it.
     */
    public Future<Estimate> estimate(Observable contextType, IGeometry geometry, Object... arguments) {

        ContextRequest request = new ContextRequest();
        request.setContextType(contextType.toString());
        request.setGeometry(geometry.encode());
        request.setEstimate(true);
        for(Object o : arguments) {
            if (o instanceof Observable) {
                request.getObservables().add(((Observable) o).toString());
            } else if (o instanceof String) {
                request.getScenarios().add((String) o);
            }
        }

        if (request.getGeometry() != null && request.getContextType() != null) {
            String ticket = engine.submitContext(request);
            if (ticket != null) {
                return new TicketHandler<Estimate>(engine, ticket, null);
            }
        }

        throw new KlabIllegalArgumentException("Cannot build estimate request from arguments: " + Arrays.toString(arguments));
    }

    public Future<Estimate> estimate(String urn, Object... arguments) {
        ContextRequest request = new ContextRequest();
        request.setUrn(urn);
        request.setEstimate(true);
        if (arguments != null) {
            for(Object o : arguments) {
                if (o instanceof Observable) {
                    request.getObservables().add(((Observable) o).toString());
                } else if (o instanceof String) {
                    request.getScenarios().add((String) o);
                }
            }
        }

        String ticket = engine.submitContext(request);
        if (ticket != null) {
            return new TicketHandler<Estimate>(engine, ticket, null);
        }

        throw new KlabIllegalArgumentException("Cannot build estimate request from arguments: " + Arrays.toString(arguments));

    }

    /**
     * Accept a previously obtained estimate and retrieve a task computing the correspondent
     * context.
     * 
     * @param estimate
     * @return
     */
    public Future<Context> submit(Estimate estimate) {

        if (((EstimateImpl) estimate).getTicketType() != Type.ContextEstimate) {
            throw new KlabIllegalArgumentException("the estimate passed is not a context estimate");
        }
        String ticket = engine.submitEstimate(((EstimateImpl) estimate).getEstimateId());
        if (ticket != null) {
            return new TicketHandler<Context>(engine, ticket, null);
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
     *        interpreted as scenario URNs.
     * @return
     */
    public Future<Context> submit(Observable contextType, IGeometry geometry, Object... arguments) {

        ContextRequest request = new ContextRequest();
        request.setContextType(contextType.toString());
        request.setGeometry(geometry.encode());
        request.setEstimate(false);
        for(Object o : arguments) {
            if (o instanceof Observable) {
                request.getObservables().add(((Observable) o).toString());
            } else if (o instanceof String) {
                request.getScenarios().add((String) o);
            }
        }

        if (request.getGeometry() != null && request.getContextType() != null) {
            String ticket = engine.submitContext(request);
            if (ticket != null) {
                return new TicketHandler<Context>(engine, ticket, null);
            }
        }

        throw new KlabIllegalArgumentException("Cannot build estimate request from arguments: " + Arrays.toString(arguments));
    }

    public Future<Context> submit(String urn, Object... arguments) {
        ContextRequest request = new ContextRequest();
        request.setUrn(urn);
        request.setEstimate(false);
        if (arguments != null) {
            for(Object o : arguments) {
                if (o instanceof Observable) {
                    request.getObservables().add(((Observable) o).toString());
                } else if (o instanceof String) {
                    request.getScenarios().add((String) o);
                }
            }
        }

        String ticket = engine.submitContext(request);
        if (ticket != null) {
            return new TicketHandler<Context>(engine, ticket, null);
        }

        throw new KlabIllegalArgumentException("Cannot build estimate request from arguments: " + Arrays.toString(arguments));
    }

    @Override
    public void close() throws IOException {
        if (this.engine.isOnline()) {
            this.engine.deauthenticate();
        }
    }
}
