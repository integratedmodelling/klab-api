package org.integratedmodelling.klab.api.test;

import java.util.concurrent.Future;

import org.integratedmodelling.klab.api.Estimate;
import org.integratedmodelling.klab.api.Klab;
import org.junit.Test;

public class SimpleObservation {

	@Test
	public void test() {
		Klab klab = Klab.create("http://127.0.0.1:8283/modeler");
		Future<Estimate> estimate = klab.estimate();
//		if (estimate.get().getCost() < 100000) {
//			Future<Context> context = klab.submit(estimate.get());
//		}
	}

}
