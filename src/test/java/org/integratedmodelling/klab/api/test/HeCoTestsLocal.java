package org.integratedmodelling.klab.api.test;

import java.io.IOException;

import org.integratedmodelling.klab.api.Context;
import org.integratedmodelling.klab.api.Klab;
import org.integratedmodelling.klab.api.Observable;
import org.integratedmodelling.klab.api.Observation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * HeCo test suite configured for a local engine. In order for the test to run,
 * the k.LAB engine and modeler must be running on the local machine, and the
 * project https://bitbucket.org/integratedmodelling/aries.heco.git must have
 * been imported in the modeler and set to the develop branch prior to starting
 * the engine.
 * 
 * @author Ferd
 *
 */
public class HeCoTestsLocal {

	static String[] indicators = { "im:Indicator value of ecology:Biodiversity",
			"im:Indicator value of ecology:Ecosystem for es:ClimateRegulation",
			"im:Indicator es.nca:Condition of demography:SocialStructure",
			"im:Indicator es.nca:Condition of earth:Aquatic ecology:Ecosystem" };

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

	@Test
	public void biodiversityIndicator() throws Exception {
		Context colombia = klab.submit("aries.heco.locations.colombia_continental").get();
		assert colombia != null;
		Observation biodiversityIndicator = colombia.submit(Observable.create(indicators[0])).get();
		assert biodiversityIndicator != null && !biodiversityIndicator.isEmpty();
		assert biodiversityIndicator.getAggregatedValue() instanceof Number
				&& ((Number) biodiversityIndicator.getAggregatedValue()).doubleValue() > 0;
		assert biodiversityIndicator.getScalarValue() == null;
	}
}
