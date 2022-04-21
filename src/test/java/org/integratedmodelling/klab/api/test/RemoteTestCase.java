package org.integratedmodelling.klab.api.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.integratedmodelling.klab.exceptions.KlabResourceNotFoundException;
import org.junit.Before;

public class RemoteTestCase {
	
	protected String username;
	protected String password;
	
	@Before
	public void readCredentials() {
		Properties properties = new Properties();
		try (InputStream input = new FileInputStream(new File(System.getProperty("user.home") + File.separator + ".klab"
				+ File.separator + "testcredentials.properties"))) {
			properties.load(input);
		} catch (IOException e) {
			throw new KlabResourceNotFoundException(
					"can't open ~/.klab/testcredentials.properties with username and passwords for test engine");
		}
		this.username = properties.getProperty("username", "username");
		this.password = properties.getProperty("password", "password");
	}
	
}
