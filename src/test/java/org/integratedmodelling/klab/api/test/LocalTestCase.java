package org.integratedmodelling.klab.api.test;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.concurrent.Future;

import org.integratedmodelling.klab.api.API.PUBLIC.Export;
import org.integratedmodelling.klab.api.Klab.ExportFormat;
import org.integratedmodelling.klab.api.Klab;
import org.integratedmodelling.klab.api.model.Context;
import org.integratedmodelling.klab.api.model.Estimate;
import org.integratedmodelling.klab.api.model.Observable;
import org.integratedmodelling.klab.api.model.Observation;
import org.integratedmodelling.klab.common.Geometry;
import org.integratedmodelling.klab.utils.NumberUtils;
import org.integratedmodelling.klab.utils.Range;
import org.junit.Before;
import org.junit.Test;

public class LocalTestCase {

	/**
	 * A square piece of Tanzania in which elevation values (in NASA STRM 90m DEM)
	 * are between 271 and 2875 m
	 */
	private static String ruaha = "EPSG:4326 POLYGON ((36.411867526247384 -9.402230077083843, 33.363313628958174 -9.402230077083843, 33.363313628958174 -7.026333540991345, 36.411867526247384 -7.026333540991345, 36.411867526247384 -9.402230077083843))";
	private Klab klab;

	@Before
	public void connect() {
		if (this.klab == null) {
			this.klab = Klab.create();
			if (!klab.isOnline()) {
				fail("engine is offline: aborting tests");
			}
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
		assert Range.create(0, 3000).contains(context.getObservation("elevation").getDataRange())
				&& context.getObservation("elevation").getDataRange().contains(Range.create(500, 2500));
	}
	
	@Test
	public void testSpatialObjects() throws Exception {

		Future<Context> contextTask = klab.submit(Observable.create("earth:Region"),
				Geometry.builder().grid(ruaha, "1 km").years(2010).build());

		/**
		 * Retrieve the context and assert it's valid
		 */
		Context context = contextTask.get();
		Observation towns = context.submit(Observable.create("infrastructure:Town")).get();
		
		System.out.println(towns.export(Export.DATA, ExportFormat.GEOJSON_FEATURES));;
		
		assert towns != null;
	}
	
	@Test
	public void testSpatialObjectsInCatalog() throws Exception {

		Future<Context> contextTask = klab.submit(Observable.create("earth:Region"),
				Geometry.builder().grid(ruaha, "1 km").years(2010).build(), Observable.create("infrastructure:Town"));
		
		assert contextTask.get().getObservation("town") != null;
	}
	
	@Test
	public void testDirectNamedObservation() throws Exception {

		/*
		 * pass a semantic type and a geometry + a quality to observe. The quality will
		 * be available with the (obvious) name in the context
		 */
		Future<Context> contextTask = klab.submit(Observable.create("earth:Region"),
				Geometry.builder().grid(ruaha, "1 km").years(2010).build(),
				Observable.create("geography:Elevation").in("ft").named("zurba"));

		/**
		 * Retrieve the context and assert it's valid
		 */
		Context context = contextTask.get();

		assert context != null;
		assert context.getObservation("elevation") == null;
		assert context.getObservation("zurba") != null;

		/*
		 * observation is in ft: check that range is wider than the expected in m
		 */
		assert !Range.create(0, 3000).contains(context.getObservation("zurba").getDataRange());

	}

	@Test
	public void testEstimateObservation() throws Exception {

		/*
		 * same parameters as an observe() call
		 */
		Future<Estimate> estimateTask = klab.estimate(Observable.create("earth:Region"),
				Geometry.builder().grid(ruaha, "1 km").years(2010).build(), Observable.create("geography:Elevation"));

		/**
		 * Retrieve the context and assert it's valid
		 */
		Estimate estimate = estimateTask.get();

		assert estimate != null && estimate.getEstimateId() != null;

		if (estimate.getCost() >= 0) {

			Future<Context> contextTask = klab.submit(estimate);
			Context context = contextTask.get();

			assert context != null;
			assert context.getObservation("elevation") != null;
		}
	}

	@Test
	public void testContextualObservation() throws Exception {

		Context context = klab
				.submit(Observable.create("earth:Region"), Geometry.builder().grid(ruaha, "1 km").years(2010).build())
				.get();

		assert context != null;

		Observation elevation = context.submit(new Observable("geography:Elevation")).get();

		assert elevation != null;
		assert elevation.getDataRange().contains(Range.create(500, 2500));

		/*
		 * ensure the context has been updated with the new observation
		 */
		assert context.getObservation("elevation") instanceof Observation;

	}

	@Test
	public void testImageExport() throws Exception {

		Context context = klab
				.submit(Observable.create("earth:Region"), Geometry.builder().grid(ruaha, "1 km").years(2010).build())
				.get();

		assert context != null;
		Observation elevation = context.submit(new Observable("geography:Elevation")).get();
		File outfile = File.createTempFile("ruaha", ".png");
		assert elevation.export(Export.DATA, ExportFormat.PNG_IMAGE, outfile, "viewport", "900");
		System.out.println(elevation.export(Export.LEGEND, ExportFormat.JSON_CODE)); 
		// TODO read the image into an outputstream and check it for size and content -
		// currently fails because of weird crop bug (not happening in explorer)
	}

	@Test
	public void testConstantObservation() throws Exception {

		Context context = klab
				.submit(Observable.create("earth:Region"), Geometry.builder().grid(ruaha, "1 km").years(2010).build())
				.get();
		assert context != null;
		Observation constantState = context.submit(Observable.create("geography:Elevation").in("m").value(100)).get();
		assert constantState.getScalarValue() instanceof Double
				&& NumberUtils.equal((Double) constantState.getScalarValue(), 100.0);

	}
}
