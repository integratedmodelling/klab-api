package org.integratedmodelling.klab.api.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.integratedmodelling.klab.api.API;
import org.integratedmodelling.klab.exceptions.KlabIOException;
import org.integratedmodelling.klab.rest.EngineAuthenticationResponse;
import org.integratedmodelling.klab.rest.RemoteUserAuthenticationRequest;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
 * The engine REST client wrapping all the calls and bean types. Unirest handles
 * the basic communication.
 * 
 * @author Ferd
 *
 */
public class Engine {

	private String token;
	private String url;

	public Engine(String engineUrl) {
		this.url = engineUrl;
		while (this.url.endsWith("/")) {
			this.url = this.url.substring(0, this.url.length() - 1);
		}
	}

	private <T> T post(String endpoint, Object request, Class<? extends T> responseType) {
		return (T) Unirest.post(makeUrl(endpoint)).accept("application/json").header("Authorization", token)
				.body(request).asObject(responseType).getBody();
	}

	private <T> T get(String endpoint, Class<? extends T> cls, Object... parameters) {
		return (T) Unirest.get(makeUrl(endpoint, parameters)).accept("application/json").header("Authorization", token)
				.asObject(cls).getBody();
	}

	private String makeUrl(String endpoint, Object... parameters) {
		String parms = "";
		if (parameters != null) {
			for (int i = 0; i < parameters.length; i++) {
				parms += (parms.isEmpty() ? "" : "&" + parameters[i] + "=" + parameters[++i]);
			}
		}
		return this.url + endpoint + (parms.isEmpty() ? "" : ("?" + parms));
	}

	/**
	 * Returns auth data and sets the token for future calls.
	 * 
	 * @param certificate
	 * @return
	 */
	public EngineAuthenticationResponse authenticate(File certificate) {
		RemoteUserAuthenticationRequest request;
		EngineAuthenticationResponse ret = null;
		try {
			HttpResponse<EngineAuthenticationResponse> response = Unirest.post(makeUrl(API.ENGINE.AUTHENTICATE))
					.accept("application/json")
					.body(Files.readString(certificate.toPath(), StandardCharsets.US_ASCII))
					.asObject(EngineAuthenticationResponse.class);

			if (response.isSuccess()) {
				ret = response.getBody();
				if (ret != null && ret.getUserData() != null) {
					this.token = ret.getUserData().getToken();
				}
			}

			return ret;
			
		} catch (IOException e) {
			throw new KlabIOException(e);
		}
	}

	public boolean isOnline() {
		return this.token != null;
	}

}
