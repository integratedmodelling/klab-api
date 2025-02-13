package org.integratedmodelling.klab.api.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Future;

import org.integratedmodelling.klab.api.Context;
import org.integratedmodelling.klab.api.Estimate;
import org.integratedmodelling.klab.api.Klab;
import org.integratedmodelling.klab.api.Observable;
import org.integratedmodelling.klab.api.Observation;
import org.integratedmodelling.klab.api.API.PUBLIC.Export;
import org.integratedmodelling.klab.api.Klab.ExportFormat;
import org.integratedmodelling.klab.api.impl.EstimateImpl;
import org.integratedmodelling.klab.api.impl.ObservationImpl;
import org.integratedmodelling.klab.common.Geometry;
import org.integratedmodelling.klab.utils.NumberUtils;
import org.integratedmodelling.klab.utils.Range;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class KlabAPITestsuite {

    /**
     * A square piece of Tanzania
     */
    protected static String ruaha = "EPSG:4326 POLYGON((33.796 -7.086, 35.946 -7.086, 35.946 -9.41, 33.796 -9.41, 33.796 -7.086))";
    protected Klab klab;

    /**
     * String with template variables; I assume a span of one year is OK and everything is in
     * lat/lon projection (EPSG:4326)
     */
    private String geometryEncoding = "τ0(1){ttype=LOGICAL,period=[{TIME_PERIOD}],tscope=1.0,tunit=YEAR}S2({GRID_RESOLUTION_XY}){bbox=[{BOUNDING_BOX}],shape={WKB_SHAPE},proj=EPSG:4326}";

    /**
     * Subst variables to the {TIME_PERIOD}, {GRID_RESOLUTION_XY}, {BOUNDING_BOX} and {WKB_SHAPE}
     */
    private String timePeriod = "1640995200000 1672531200000";
    private String gridResolutionXY = "520,297";
    private String boundingBox = "-7.256596802202454 -4.408874148363334 38.39721372248553 40.02677860935444";
    private String wkbShape = "00000000030000000100000007C01D06C14FE6DEF24043B5F39D8BB550C0160A7B8B2DC6224044036D7B41B470C011A2AFE79D99FB4043B5C0443B5A7CC014D2EFCFADC624404355FA189A597CC0199C599EE6C5B8404332D7E635CC84C01C3D49F12A6BC440437995016B4E6CC01D06C14FE6DEF24043B5F39D8BB550";

    @Before
    public void connect() {
        this.klab = createClient();
        assert klab.isOnline();
    }

    protected abstract Klab createClient();

    @After
    public void disconnect() throws IOException {
        if (klab.isOnline()) {
            klab.close();
        }
    }

    @Test
    public void testGeometryTemplate() throws Exception {

        String geometrySpecs = geometryEncoding.replace("{BOUNDING_BOX}", boundingBox).replace("{TIME_PERIOD}", timePeriod)
                .replace("{GRID_RESOLUTION_XY}", gridResolutionXY).replace("{WKB_SHAPE}", wkbShape);
        Geometry geom = Geometry.create(geometrySpecs);
        Future<Context> contextTask = klab.submit(Observable.create("earth:Region"), geom);
        Context context = contextTask.get();
        assert context != null;

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
                Geometry.builder().grid(ruaha, "1 km").years(2010).build(), Observable.create("geography:Elevation"));

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
        assert Range.create(0, 3000).contains(context.getObservation("elevation").getDataRange())
                && context.getObservation("elevation").getDataRange().contains(Range.create(500, 2500));
    }

    @Test
    public void testAggregatedResults() throws Exception {

        /*
         * pass a semantic type and a geometry + a quality to observe. The quality will be available
         * with the (obvious) name in the context
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
         * assert that the value of the elevation is in the standard unit (m) and the range is
         * within the expected.
         */
        assert Range.create(0, 3000).contains(context.getObservation("elevation").getDataRange())
                && context.getObservation("elevation").getDataRange().contains(Range.create(500, 2500));

        Object aggregated = context.getObservation("elevation").getAggregatedValue();
        Object scalar = context.getObservation("elevation").getScalarValue();

        assert aggregated instanceof Number && ((Number) aggregated).doubleValue() > 0;
        assert scalar == null;
    }

    @Test
    public void testCategories() throws Exception {

        /*
         * pass a semantic type and a geometry + a quality to observe. The quality will be available
         * with the (obvious) name in the context
         */
        Future<Context> contextTask = klab.submit(Observable.create("earth:Region"),
                Geometry.builder().grid(ruaha, "1 km").years(2010).build(),
                Observable.create("landcover:LandCoverType").named("landcover"));

        /**
         * Retrieve the context and assert it's valid
         */
        Context context = contextTask.get();

        assert context != null;
        Observation landcover = context.getObservation("landcover");
        assert landcover != null;
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
    public void testSpatialRasterObjects() throws Exception {

        Future<Context> contextTask = klab.submit(Observable.create("earth:Region"),
                Geometry.builder().grid(ruaha, "1 km").years(2010).build());

        Context context = contextTask.get();
        Observation elevation = context.submit(Observable.create("geography:Elevation")).get();

        assert Range.create(0, 3000).contains(elevation.getDataRange())
                && elevation.getDataRange().contains(Range.create(500, 2500));

        // export to zip with raster and qgis style
        Path file = Files.createTempFile("klab_test_raster", ".zip");
        elevation.export(Export.DATA, ExportFormat.GEOTIFF_RASTER, file.toFile());
        assert file.toFile().exists();
        assert Files.readAllBytes(file).length > 0;

        // export to tiff via direct stream
        file = Files.createTempFile("klab_test_raster_stream", ".tiff");
        elevation.export(Export.DATA, ExportFormat.BYTESTREAM, file.toFile());
        assert file.toFile().exists();
        assert Files.readAllBytes(file).length > 0;

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
         * pass a semantic type and a geometry + a quality to observe. The quality will be available
         * with the (obvious) name in the context
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

        assert estimate != null && ((EstimateImpl) estimate).getEstimateId() != null;

        if (estimate.getCost() >= 0) {

            Future<Context> contextTask = klab.submit(estimate);
            Context context = contextTask.get();

            assert context != null;
            assert context.getObservation("elevation") != null;
            String dataflow = context.getDataflow(ExportFormat.KDL_CODE);
            assert dataflow != null && dataflow.length() > 0;
            String provenance = context.getProvenance(true, ExportFormat.ELK_GRAPH_JSON);
            assert provenance != null && provenance.length() > 0;
        }
    }

    @Test
    public void testContextualObservation() throws Exception {

        Future<Context> submit = klab.submit(Observable.create("earth:Region"),
                Geometry.builder().grid(ruaha, "1 km").years(2010).build());
        Context context = submit.get();

        assert context != null;

        Future<Observation> submit2 = context.submit(new Observable("geography:Elevation"));
        Observation elevation = submit2.get();

        assert elevation != null;
        assert elevation.getDataRange().contains(Range.create(500, 2500));

        /*
         * ensure the context has been updated with the new observation
         */
        assert context.getObservation("elevation") instanceof ObservationImpl;
        String dataflow = context.getDataflow(ExportFormat.KDL_CODE);
        assert dataflow != null && dataflow.length() > 0;
        String provenance = context.getProvenance(true, ExportFormat.ELK_GRAPH_JSON);
        assert provenance != null && provenance.length() > 0;
    }

    @Test
    public void testImageExport() throws Exception {

        Context context = klab
                .submit(Observable.create("earth:Region"), Geometry.builder().grid(ruaha, "1 km").years(2010).build()).get();

        assert context != null;
        Observation elevation = context.submit(new Observable("geography:Elevation")).get();
        File outfile = File.createTempFile("ruaha", ".png");
        outfile.deleteOnExit();
        assert elevation.export(Export.DATA, ExportFormat.PNG_IMAGE, outfile, "viewport", "900");
        assert outfile.exists() && outfile.length() > 10000;
        System.out.println(elevation.export(Export.LEGEND, ExportFormat.JSON_CODE));
        // TODO read the image into an outputstream and check it for size and content
    }

    @Test
    public void testConstantObservation() throws Exception {

        Context context = klab
                .submit(Observable.create("earth:Region"), Geometry.builder().grid(ruaha, "1 km").years(2010).build()).get();
        assert context != null;
        Observation constantState = context.submit(Observable.create("geography:Elevation").in("m").value(100)).get();
        assert constantState.getScalarValue() instanceof Double
                && NumberUtils.equal((Double) constantState.getScalarValue(), 100.0);

    }

}
