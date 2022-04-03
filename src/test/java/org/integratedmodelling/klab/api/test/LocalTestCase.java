package org.integratedmodelling.klab.api.test;

import static org.junit.Assert.fail;

import java.util.concurrent.Future;

import org.integratedmodelling.klab.api.Klab;
import org.integratedmodelling.klab.api.model.Context;
import org.integratedmodelling.klab.api.model.Estimate;
import org.integratedmodelling.klab.api.model.Observable;
import org.integratedmodelling.klab.common.Geometry;
import org.junit.Before;
import org.junit.Test;

public class LocalTestCase {

    /**
     * A square piece of Tanzania
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
    }

    @Test
    public void testDirectNamedObservation() throws Exception {

        /*
         * pass a semantic type and a geometry + a quality to observe. The quality will be available
         * with the (obvious) name in the context
         */
        Future<Context> contextTask = klab.submit(Observable.create("earth:Region"),
                Geometry.builder().grid(ruaha, "1 km").years(2010).build(),
                Observable.create("geography:Elevation named zurba"));

        /**
         * Retrieve the context and assert it's valid
         */
        Context context = contextTask.get();

        assert context != null;
        assert context.getObservation("elevation") == null;
        assert context.getObservation("zurba") != null;
    }

    @Test
    public void testEstimateObservation() throws Exception {

        /*
         * same parameters
         */
        Future<Estimate> estimateTask = klab.estimate(Observable.create("earth:Region"),
                Geometry.builder().grid(ruaha, "1 km").years(2010).build(),
                Observable.create("geography:Elevation in ft"));

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

}
