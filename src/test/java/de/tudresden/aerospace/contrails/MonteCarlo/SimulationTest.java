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

import de.tudresden.aerospace.contrails.Configuration.IO.SimulationParser;
import de.tudresden.aerospace.contrails.Configuration.Parameters.XMLParameters;
import de.tudresden.aerospace.contrails.Configuration.PropertiesManager;
import de.tudresden.aerospace.contrails.Modeling.SolarContrail;
import de.tudresden.aerospace.contrails.Modeling.TerrestrialContrail;
import de.tudresden.aerospace.contrails.Utility.IOHelpers;
import de.tudresden.aerospace.contrails.Utility.MathHelpers;
import org.junit.jupiter.api.*;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * This class performs statistical tests on the MC simulation output. For this, metrics are used. Statistical tests can
 * never be 100% reliable. However, given the architecture of a Monte Carlo simulation, this is a decent way of making
 * sure that the simulation's output is within the expected range, which was determined during the evaluation phase.
 */
public class SimulationTest {
    private enum Type {
        SOLAR_DIRECT,
        SOLAR_DIFFUSE,
        TERRESTRIAL_DIFFUSE
    }

    private PropertiesManager pm;
    private File dirOut;
    private String outPrefixOriginal;
    private XMLParameters parameters;

    private Double maxDeviationFactor;
    private BigDecimal numPhotons;
    private BigDecimal avgScatUpExpected;
    private BigDecimal avgScatDownExpected;
    private BigDecimal avgAbsExpected;

    @BeforeEach
    void setup() {
        // Setup directories and parameters
        pm = PropertiesManager.getInstance();
        File configFolder = new File(pm.getDirResourcesTest(),
                "test_simulation");
        dirOut = new File(configFolder, "output");

        parameters = XMLParameters.unmarshal(configFolder.toString(), "test.xml");
        outPrefixOriginal = parameters.getCommon().getOutputFilePrefix();
        parameters.getCommon().setOutputFilePrefix(
                IOHelpers.getTestResourcesPath("test_simulation/output/test_simulation"));

        // Create output directory
        File outputFolder = new File(configFolder, "output");
        if (!outputFolder.exists())
            outputFolder.mkdirs();
    }

    /**
     * Sets the expected values for tests to compare against, depending on the type of simulation.
     * @param type The {@link Type} of simulation to assign expected values for
     */
    void setupExpectedValues(Type type) {
        numPhotons = new BigDecimal(100_000); // 100'000 photons per angle

        // Tests pass if there is 0.001% of num_photons or less deviation from expected values
        maxDeviationFactor = 1E-4; // With 100'000 photons = 10 photons maximum deviation

        // Hard-coded values have been determined by averaging (and rounding) 5 runs of the given configuration
        switch (type) {
            case SOLAR_DIRECT:
                avgAbsExpected = new BigDecimal(0);
                avgScatUpExpected = new BigDecimal(44);
                avgScatDownExpected = new BigDecimal(2404);
                break;
            case SOLAR_DIFFUSE:
                avgAbsExpected = new BigDecimal(0.11839506);
                avgScatUpExpected = new BigDecimal(718);
                avgScatDownExpected = new BigDecimal(718);
                break;
            case TERRESTRIAL_DIFFUSE:
                avgAbsExpected = new BigDecimal(828.5748);
                avgScatUpExpected = new BigDecimal(310);
                avgScatDownExpected = new BigDecimal(311);
                break;
        }
    }

    /**
     * Runs the parts of the simulation separately, with the given parameters.
     */
    @Nested
    @Order(1)
    @Tag("run_simulation")
    class RunSimulation {
        @Test
        @Order(1)
        @DisplayName("Runs the solar direct part of the simulation and writes results to output folder")
        void runSolarDirect() {
            SolarSimulation simulation = new SolarSimulation(parameters);
            simulation.simulateDirectRadiation();
        }

        @Test
        @Order(2)
        @DisplayName("Runs the solar diffuse part of the simulation and writes results to output folder")
        void runSolarDiffuse() {
            SolarSimulation simulation = new SolarSimulation(parameters);
            SolarContrail contrail = new SolarContrail(parameters.getSolarDiffuse());
            int threads = Runtime.getRuntime().availableProcessors();
            simulation.simulateDiffuseRadiation(contrail, threads, false);
        }

        @Test
        @Order(3)
        @DisplayName("Runs the terrestrial diffuse part of the simulation and writes results to output folder")
        void runTerrestrialDiffuse() {
            TerrestrialSimulation simulation = new TerrestrialSimulation(parameters);
            TerrestrialContrail contrail = new TerrestrialContrail(parameters.getTerrestrialDiffuse());
            int threads = Runtime.getRuntime().availableProcessors();
            simulation.simulateDiffuseRadiation(contrail, threads, false);
        }
    }

    /**
     * Evaluates the results of simulation parts run with {@link RunSimulation}.
     */
    @Nested
    @Order(2)
    class EvaluateSimulationResults {
        /**
         * Helper to get a single output file from the output directory by type.
         * @param type The {@link Type} of the simulation (solar_dir, solar_diff, terr_diff)
         * @return An output file of matching type, if exists. Throws an exception or fails otherwise.
         */
        File getOutputFileByType(Type type) {
            File[] simulationFiles = null;
            switch (type) {
                case SOLAR_DIRECT:
                    simulationFiles = IOHelpers.getSimulationOutputFiles(dirOut, outPrefixOriginal,
                            SolarSimulation.suffixDirect);
                    break;
                case SOLAR_DIFFUSE:
                    simulationFiles = IOHelpers.getSimulationOutputFiles(dirOut, outPrefixOriginal,
                            SolarSimulation.suffixDiffuseSolar);
                    break;
                case TERRESTRIAL_DIFFUSE:
                    simulationFiles = IOHelpers.getSimulationOutputFiles(dirOut, outPrefixOriginal,
                            TerrestrialSimulation.suffixDiffuseTerrestrial);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid simulation type");
            }

            if (simulationFiles == null)
                throw new RuntimeException("Error trying to read files in output directory " + dirOut);

            if (simulationFiles.length == 0) {
                fail("Could not find any output file for this part of radiation. " +
                        "Make sure to run the simulation before running the evaluation.");
            }
            return simulationFiles[0];
        }

        /**
         * Gets the average value of a column in a parsed simulation output file.
         * @param parser The simulation parser, which has already parsed a simulation file.
         * @param columnIndex The index of the column to parse
         * @return A {@link BigDecimal} containing the average value
         */
        BigDecimal averageColumn(SimulationParser parser, SimulationParser.TableIndex columnIndex) {
            int id = columnIndex.ordinal();
            List<Double> nums = parser.getRangeList(id, id + 1);

            BigDecimal sum = BigDecimal.ZERO;
            for (int i = 0; i < nums.size(); i++) {
                sum = sum.add(BigDecimal.valueOf(nums.get(i)));
            }
            return sum.divide(BigDecimal.valueOf(nums.size()), 8, RoundingMode.HALF_UP);
        }

        /**
         * Helper that performs the statistical test. Assumes expected values to be initialized by the test method.
         * @param type The {@link Type} of the simulation (solar_dir, solar_diff, terr_diff)
         */
        void testStatistic(Type type) {
            // Results get overwritten, therefore there should only be one result
            File simFile = getOutputFileByType(type);
            SimulationParser parser = new SimulationParser(simFile, " ");
            parser.parse();

            // num_abs
            // sensitivity is much higher than with num_scat_*, therefore add fixed offset of 1 to avoid epsilons of 0
            // and scale by one order of magnitude
            Double epsilon = 0.1 * maxDeviationFactor * numPhotons.doubleValue() + 1.0;
            BigDecimal avg_abs = averageColumn(parser, SimulationParser.TableIndex.NUM_ABS);
            boolean pass = MathHelpers.compareDouble(avg_abs.doubleValue(), avgAbsExpected.doubleValue(),
                    epsilon);
            try {
                assertThat(pass).isTrue();
            } catch (AssertionError e) {
                fail(String.format("Simulation has average num_abs = %.8f, which is not within expected value = %.8f " +
                                "+- epsilon = %.8f", avg_abs, avgAbsExpected, epsilon));
            }

            // num_scat_up
            epsilon = maxDeviationFactor * numPhotons.doubleValue();
            BigDecimal avg_scat_up = averageColumn(parser, SimulationParser.TableIndex.NUM_SCATTERED_UP);
            pass = MathHelpers.compareDouble(avg_scat_up.doubleValue(), avgScatUpExpected.doubleValue(),
                    epsilon);
            try {
                assertThat(pass).isTrue();
            } catch (AssertionError e) {
                fail(String.format("Simulation has average num_scat_up = %.2f, which is not within expected value = %.2f " +
                        "+- epsilon = %.2f", avg_scat_up, avgScatUpExpected, epsilon));
            }

            // num_scat_down
            epsilon = maxDeviationFactor * numPhotons.doubleValue();
            BigDecimal avg_scat_down = averageColumn(parser, SimulationParser.TableIndex.NUM_SCATTERED_DOWN);
            pass = MathHelpers.compareDouble(avg_scat_down.doubleValue(), avgScatDownExpected.doubleValue(),
                    epsilon);
            try {
                assertThat(pass).isTrue();
            } catch (AssertionError e) {
                fail(String.format("Simulation has average num_scat_down = %.2f, which is not within expected value = %.2f " +
                        "+- epsilon = %.2f", avg_scat_down, avgScatDownExpected, epsilon));
            }
        }

        /**
         * Conducts a statistical test for the direct part of the solar simulation, comparing values from the output
         * file to expected values.
         */
        @Test
        @Order(1)
        @DisplayName("Solar direct simulation results do not deviate from expected statistical results by more than" +
                "0.001% of num_photons + 1")
        void testSolarDirectSimulationStatistic() {
            setupExpectedValues(Type.SOLAR_DIRECT);
            testStatistic(Type.SOLAR_DIRECT);
        }

        /**
         * Conducts a statistical test for the diffuse part of the solar simulation, comparing values from the output
         * file to expected values.
         */
        @Test
        @Order(2)
        @DisplayName("Solar diffuse simulation results do not deviate from expected statistical results by more than" +
                "0.001% of num_photons")
        void testSolarDiffuseSimulationStatistic() {
            setupExpectedValues(Type.SOLAR_DIFFUSE);
            testStatistic(Type.SOLAR_DIFFUSE);
        }

        /**
         * Conducts a statistical test for the diffuse part of the terrestrial simulation, comparing values from the output
         * file to expected values.
         */
        @Test
        @Order(3)
        @DisplayName("Terrestrial diffuse simulation results do not deviate from expected statistical results by more than" +
                "0.001% of num_photons")
        void testTerrestrialDiffuseSimulationStatistic() {
            setupExpectedValues(Type.TERRESTRIAL_DIFFUSE);
            testStatistic(Type.TERRESTRIAL_DIFFUSE);
        }
    }
}
