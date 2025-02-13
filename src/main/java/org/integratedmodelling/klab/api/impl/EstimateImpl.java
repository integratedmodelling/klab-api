package org.integratedmodelling.klab.api.impl;

import org.integratedmodelling.klab.api.Estimate;
import org.integratedmodelling.klab.api.Klab.ExportFormat;
import org.integratedmodelling.klab.api.runtime.ITicket;
import org.integratedmodelling.klab.api.runtime.ITicket.Type;

public class EstimateImpl implements Estimate {

    private String estimateId;
    private double cost;
    private boolean feasible;
    private String currency;
    private ITicket.Type ticketType;

    public EstimateImpl(String id, double cost, String currency, Type type, String feasible) {
        this.estimateId = id;
        this.cost = cost;
        this.currency = currency;
        this.ticketType = type;
        this.feasible = "true".equals(feasible);
    }

    /**
     * The cost of the estimate, converted to the user currency returned by
     * {@link #getCurrency()}.
     * 
     * @return
     */
    @Override
    public double getCost() {
        return cost;
    }

    /**
     * The currency of the estimate (ISO code). If the estimate is in raw k.LAB
     * credits, this will return "KLB".
     * 
     * @return
     */
    @Override
    public String getCurrency() {
        return currency;
    }

    public String getEstimateId() {
        return estimateId;
    }

    public ITicket.Type getTicketType() {
        return ticketType;
    }

    @Override
    public String getDataflow(ExportFormat format) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isFeasible() {
        return feasible;
    }

}
