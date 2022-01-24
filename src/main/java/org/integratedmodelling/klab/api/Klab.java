package org.integratedmodelling.klab.api;

import java.io.File;
import java.util.concurrent.Future;

/**
 * Main k.LAB client 
 * 
 * @author Ferd
 *
 */
public class Klab {

	private Klab() {
		// TODO authenticate
	}

	/**
	 * Use the certificate from the default locations.
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
	public Future<Estimate> estimate() {
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
