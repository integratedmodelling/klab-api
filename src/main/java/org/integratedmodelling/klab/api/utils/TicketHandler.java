package org.integratedmodelling.klab.api.utils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Handler that uses the ticket API in k.LAB to provide access to a bean
 * computed asynchronously at the remote side.
 * 
 * @author Ferd
 *
 * @param <T> class of final result
 * @param <B> class of the bean to be converted into the T result
 */
public abstract class TicketHandler<T, B> implements Future<T> {

	/**
	 * Default polling interval is every 1.5 seconds.
	 */
	private long POLL_INTERVAL = 1500;

	private Engine engine;
	private String ticketId;
	private Class<B> beanClass;
	private AtomicReference<T> result = new AtomicReference<>();
	private boolean cancelled;

	public TicketHandler(Engine engine, String ticketId, Class<B> beanClass) {
		this.engine = engine;
		this.ticketId = ticketId;
		this.beanClass = beanClass;
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
			B bean = retrieveBean(engine);
			if (bean != null) {
				result.set(convertBean(bean));
				break;
			} else if (isCancelled()) {
				break;
			}
			Thread.sleep(POLL_INTERVAL);
		}
		return null;
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
			B bean = retrieveBean(engine);
			if (bean != null) {
				result.set(convertBean(bean));
				break;
			} else if (isCancelled()) {
				break;
			}
			Thread.sleep(POLL_INTERVAL);
			time += POLL_INTERVAL;
		}

		return result.get();
	}

	/**
	 * Convert the B bean into the T result
	 * 
	 * @param bean
	 * @return
	 */
	protected abstract T convertBean(B bean);

	/**
	 * Make an attempt to retrieve the bean from the remote service; if the task has
	 * not completed, return null. This will be called at regular intervals during
	 * polling if get() is called. If errors happen during polling, the
	 * implementation should call cancel(true).
	 * 
	 * @param engine
	 * @return
	 */
	protected abstract B retrieveBean(Engine engine);

}
