package org.integratedmodelling.klab.api.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.integratedmodelling.klab.api.Klab;
import org.integratedmodelling.klab.exceptions.KlabResourceNotFoundException;
import org.junit.Before;

/**
 * To run the test suite on a remote engine, a ~/.klab/testcredential.properties
 * file must be present and contain username and password for the test engine,
 * whose URL defaults to the development engine at BC3 and can be changed using
 * the 'engine' property.
 * 
 * @author Ferd
 *
 */
public class RemoteTestCase extends KlabAPITestsuite {

    protected String username;
    protected String password;
    protected String testEngine;

    @Before
    public void readCredentials() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(new File(
                System.getProperty("user.home") + File.separator + ".klab" + File.separator + "testcredentials.properties"))) {
            properties.load(input);
        } catch (IOException e) {
            throw new KlabResourceNotFoundException(
                    "can't open ~/.klab/testcredentials.properties with username and passwords for test engine");
        }
        this.username = properties.getProperty("username", "username");
        this.password = properties.getProperty("password", "password");
        this.testEngine = properties.getProperty("engine", "https://developers.integratedmodelling.org/modeler");
    }

    @Override
    protected Klab createClient() {
        readCredentials();
        return Klab.create(testEngine, username, password);
    }

}
