package org.integratedmodelling.klab.api;

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
import org.integratedmodelling.klab.api.services.IConfigurationService;
import org.integratedmodelling.klab.common.GeometryBuilder;
import org.integratedmodelling.klab.exceptions.KlabIllegalArgumentException;
import org.integratedmodelling.klab.exceptions.KlabIllegalStateException;
import org.integratedmodelling.klab.rest.ContextRequest;

/**
 * Main k.LAB client. Also holds all types and interfaces for the observation
 * classes.
 * <p>
 * Instantiate a k.LAB client using {@link #create(String, String, String))}
 * using your engine URL and credentials, or use {@link #create()} or
 * {@link #create(String)} to connect to a <a href=
 * "https://docs.integratedmodelling.org/klab/get_started/index.html">local
 * engine</a>. Then submit ({@link #submit(Observable, IGeometry, Object...)})
 * or request estimates for
 * ({@link #estimate(Observable, IGeometry, Object...)}) queries .
 * 
 * 
 * @author Ferdinando Villa, BC3/Ikerbasque
 *
 */
public class Klab {

	Engine engine;
	String session;

	/**
	 * Each export format carries the actual media type for content negotiation and
	 * the export items it's admitted for. Exporting may still generate errors if
	 * the specific observation it's requested for does not admit the requested
	 * view.
	 * 
	 * @author Ferd
	 *
	 */
	public static enum ExportFormat {

		PNG_IMAGE("image/png", Export.DATA, Export.LEGEND, Export.VIEW), GEOTIFF_RASTER("image/tiff", Export.DATA),
		GEOJSON_FEATURES("application/json", Export.DATA),
		JSON_CODE("application/json", Export.LEGEND, Export.STRUCTURE), KDL_CODE("text/plain", Export.DATAFLOW),
		KIM_CODE("text/plain", Export.PROVENANCE_FULL, Export.PROVENANCE_SIMPLIFIED),
		ELK_GRAPH_JSON("application/json", Export.DATAFLOW, Export.PROVENANCE_FULL, Export.PROVENANCE_SIMPLIFIED),
		CSV_TABLE("text/csv", Export.VIEW), PDF_DOCUMENT("application/pdf", Export.REPORT),
		EXCEL_TABLE("application/vnd.ms-excel", Export.VIEW),
		WORD_DOCUMENT("application/vnd.openxmlformats-officedocument.wordprocessingml.document", Export.REPORT);

		String mediaType;
		Set<Export> allowedExports = EnumSet.noneOf(Export.class);

		ExportFormat(String mediaType, Export... allowed) {
			this.mediaType = mediaType;
			if (allowed != null) {
				for (Export export : allowed) {
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
	 * The type of data that can be extracted from an observation. Used with states;
	 * any other observation will return NONE. This is the low-level data
	 * representation for bridging to other APIs; the basic semantic types hold more
	 * information (accessible through getSemantics())
	 * 
	 * @author Ferd
	 *
	 */
	public static enum DataRepresentation {
		VOID, BOOLEAN, NUMERIC, CATEGORICAL
	}

	public static enum SpatialRepresentation {
		NONE, SHAPE, GRID, FEATURES
	}

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

	public void disconnect() {
		if (this.engine.isOnline()) {
			this.engine.deauthenticate();
		}

	}

	/**
	 * Authenticate with the hub in a certificate and open a session with the engine
	 * passed. Use the certificate from the default location ($HOME/.klab/im.cert or
	 * the value of the {@link IConfigurationService#KLAB_ENGINE_CERTIFICATE} system
	 * property.
	 * 
	 * @param engineUrl
	 * @return
	 */
	public static Klab create(String remoteEngineUrl, String username, String password) {
		return new Klab(remoteEngineUrl, username, password);
	}

	/**
	 * Authenticate with a local engine in a certificate and open a session with the
	 * engine passed. Won't require authentication but only works if the engine is
	 * local and authenticated.
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
	 * Call with a concept and geometry to retrieve an estimate of the cost of
	 * making the context observation described. Add any observations to be made in
	 * the context as semantic types or options.
	 * 
	 * @param contextType the type of the context. The Explorer sets that as
	 *                    earth:Region by default.
	 * @param geometry    the geometry for the context. Use {@link GeometryBuilder}
	 *                    to create fluently.
	 * @param arguments   pass observables for further observations to be made in
	 *                    the context (if passed, the task will finish after all
	 *                    have been computed). Strings will be interpreted as
	 *                    scenario URNs.
	 * @return an estimate future; call get() to wait until the estimate is ready
	 *         and retrieve it.
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
			String ticket = engine.submitContext(request);
			if (ticket != null) {
				return new TicketHandler<Estimate>(engine, ticket, null);
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

		if (((EstimateImpl) estimate).getTicketType() != Type.ContextEstimate) {
			throw new KlabIllegalArgumentException("the estimate passed is not a context estimate");
		}
		String ticket = engine.submitEstimate(((EstimateImpl)estimate).getEstimateId());
		if (ticket != null) {
			return new TicketHandler<Context>(engine, ticket, null);
		}

		throw new KlabIllegalStateException("estimate cannot be used");
	}

	/**
	 * Call with a concept and geometry to create the context observation (accepting
	 * all costs) and optionally further observations as semantic types or options.
	 * 
	 * @param contextType the type of the context. The Explorer sets that as
	 *                    earth:Region by default.
	 * @param geometry    the geometry for the context. Use {@link GeometryBuilder}
	 *                    to create fluently.
	 * @param arguments   pass semantic types for further observations to be made in
	 *                    the context (if passed, the task will finish after all
	 *                    have been computed). Strings will be interpreted as
	 *                    scenario URNs.
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
			String ticket = engine.submitContext(request);
			if (ticket != null) {
				return new TicketHandler<Context>(engine, ticket, null);
			}
		}

		throw new KlabIllegalArgumentException(
				"Cannot build estimate request from arguments: " + Arrays.toString(arguments));
	}
}
