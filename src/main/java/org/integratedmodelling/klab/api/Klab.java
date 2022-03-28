package org.integratedmodelling.klab.api;

import java.io.File;
import java.util.concurrent.Future;

import org.integratedmodelling.klab.api.model.Observation;
import org.integratedmodelling.klab.api.services.IConfigurationService;
import org.integratedmodelling.klab.api.utils.Engine;
import org.integratedmodelling.klab.rest.EngineAuthenticationResponse;

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
	EngineAuthenticationResponse authenticationData;

	private long POLLING_INTERVAL_MS = 2000l;

	private Klab(String engineUrl, File certificate) {
		this.engine = new Engine(engineUrl);
		this.authenticationData = this.engine.authenticate(certificate);
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
	public static Klab create(String engineUrl) {
		return new Klab(engineUrl, getDefaultCertificateFile());
	}

	/**
	 * 
	 * @return
	 */
	private static File getDefaultCertificateFile() {
		return new File(System.getProperty("user.home") + File.separator + ".klab" + File.separator + "klab.cert");
	}

	/**
	 * Pass a certificate file explicitly.
	 * 
	 * @param engineUrl
	 * @param certificate
	 * @return
	 */
	public static Klab create(String engineUrl, File certificate) {
		return new Klab(engineUrl, certificate);
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
		return null;
	}

	/**
	 * Call with a concept and geometry to create an observation (accepting all
	 * costs) or with an estimate to submit the estimate.
	 * 
	 * @return
	 */
	public Future<Observation> submit(Object... arguments) {
		return null;
	}
}
