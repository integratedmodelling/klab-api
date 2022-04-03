package org.integratedmodelling.klab.api.utils;

import java.util.HashMap;
import java.util.Map;

import org.integratedmodelling.klab.api.API;
import org.integratedmodelling.klab.rest.ContextRequest;
import org.integratedmodelling.klab.rest.ObservationReference;
import org.integratedmodelling.klab.rest.ObservationRequest;
import org.integratedmodelling.klab.rest.PingResponse;
import org.integratedmodelling.klab.rest.TicketResponse;
import org.integratedmodelling.klab.rest.TicketResponse.Ticket;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
 * The engine REST client wrapping all the calls and bean types. Unirest handles the basic
 * communication.
 * 
 * @author Ferd
 *
 */
public class Engine implements API.PUBLIC {

    private String token;
    private String url;

    public Engine(String engineUrl) {
        this.url = engineUrl;
        while(this.url.endsWith("/")) {
            this.url = this.url.substring(0, this.url.length() - 1);
        }
    }

    private <T> T post(String endpoint, Object request, Class<? extends T> responseType,
            Object... pathVariables) {
        if (pathVariables != null) {
            for (int i = 0; i < pathVariables.length; i++) {
                endpoint = endpoint.replace(pathVariables[i].toString(), pathVariables[++i].toString());
            }
        }
        return (T) Unirest.post(makeUrl(endpoint)).contentType("application/json").accept("application/json")
                .header("Authorization", token).body(request).asObject(responseType).getBody();
    }

    private <T> T get(String endpoint, Class<? extends T> cls, Object... parameters) {
        return (T) Unirest.get(makeUrl(endpoint, parameters)).accept("application/json")
                .header("Authorization", token)
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
        Map<?, ?> result = post(AUTHENTICATE_USER, request, Map.class);
        if (result.containsKey("authorization")) {
            this.token = result.get("authorization").toString();
        }
        return result.get("session").toString();
    }

    /**
     * Send the de-authentication request to clean up and exit gracefully. Always appreciated.
     * 
     * @return
     */
    public boolean deauthenticate() {
        return Unirest.post(makeUrl(DEAUTHENTICATE_USER)).header("Authorization", token).asEmpty()
                .isSuccess();
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
    public String submitContext(ContextRequest request, String sessionId) {
        TicketResponse.Ticket response = post(CREATE_CONTEXT, request, TicketResponse.Ticket.class, P_SESSION,
                sessionId);
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
    public String submitObservation(ObservationRequest request, String sessionId) {
        TicketResponse.Ticket response = post(
                OBSERVE_IN_CONTEXT.replace(P_SESSION, sessionId).replace(P_CONTEXT, request.getContextId()),
                request, TicketResponse.Ticket.class);
        if (response != null && response.getId() != null) {
            return response.getId();
        }
        return null;
    }

    public String submitEstimate(String estimateId, String sessionId) {
        Ticket response = get(SUBMIT_ESTIMATE.replace(P_SESSION, sessionId).replace(P_ESTIMATE, estimateId),
                TicketResponse.Ticket.class);
        if (response != null && response.getId() != null) {
            return response.getId();
        }
        return null;
    }

    public Ticket getTicket(String ticketId, String sessionId) {
        Ticket ret = get(TICKET_INFO.replace(P_SESSION, sessionId).replace(P_TICKET, ticketId),
                TicketResponse.Ticket.class);
        return (ret == null || ret.getId() == null) ? null : ret;
    }

    public ObservationReference getObservation(String artifactId, String sessionId) {
        ObservationReference ret = get(
                RETRIEVE_OBSERVATION_DESCRIPTOR.replace(P_SESSION, sessionId).replace(P_OBSERVATION,
                        artifactId),
                ObservationReference.class);
        return (ret == null || ret.getId() == null) ? null : ret;
    }
}
