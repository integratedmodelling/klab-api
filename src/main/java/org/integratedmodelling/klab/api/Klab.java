package org.integratedmodelling.klab.api;

import java.util.Arrays;
import java.util.concurrent.Future;

import org.integratedmodelling.klab.api.data.IGeometry;
import org.integratedmodelling.klab.api.services.IConfigurationService;
import org.integratedmodelling.klab.api.utils.Engine;
import org.integratedmodelling.klab.api.utils.TicketHandler;
import org.integratedmodelling.klab.common.SemanticType;
import org.integratedmodelling.klab.exceptions.KlabIllegalArgumentException;
import org.integratedmodelling.klab.rest.ContextRequest;
import org.integratedmodelling.klab.rest.ObservationReference;
import org.integratedmodelling.klab.rest.TicketResponse;

/**
 * Main k.LAB client. Instantiate one with your certificate/engine URL (or
 * defaults) to start using k.LAB within a Java application.
 * 
 * 
 * 
 * @author Ferd
 *
 */
public class Klab {

	Engine engine;
	String session;

	private long POLLING_INTERVAL_MS = 2000l;

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
	public static Klab create(String localEngineUrl, String username, String password) {
		return new Klab(localEngineUrl, username, password);
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
	 * TEMPORARY needs to be the last step of a specification created by the
	 * singleton.
	 * 
	 * @return
	 */
	public Future<Estimate> estimate(Object... arguments) {
		if (arguments != null) {
			ContextRequest request = new ContextRequest();
			request.setEstimate(true);
			for (Object o : arguments) {
				if (o instanceof IGeometry) {
					request.setGeometry(((IGeometry) o).encode());
				} else if (o instanceof SemanticType) {
					request.setUrn(((SemanticType) o).toString());
				} else if (o instanceof String) {
					if (request.getUrn() != null) {
						request.getScenarios().add((String) o);
					} else {
						request.setUrn((String) o);
					}
				}
			}

			if (request.getGeometry() != null && request.getUrn() != null) {
				String ticket = engine.submitContext(request, this.session);
				if (ticket != null) {
					// TODO
					return null;
				}
			}
		}
		throw new KlabIllegalArgumentException(
				"Cannot build estimate request from arguments: " + Arrays.toString(arguments));
	}

	/**
	 * Call with a concept and geometry to create an observation (accepting all
	 * costs) or with an estimate to submit the estimate.
	 * 
	 * @return
	 */
	public Future<Context> submit(Object... arguments) {
		if (arguments != null) {
			ContextRequest request = new ContextRequest();
			for (Object o : arguments) {
				if (o instanceof IGeometry) {
					request.setGeometry(((IGeometry) o).encode());
				} else if (o instanceof SemanticType) {
					request.setUrn(((SemanticType) o).toString());
				} else if (o instanceof String) {
					if (request.getUrn() != null) {
						request.getScenarios().add((String) o);
					} else {
						request.setUrn((String) o);
					}
				}
			}

			if (request.getGeometry() != null && request.getUrn() != null) {
				String ticket = engine.submitContext(request, this.session);
				if (ticket != null) {
					return new TicketHandler<Context, ObservationReference>(engine, session, ticket,
							ObservationReference.class) {

						@Override
						protected Context convertBean(ObservationReference bean) {
							return new Context(bean, engine, session);
						}

						@Override
						protected ObservationReference retrieveBean(Engine engine, String artifactId) {
							return null;
						}
					};
				}
			}
		}
		throw new KlabIllegalArgumentException(
				"Cannot build estimate request from arguments: " + Arrays.toString(arguments));
	}

}
