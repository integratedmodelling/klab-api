package org.integratedmodelling.klab.api.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.integratedmodelling.klab.api.API;
import org.integratedmodelling.klab.api.Klab.ExportFormat;
import org.integratedmodelling.klab.exceptions.KlabIOException;
import org.integratedmodelling.klab.rest.ContextRequest;
import org.integratedmodelling.klab.rest.ObservationReference;
import org.integratedmodelling.klab.rest.ObservationRequest;
import org.integratedmodelling.klab.rest.PingResponse;
import org.integratedmodelling.klab.rest.TicketResponse;
import org.integratedmodelling.klab.rest.TicketResponse.Ticket;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
 * The engine REST client wrapping all the calls and bean types. Unirest handles
 * the basic communication.
 * 
 * @author Ferd
 *
 */
public class Engine implements API.PUBLIC {

	private String token;
	private String url;
	// change temporarily using 'with'
	private String acceptHeader = null;

	public Engine(String engineUrl) {
		this.url = engineUrl;
		while (this.url.endsWith("/")) {
			this.url = this.url.substring(0, this.url.length() - 1);
		}
	}

	private <T> T post(String endpoint, Object request, Class<? extends T> responseType, Object... pathVariables) {

		String mediaType = "application/json";
		if (this.acceptHeader != null) {
			mediaType = acceptHeader;
			this.acceptHeader = null;
		}

		if (pathVariables != null) {
			for (int i = 0; i < pathVariables.length; i++) {
				endpoint = endpoint.replace(pathVariables[i].toString(), pathVariables[++i].toString());
			}
		}

		return (T) Unirest.post(makeUrl(endpoint)).contentType("application/json").accept(mediaType)
				.header("Authorization", token).body(request).asObject(responseType).getBody();
	}

	private <T> T get(String endpoint, Class<? extends T> cls, Object... parameters) {

		String mediaType = "application/json";
		if (this.acceptHeader != null) {
			mediaType = acceptHeader;
			this.acceptHeader = null;
		}

		// TODO handle different responses if the Accept header has been modified.
		// Should pass a String class for text or an InputStream class for streamed
		// data.
		return (T) Unirest.get(makeUrl(endpoint, parameters)).accept(mediaType).header("Authorization", token)
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
	 * Use in fluent fashion to change the Accept: header to a different media type
	 * than application/json at the next request.
	 * 
	 * @param mediaType
	 * @return
	 */
	protected Engine accept(String mediaType) {
		this.acceptHeader = mediaType;
		return this;
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
		Map<?, ?> result = post(AUTHENTICATE_USER, request, Map.class);
		if (result.containsKey("session")) {
			// TODO check if we need to remember the user-bound authorization token
			this.token = result.get("session").toString();
		}
		return this.token;
	}

	/**
	 * Send the de-authentication request to clean up and exit gracefully. Always
	 * appreciated.
	 * 
	 * @return
	 */
	public boolean deauthenticate() {
		return Unirest.post(makeUrl(DEAUTHENTICATE_USER)).header("Authorization", token).asEmpty().isSuccess();
	}

	/**
	 * Local engine login. No auth necessary.
	 * 
	 * @return
	 */
	public String authenticate() {
		try {
			HttpResponse<PingResponse> request = Unirest.get(makeUrl(API.PING)).accept("application/json")
					.asObject(PingResponse.class);
			if (request.isSuccess()) {
				PingResponse response = request.getBody();
				if (response != null && response.getLocalSessionId() != null) {
					this.token = response.getLocalSessionId();
				}
			}
		} catch (Throwable t) {
			// no connection: just return null, isOnline() will return false
		}
		return this.token;
	}

	public boolean isOnline() {
		return this.token != null;
	}

	/**
	 * Submit context request, return ticket number or null in case of error
	 * 
	 * @param request
	 * @return
	 */
	public String submitContext(ContextRequest request) {
		TicketResponse.Ticket response = post(CREATE_CONTEXT, request, TicketResponse.Ticket.class);
		if (response != null) {
			return response.getId();
		}
		return null;
	}

	/**
	 * Submit context request, return ticket number or null in case of error
	 * 
	 * @param request
	 * @return
	 */
	public String submitObservation(ObservationRequest request) {
		TicketResponse.Ticket response = post(OBSERVE_IN_CONTEXT.replace(P_CONTEXT, request.getContextId()), request,
				TicketResponse.Ticket.class);
		if (response != null && response.getId() != null) {
			return response.getId();
		}
		return null;
	}

	public String submitEstimate(String estimateId) {
		Ticket response = get(SUBMIT_ESTIMATE.replace(P_ESTIMATE, estimateId), TicketResponse.Ticket.class);
		if (response != null && response.getId() != null) {
			return response.getId();
		}
		return null;
	}

	public Ticket getTicket(String ticketId) {
		Ticket ret = get(TICKET_INFO.replace(P_TICKET, ticketId), TicketResponse.Ticket.class);
		return (ret == null || ret.getId() == null) ? null : ret;
	}

	public ObservationReference getObservation(String artifactId) {
		ObservationReference ret = get(
				EXPORT_DATA.replace(P_EXPORT, Export.STRUCTURE.name().toLowerCase()).replace(P_OBSERVATION, artifactId),
				ObservationReference.class);
		return (ret == null || ret.getId() == null) ? null : ret;
	}

	public boolean streamExport(String observationId, Export target, ExportFormat format, final OutputStream output,
			Object... parameters) {

		String url = makeUrl(
				EXPORT_DATA.replace(P_EXPORT, target.name().toLowerCase()).replace(P_OBSERVATION, observationId),
				parameters);
		try {
			
			Unirest.get(url).accept(format.getMediaType()).header("Authorization", token)
					.thenConsume(response -> {
						try {
							response.getContent().transferTo(output);
						} catch (IOException e) {
							// uncheck
							throw new KlabIOException(e);
						}
					});
			
			return true;
					
		} catch (Throwable t) {
			// just return false
		}

		return false;
	}
}
