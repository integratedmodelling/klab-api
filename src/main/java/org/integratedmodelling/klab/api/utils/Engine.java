package org.integratedmodelling.klab.api.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.integratedmodelling.klab.api.API;

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
		return (T) Unirest.post(makeUrl(endpoint)).contentType("application/json").accept("application/json")
				.header("Authorization", token).body(request).asObject(responseType).getBody();
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
	 * Username-Password authentication for remote engine
	 * 
	 * @param username
	 * @param password
	 * @return the ID of the user session
	 */
	public String authenticate(String username, String password) {

		Map<String, String> request = new HashMap<>();
		request.put("username", username);
		request.put("password", password);
		Map<?, ?> result = post(API.HUB.AUTHENTICATE_USER, request, Map.class);
		if (result.containsKey("authorization")) {
			this.token = result.get("authorization").toString();
		}
		return result.get("session").toString();
	}

	/**
	 * Local engine login. No auth necessary.
	 * 
	 * @return
	 */
	public String authenticate() {
		return null;
//		// request for a remote engine; local just ping and connect
//		RemoteUserAuthenticationRequest request;
//		// response from a local engine; no bean for remote
//		EngineAuthenticationResponse ret = null;
//		
//		try {
//			HttpResponse<EngineAuthenticationResponse> response = Unirest.post(makeUrl(API.ENGINE.AUTHENTICATE))
//					.accept("application/json")
//					.body(Files.readString(certificate.toPath(), StandardCharsets.US_ASCII))
//					.asObject(EngineAuthenticationResponse.class);
//
//			if (response.isSuccess()) {
//				ret = response.getBody();
//				if (ret != null && ret.getUserData() != null) {
//					this.token = ret.getUserData().getToken();
//				}
//			}
//
//			return ret;
//			
//		} catch (IOException e) {
//			throw new KlabIOException(e);
//		}
	}

	public boolean isOnline() {
		return this.token != null;
	}

}
