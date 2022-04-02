package org.integratedmodelling.klab.api.test;

import java.util.concurrent.Future;

import org.integratedmodelling.klab.api.Klab;
import org.integratedmodelling.klab.api.model.Context;
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
        assert klab.isOnline();
    }

    @Test
    public void testObservation() throws Exception {

        /*
         * pass a semantic type and a geometry
         */
        Future<Context> contextTask = klab.submit(Observable.create("earth:Region"),
                Geometry.builder().grid(ruaha, "1 km").years(2010).build(),
                Observable.create("geography:Elevation").in("ft"));

        /**
         * Retrieve the context and assert it's valid
         */
        Context context = contextTask.get();

        assert context != null;
        assert context.getObservation("elevation") != null;
    }

}
