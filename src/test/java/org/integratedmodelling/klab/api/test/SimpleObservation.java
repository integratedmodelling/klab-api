package org.integratedmodelling.klab.api.test;

import java.util.concurrent.Future;

import org.integratedmodelling.klab.api.Klab;
import org.integratedmodelling.klab.api.model.Context;
import org.integratedmodelling.klab.api.model.Observable;
import org.integratedmodelling.klab.common.Geometry;
import org.integratedmodelling.klab.utils.Range;
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
		this.klab = Klab.create("https://developers.integratedmodelling.org/modeler", username, password);
		assert klab.isOnline();
	}
	
	@After
	public void disconnect() {
		if (klab.isOnline()) {
			klab.disconnect();
		}
	}

	@Test
	public void testContextObservation() throws Exception {

		/*
		 * pass a semantic type and a geometry
		 */
		Future<Context> contextTask = klab.submit(Observable.create("earth:Region"),
				Geometry.builder().grid(ruaha, "1 km").years(2010).build());

		/**
		 * Retrieve the context and assert it's valid
		 */
		Context context = contextTask.get();

		assert context != null;

	}

	@Test
	public void testDirectObservation() throws Exception {

		/*
		 * pass a semantic type and a geometry + a quality to observe. The quality will
		 * be available with the (obvious) name in the context
		 */
		Future<Context> contextTask = klab.submit(Observable.create("earth:Region"),
				Geometry.builder().grid(ruaha, "1 km").years(2010).build(), Observable.create("geography:Elevation"));

		/**
		 * Retrieve the context and assert it's valid
		 */
		Context context = contextTask.get();

		assert context != null;
		assert context.getObservation("elevation") != null;

		/*
		 * assert that the value of the elevation is in the standard unit (m) and the
		 * range is within the expected.
		 */
		assert Range.create(0, 3000).contains(context.getObservation("elevation").getDataRange());
	}

}
