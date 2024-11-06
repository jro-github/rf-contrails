package de.tudresden.aerospace.contrails.MonteCarlo;

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
import de.tudresden.aerospace.contrails.Modeling.Contrail;
import de.tudresden.aerospace.contrails.Modeling.Legacy.SolarContrailLegacy;
import de.tudresden.aerospace.contrails.Modeling.SolarContrail;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @deprecated
 * This class aimed to test the correctness of the simulation of solar radiation with respect to parallelization. To do
 * this, multiple test runs were conducted, and randomness between them was eliminated by initializing the random
 * generator of each run with the same seed. This class has been deprecated and replaced with a statistical test, which
 * compares metrics instead. This is due to this class causing infinite loops and OutOfMemoryErrors when photons are
 * scattered, likely due to the mocked random values altering the behavior. Use {@link SimulationTest} instead.
 */
@Tag("deprecated")
@Tag("run_simulation")
class SolarSimulationTest {
    Random mockRandom;
    double mockRandomValue = Math.PI / 6.0; // Between 0.0 (inclusive) and 1.0 (exclusive)

    private File configFolder;
    private File outputFolder;

    private SolarContrailLegacy simSequential;

    private SolarContrail cParallel1;
    private SolarContrail cParallel2;
    private SolarSimulation simParallel1;
    private SolarSimulation simParallel2;

    @BeforeEach
    void setup() {
        configFolder = new File(PropertiesManager.getInstance().getDirResourcesTest(),
                "test_solar_correctness");

        // Initialize simulations
        XMLParametersLegacy pLegacy = XMLParametersLegacy.unmarshal(configFolder.toString(), "test_legacy.xml");
        XMLParameters pFast1 = XMLParameters.unmarshal(configFolder.toString(), "test.xml");
        XMLParameters pFast2 = XMLParameters.unmarshal(configFolder.toString(), "test.xml");

        outputFolder = new File(configFolder, "output");
        pLegacy.setOutputFilePrefix(new File(outputFolder, "test_legacy").toString());
        pFast1.getCommon().setOutputFilePrefix(new File(outputFolder, "test_fast_1").toString());
        pFast2.getCommon().setOutputFilePrefix(new File(outputFolder, "test_fast_2").toString());

        mockRandom = mock(Random.class);
        when(mockRandom.nextDouble()).thenReturn(mockRandomValue);

        simSequential = new SolarContrailLegacy(pLegacy, mockRandom);
        simSequential.getScPhFun().setRandomProvider(mockRandom);
        simParallel1 = new SolarSimulation(pFast1);
        simParallel2 = new SolarSimulation(pFast2);

        cParallel1 = new SolarContrail(pFast1.getSolarDiffuse(), mockRandom);
        cParallel2 = new SolarContrail(pFast2.getSolarDiffuse(), mockRandom);

        // Also set random provider of scattering phase function to mock object
        cParallel1.getScPhFun().setRandomProvider(mockRandom);
        cParallel2.getScPhFun().setRandomProvider(mockRandom);

        // Create output directory
        if (!outputFolder.exists())
            outputFolder.mkdirs();
    }

    /**
     * Runs the simulation using the deprecated {@link SolarContrailLegacy#MultipleDirections(File, Random)},
     * storing the result in the {@code Test} folder. Then, two more test runs are conducted with different numbers of
     * threads on {@link SolarSimulation#simulateDiffuseRadiation(Contrail, int, boolean)}, storing their respective results in
     * the same folder and comparing them.
     */
    @Test
    @DisplayName("Solar Simulation delivers the same results with and without multithreading")
    void testSimulateDiffuseRadiationParallelCorrect() {
        // Run 1: SolarContrail.MultipleDirections()
        simSequential.MultipleDirections(new File(configFolder, "test_legacy.xml"), mockRandom); // -> resources/output/test_legacy_multi.txt

        // Run 2: SolarSimulation.MultipleDirections(1)
        simParallel1.simulateDiffuseRadiation(cParallel1, 1, false); // -> resources/output/test_fast_1_multi.txt

        // Run 3: SolarSimulation.MultipleDirections(num_processors)
        int numThreads = Runtime.getRuntime().availableProcessors();
        simParallel2.simulateDiffuseRadiation(cParallel2, numThreads, false); // -> resources/output/test_fast_2_multi.txt

        // Compare
        File fLegacy = new File(outputFolder, "test_legacy_multi.txt");
        File f1 = new File(outputFolder, "test_fast_1.txt");
        File f2 = new File(outputFolder, "test_fast_2.txt");

        try {
            Assertions.assertThat(fLegacy).hasSameBinaryContentAs(f1);
        } catch (AssertionError e) {
            fail("test_legacy_multi.txt and test_fast_1.txt should have the same content!");
        }
        try {
            Assertions.assertThat(f1).hasSameBinaryContentAs(f2);
        } catch (AssertionError e) {
            fail("test_fast_1.txt and test_fast_2.txt should have the same content!");
        }
    }
}
