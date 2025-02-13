package org.integratedmodelling.klab.api.test;

import java.io.IOException;

import org.integratedmodelling.klab.api.Context;
import org.integratedmodelling.klab.api.Klab;
import org.integratedmodelling.klab.api.Observable;
import org.integratedmodelling.klab.api.Observation;
import org.integratedmodelling.klab.common.Geometry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * HeCo test suite configured for a local engine. In order for the test to run, the k.LAB engine and
 * modeler must be running on the local machine, and the project
 * https://bitbucket.org/integratedmodelling/aries.heco.git must have been imported in the modeler
 * and set to the develop branch prior to starting the engine.
 * 
 * @author Ferd
 *
 */
public class HeCoTestsLocal {

    static String[] indicators = {"im:Indicator (value of ecology:Biodiversity)",
            "im:Indicator (value of ecology:Ecosystem for es:ClimateRegulation)",
            "im:Indicator (es.nca:Condition of earth:Aquatic ecology:Ecosystem)",
            "im:Indicator (es.nca:Condition of demography:Human demography:Community)"};

    static Geometry centralColombia = Geometry.create(
            "τ0(1){ttype=LOGICAL,period=[1609459200000 1640995200000],tscope=1.0,tunit=YEAR}S2(934,631){bbox=[-75.2281407807369 -72.67107290964314 3.5641500380320963 5.302943221927137],shape=00000000030000000100000005C0522AF2DBCA0987400C8361185B1480C052CE99DBCA0987400C8361185B1480C052CE99DBCA098740153636BF7AE340C0522AF2DBCA098740153636BF7AE340C0522AF2DBCA0987400C8361185B1480,proj=EPSG:4326}");

    protected Klab klab;

    @Before
    public void connect() {
        this.klab = createClient();
        assert klab.isOnline();
    }

    protected Klab createClient() {
        return Klab.create();
    }

    @After
    public void disconnect() throws IOException {
        if (klab.isOnline()) {
            klab.close();
        }
    }

//    private void computeIndicator(String indicatorObservable) throws Exception {
//        // Context colombia = klab.submit("aries.heco.locations.colombia_continental").get();
//        Context colombia = klab.submit(Observable.create("earth:Region"), centralColombia).get();
//        assert colombia != null;
//        Observation indicator = colombia.submit(Observable.create(indicatorObservable)).get();
//        assert indicator != null && !indicator.isEmpty();
//        System.out.println(indicator + " = " + indicator.getAggregatedValue());
//        assert indicator.getAggregatedValue() instanceof Number && ((Number) indicator.getAggregatedValue()).doubleValue() > 0;
//        assert indicator.getScalarValue() == null;
//    }

//    @Test
//    public void firstIndicatorSeparately() throws Exception {
//        computeIndicator(indicators[0]);
//    }
//
//    @Test
//    public void secondIndicatorSeparately() throws Exception {
//        computeIndicator(indicators[1]);
//    }
//
//    @Test
//    public void thirdIndicatorSeparately() throws Exception {
//        computeIndicator(indicators[2]);
//    }
//
//    @Test
//    public void fourthIndicatorSeparately() throws Exception {
//        computeIndicator(indicators[3]);
//    }

    @Test
    public void allIndicatorsSequentially() throws Exception {
//        Context colombia = klab.submit("aries.heco.locations.colombia_continental").get();
        Context colombia = klab.submit(Observable.create("earth:Region"), centralColombia).get();
        assert colombia != null;

        int success = 0;
        for(String indicator : indicators) {
            Observation observedIndicator = colombia.submit(Observable.create(indicator)).get();
            if (observedIndicator.isEmpty()) {
                System.out.println("Observation of " + indicator + " failed");
                continue;
            }
            success++;
            System.out.println(indicator + " = " + observedIndicator.getAggregatedValue());
        }

        assert success == indicators.length;
    }
}
