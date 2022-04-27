package org.integratedmodelling.klab.api.test;

import org.integratedmodelling.klab.api.Klab;

public class LocalTestCase extends KlabAPITestsuite {

	@Override
	protected Klab createClient() {
		return Klab.create();
	}

}
