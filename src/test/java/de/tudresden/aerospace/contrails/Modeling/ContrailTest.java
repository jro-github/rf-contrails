package de.tudresden.aerospace.contrails.Modeling;

/*-
 * #%L
 * RF-Contrails
 * %%
 * Copyright (C) 2024 Institute of Aerospace Engineering, TU Dresden
 * %%
 * Copyright 2,024 Institute of Aerospace Engineering, TU Dresden
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * #L%
 */

import de.tudresden.aerospace.contrails.Configuration.Parameters.XMLParameters;
import de.tudresden.aerospace.contrails.Configuration.PropertiesManager;
import de.tudresden.aerospace.contrails.Configuration.XMLParametersLegacy;
import de.tudresden.aerospace.contrails.Modeling.Legacy.ContrailLegacy;
import de.tudresden.aerospace.contrails.Modeling.Legacy.SolarContrailLegacy;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Random;

public class ContrailTest {
    private Contrail c;
    private ContrailLegacy cLegacy;
    private File configFolder;
    private Random rand;
    private Random rContrail;
    private Random rContrailLegacy;

    @BeforeEach
    void setup() {
        PropertiesManager pm = PropertiesManager.getInstance();
        File dir_resources_test = PropertiesManager.getInstance().getDirResourcesTest();
        configFolder = new File(dir_resources_test,
                "test_contrail");
        XMLParametersLegacy pLegacy = XMLParametersLegacy.unmarshal(configFolder.toString(), "test_legacy.xml");
        XMLParameters p = XMLParameters.unmarshal(configFolder.toString(), "test.xml");

        rand = new Random();
        rContrail = new Random(12345678);
        rContrailLegacy = new Random(12345678);

        c = new SolarContrail(p.getSolarDiffuse(), rContrail);
        c.setMultiThreadedMode(false);
        c.getScPhFun().setRandomProvider(rContrail);

        cLegacy = new SolarContrailLegacy(pLegacy, rContrailLegacy);
        cLegacy.multiThreadedMode = false;
        cLegacy.getScPhFun().setRandomProvider(rContrailLegacy);
    }

    @Test
    @DisplayName("Iextinction calculations correct")
    void testExtinctionIntegral() {
        // Test with 50 different parameter configurations
        for (int i = 0; i < 1_000_000; i++) {
            double y0 = rand.nextDouble() * Math.PI; // Range [0, Pi]
            double z0 = rand.nextDouble() * 2 - 1; // Range [-1, 1]
            double theta = rand.nextDouble() * 2 * Math.PI; // Range [0, 2*Pi]
            double phi = rand.nextDouble() * Math.PI; // Range [0, Pi]
            double s = rand.nextDouble() * 2.01 * c.getParams().getIncidentRadius() + 0.01;

            double res = c.Iextinction(y0, z0, theta, phi, s);
            double resLegacy = cLegacy.Iextinction(y0, z0, theta, phi, s);

            Assertions.assertThat(res).isEqualTo(resLegacy);
        }
    }

    @Test
    @DisplayName("SinglePhotonIntegration correct")
    void testSinglePhotonIntegration() {
        // Test for 1.000.000 photons
        for (int i = 0; i < 1_000_000; i++) {
            double theta = rand.nextDouble() * 2 * Math.PI; // Range [0, 2*Pi]
            double phi = rand.nextDouble() * Math.PI; // Range [0, Pi]

            RePhoton res = c.singlePhotonIntegration(theta, phi);
            RePhoton resLegacy = cLegacy.singlePhotonIntegration(theta, phi);
            Assertions.assertThat(res.Scat_events).isEqualTo(resLegacy.Scat_events);
        }
    }
}
