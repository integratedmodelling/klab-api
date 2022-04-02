package org.integratedmodelling.klab.api.model;

import org.integratedmodelling.klab.api.runtime.ITicket;
import org.integratedmodelling.klab.api.runtime.ITicket.Type;

public class Estimate {

    private String estimateId;
    private double cost;
    private String currency;
    private ITicket.Type ticketType;

    public Estimate(String id, double cost, String currency, Type type) {
        this.estimateId = id;
        this.cost = cost;
        this.currency = currency;
        this.ticketType = type;
    }

    /**
     * The cost of the estimate, converted to the user currency returned by {@link #getCurrency()}.
     * 
     * @return
     */
    public double getCost() {
        return cost;
    }

    /**
     * The currency of the estimate. If the estimate is in raw k.LAB credits, this will return
     * "KLB".
     * 
     * @return
     */
    public String getCurrency() {
        return currency;
    }

    public String getEstimateId() {
        return estimateId;
    }

    public ITicket.Type getTicketType() {
        return ticketType;
    }

}
