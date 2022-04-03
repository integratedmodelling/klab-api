package org.integratedmodelling.klab.api.test;

import static org.junit.Assert.fail;

import java.util.concurrent.Future;

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
     * A square piece of Tanzania in which elevation values (in NASA STRM 90m DEM) are between 271
     * and 2875 m
     */
    private static String ruaha = "EPSG:4326 POLYGON((33.796 -7.086, 35.946 -7.086, 35.946 -9.41, 33.796 -9.41, 33.796 -7.086))";
    private Klab klab;

    @Before
    public void connect() {
        this.klab = Klab.create();
        if (!klab.isOnline()) {
            fail("engine is offline: aborting tests");
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
         * pass a semantic type and a geometry + a quality to observe. The quality will be available
         * with the (obvious) name in the context
         */
        Future<Context> contextTask = klab.submit(Observable.create("earth:Region"),
                Geometry.builder().grid(ruaha, "1 km").years(2010).build(),
                Observable.create("geography:Elevation"));

        /**
         * Retrieve the context and assert it's valid
         */
        Context context = contextTask.get();

        assert context != null;
        assert context.getObservation("elevation") != null;

        /*
         * assert that the value of the elevation is in the standard unit (m) and the range is
         * within the expected.
         */
        assert Range.create(0, 3000).contains(context.getObservation("elevation").getDataRange());
    }

    @Test
    public void testDirectNamedObservation() throws Exception {

        /*
         * pass a semantic type and a geometry + a quality to observe. The quality will be available
         * with the (obvious) name in the context
         */
        Future<Context> contextTask = klab.submit(Observable.create("earth:Region"),
                Geometry.builder().grid(ruaha, "1 km").years(2010).build(),
                Observable.create("geography:Elevation in ft named zurba"));

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
                Geometry.builder().grid(ruaha, "1 km").years(2010).build(),
                Observable.create("geography:Elevation"));

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

        Context context = klab.submit(Observable.create("earth:Region"),
                Geometry.builder().grid(ruaha, "1 km").years(2010).build()).get();

        assert context != null;

        Observation elevation = context.submit(new Observable("geography:Elevation")).get();

        assert elevation != null;
        assert Range.create(0, 3000).contains(elevation.getDataRange());

        /*
         * ensure the context has been updated with the new observation
         */
        assert context.getObservation("elevation") instanceof Observation;

    }

    @Test
    public void testConstantObservation() throws Exception {

        Context context = klab.submit(Observable.create("earth:Region"),
                Geometry.builder().grid(ruaha, "1 km").years(2010).build()).get();
        assert context != null;
        Observation constantState = context.submit(Observable.create("100 as geography:Elevation in m"))
                .get();
        assert constantState.getScalarValue() instanceof Double
                && NumberUtils.equal((Double) constantState.getScalarValue(), 100.0);

    }
}
