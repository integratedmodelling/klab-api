package org.integratedmodelling.klab.api.test;

import java.util.concurrent.Future;

import org.integratedmodelling.klab.api.Klab;
import org.integratedmodelling.klab.api.model.Context;
import org.integratedmodelling.klab.api.model.Estimate;
import org.integratedmodelling.klab.api.model.Observable;
import org.integratedmodelling.klab.common.Geometry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SimpleObservation extends RemoteTestCase {

	/**
	 * A square piece of Tanzania
	 */
	private static String ruaha = "EPSG:4326 POLYGON((33.796 -7.086, 35.946 -7.086, 35.946 -9.41, 33.796 -9.41, 33.796 -7.086))";
	private Klab klab;
	
	@Before
	public void connect() {
		this.klab = Klab.create("https://integratedmodelling.org/modeler", username, password);
		assert klab.isOnline();
	}
	
	@After
	public void disconnect() {
		if (klab.isOnline()) {
			klab.disconnect();
		}
	}

	@Test
	public void testEstimate() throws Exception {
		
		/*
		 * pass a semantic type and a geometry
		 */
		Future<Estimate> estimateTask = klab.estimate(
				Observable.create("earth:Region"),
				Geometry.builder().grid(ruaha, "1 km").years(2010).build());
		
		/*
		 * block until the estimate is ready and retrieve it
		 */
		Estimate estimate = estimateTask.get();

		/*
		 * assess the cost (which will be 0) and if OK, submit the estimate
		 */
		if (estimate.getCost() < 100000) {

			Future<Context> contextTask = klab.submit(estimate);
			
			/**
			 * Retrieve the context and assert it's valid
			 */
			Context context = contextTask.get();
			
		}
	}

}
