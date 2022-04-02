package org.integratedmodelling.klab.api.model;

import java.util.HashMap;
import java.util.Map;

import org.integratedmodelling.klab.api.utils.Engine;
import org.integratedmodelling.klab.rest.ObservationReference;

public class Observation {

    protected ObservationReference reference;
    protected Map<String, String> catalogIds = new HashMap<>();
    protected Map<String, Observation> catalog = new HashMap<>();
    protected String session;
    protected Engine engine;

    protected Observation(ObservationReference reference, String session, Engine engine) {
        this.reference = reference;
        this.session = session;
        this.engine = engine;
    }

    public void notifyObservation(String id) {
        for (String name : this.reference.getChildIds().keySet()) {
            if (id.equals(this.reference.getChildIds().get(name))) {
                catalogIds.put(name, id);
                getObservation(name);
                break;
            }
        }
    }
    
    /**
     * Locate or retrieve the descriptor of an observation that has been made previously in the
     * context.
     * 
     * @param name the name for the observed result. That corresponds to the formal name of the
     *        observable requested.
     * @return
     */
    public Observation getObservation(String name) {
        String id = catalogIds.get(name);
        if (id != null) {
            Observation ret = catalog.get(id);
            if (ret == null) {
                ObservationReference ref = engine.getObservation(id, session);
                if (ref != null && ref.getId() != null) {
                    ret = new Observation(ref, session, engine);
                    catalog.put(id, ret);
                }
            }
            return ret;
        }
        return null;
    }

}
