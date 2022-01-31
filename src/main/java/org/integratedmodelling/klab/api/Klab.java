package org.integratedmodelling.klab.api;

import java.io.File;
import java.util.concurrent.Future;

import org.integratedmodelling.klab.api.services.IConfigurationService;

/**
 * Main k.LAB client
 * 
 * @author Ferd
 *
 */
public class Klab {

	String token;
	
	private Klab() {
		// TODO authenticate
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
		return null;
	}

	/**
	 * Pass a certificate file explicitly.
	 * 
	 * @param engineUrl
	 * @param certificate
	 * @return
	 */
	public static Klab create(String engineUrl, File certificate) {
		return null;
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
	 * TEMPORARY needs to be the last step of a specification created by the
	 * singleton.
	 * 
	 * @return
	 */
	public Future<Context> submit(Estimate estimate) {
		return null;
	}
}
