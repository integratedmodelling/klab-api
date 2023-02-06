package org.integratedmodelling.klab.api.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.integratedmodelling.klab.api.Klab;
import org.integratedmodelling.klab.api.runtime.ITicket.Status;
import org.integratedmodelling.klab.exceptions.KlabInternalErrorException;
import org.integratedmodelling.klab.rest.ObservationReference;
import org.integratedmodelling.klab.rest.TicketResponse;
import org.integratedmodelling.klab.rest.TicketResponse.Ticket;

/**
 * Handler that uses the ticket API in k.LAB to provide access to a bean
 * computed asynchronously at the remote side.
 * 
 * @author Ferd
 *
 * @param <T> class of final result
 */
public class TicketHandler<T> implements Future<T> {

	private Engine engine;
	private String ticketId;
	private ContextImpl context;
	private AtomicReference<T> result = new AtomicReference<>();
	private boolean cancelled;

	public TicketHandler(Engine engine, String ticketId, ContextImpl context) {
		this.engine = engine;
		this.ticketId = ticketId;
		this.context = context;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		// TODO call virtual to cancel the job
		this.cancelled = true;
		return false;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public boolean isDone() {
		return result.get() != null;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		if (isCancelled()) {
			return null;
		}
		while (result.get() == null) {
			T bean = poll(engine);
			if (bean != null) {
				result.set(bean);
				break;
			} else if (isCancelled()) {
				break;
			}
			Thread.sleep(Klab.POLLING_INTERVAL_MS);
		}
		return result.get();
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {

		if (isCancelled()) {
			return null;
		}

		long time = 0;
		long tout = unit.convert(timeout, TimeUnit.MILLISECONDS);
		while (result.get() == null) {
			if (time > tout) {
				break;
			}
			T bean = poll(engine);
			if (bean != null) {
				result.set(bean);
				break;
			} else if (isCancelled()) {
				break;
			}
			Thread.sleep(Klab.POLLING_INTERVAL_MS);
			time += Klab.POLLING_INTERVAL_MS;
		}

		return result.get();
	}

	private T poll(Engine engine) {
		TicketResponse.Ticket ticket = engine.getTicket(ticketId);
		if (ticket == null || ticket.getStatus() == Status.ERROR || ticket.getId() == null) {
			cancel(true);
			return null;
		}
		if (ticket.getStatus() == Status.RESOLVED) {
			return processTicket(ticket);
		}
		return null;
	}

	protected T processTicket(Ticket ticket) {

		switch (ticket.getType()) {
		case ContextEstimate:
		case ObservationEstimate:
			return makeEstimate(ticket);
		case ContextObservation:
			return makeContext(ticket);
		case ObservationInContext:
			return makeObservation(ticket);
		default:
			break;
		}

		throw new KlabInternalErrorException("unexpected ticket type: " + ticket.getType());
	}

	@SuppressWarnings("unchecked")
	private T makeObservation(Ticket ticket) {
		if (ticket.getData().containsKey("artifacts")) {
			for (String oid : ticket.getData().get("artifacts").split(",")) {
				ObservationReference bean = engine.getObservation(oid);
				ObservationImpl ret = new ObservationImpl(bean, engine);
				if (context != null && ret != null && ret.reference != null) {
					context.updateWith(ret);
				}
				return (T) ret;
			}
		} else {
		    // empty result
            return (T) (new ObservationImpl(null, engine));
        }
		return null;
	}

	@SuppressWarnings("unchecked")
	private T makeContext(Ticket ticket) {
		ObservationReference bean = engine.getObservation(ticket.getData().get("context"));
		ContextImpl context = new ContextImpl(bean, engine);
		if (ticket.getData().containsKey("artifacts")) {
			for (String oid : ticket.getData().get("artifacts").split(",")) {
				context.notifyObservation(oid);
			}
		}
		return (T) context;
	}

	@SuppressWarnings("unchecked")
	private T makeEstimate(Ticket ticket) {
		return (T) new EstimateImpl(ticket.getData().get("estimate"), Double.parseDouble(ticket.getData().get("cost")),
				ticket.getData().get("currency"), ticket.getType(), ticket.getData().get("feasible"));
	}

}
