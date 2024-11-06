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

import de.tudresden.aerospace.contrails.Configuration.IO.DynamicTable;
import de.tudresden.aerospace.contrails.Configuration.Parameters.DiffuseParameters;
import de.tudresden.aerospace.contrails.Configuration.Parameters.XMLParameters;
import de.tudresden.aerospace.contrails.Configuration.PropertiesManager;
import de.tudresden.aerospace.contrails.Modeling.SolarContrail;
import de.tudresden.aerospace.contrails.Utility.IOHelpers;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class SolarSimulation extends Simulation {
    public static final String suffixDirect = "_solar_direct";

    /**
     * Set by {@link SolarSimulation#promptCheckOutputFiles(boolean)}
     */
    private boolean runDiffuse = true;

    /**
     * Creates the solar simulation object with the given XML parameters.
     *
     * @param params The {@link XMLParameters} to initialize simulation parameters with
     */
    public SolarSimulation(XMLParameters params) {
        super(params.getCommon(), params.getSolarDiffuse(), params.getSolarDirect(), suffixDiffuseSolar);
    }

    /**
     * Performs the Monte Carlo Simulation for a single direction of incoming light, calculating the scattering towards
     * sky and ground as well as the absorption of light caused by the contrail. Output is written to a file. This
     * function is used to compute the direct part of incoming radiation by supplying {@code sza} and {@code phi0}
     * as theta and phi in the configuration file.
     */
    public void simulateDirectRadiation() {
        System.out.println("\n -= Monte Carlo simulation of optical contrail properties =-");
        System.out.println("Solar part: direct radiation calculation\n");

        // Initialize Monte Carlo - Simulation
        SolarContrail contrail = new SolarContrail(diffuseParams);

        // Compute single direction
        SingleStepResult res = null;
        Callable<SingleStepResult> task = new ComputeSingleStep(0, contrail, commonParams.getNumPhotons(),
                diffuseParams.getResolutionS(), directParams.getSza(), directParams.getPhi0());
        try {
            res = task.call();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Add weighting (see also dissertation page 85ff)
        double alpha = Math.acos(Math.sin(directParams.getSza()) * Math.cos(directParams.getPhi0()));
        double factor = 2.0 * diffuseParams.getIncidentRadius() * Math.sin(alpha) / commonParams.getNumPhotons();

        // Print results
        System.out.format("Number of absorbed photons = %d, with correction factor = %g\n",
                res.nAbs, res.nAbs * factor);
        System.out.format("Number of scattered photons = %d, with correction factor = %g\n",
                res.nScat, res.nScat * factor);
        System.out.format("Number of upwards scattered photons = %d, with correction factor = %g\n",
                res.nScatUp, res.nScatUp * factor);
        System.out.format("Number of downwards scattered photons = %d, with correction factor = %g\n",
                res.nScatDown, res.nScatDown * factor);
        System.out.format("Number of multiple scattered photons = %d, with correction factor = %g\n",
                res.nScatMultiple, res.nScatMultiple * factor);
        System.out.format("Number of transmitted photons = %d, with correction factor = %g\n\n",
                res.nTrans, res.nTrans * factor);
        System.out.format("Average number of scattering events per photon = %g\n", res.avgScattering);

        // Save results to file
        FileWriter fwDataOut = getOutputWriter(suffixDirect);

        // Save the number of scattered photons per angle theta and bin of phi
        List<String> cNames = new ArrayList<>();
        cNames.add("sza");
        cNames.add("phi0");
        cNames.add("num_absorbed");
        cNames.add("num_scattered");
        cNames.add("num_scattered_up");
        cNames.add("num_scattered_down");
        cNames.add("correction_factor"); // Multiply any num_x by correction_factor to apply correction by angle and incident radius

        DynamicTable<Double> table = new DynamicTable<>("DIRECT_SCATTERED_RADIATION", cNames);
        List<Double> row = new ArrayList<>();
        row.add(directParams.getSza());
        row.add(directParams.getPhi0());
        row.add(Double.valueOf(res.nAbs));
        row.add(Double.valueOf(res.nScat));
        row.add(Double.valueOf(res.nScatUp));
        row.add(Double.valueOf(res.nScatDown));
        row.add(factor);
        table.addRow(row);

        table.marshal(fwDataOut, " ");
        IOHelpers.tryClose(fwDataOut);
    }

    @Override
    public void promptCheckOutputFiles(boolean overwrite) {
        // Check presence of output files and prompt for re-run if already present
        File dir_out = PropertiesManager.getInstance().getDirOutput();
        File[] fDiffuse = IOHelpers.getSimulationOutputFiles(dir_out, commonParams.getOutputFilePrefix(), suffixDiffuse);
        File[] fDirect = IOHelpers.getSimulationOutputFiles(dir_out, commonParams.getOutputFilePrefix(), suffixDirect);

        // If files should not be overwritten by default, prompt user
        if (!overwrite) {
            for (File f : fDirect) {
                DiffuseParameters p = IOHelpers.getDiffuseParametersFromFile(f);
                if (p.getSpectralBandIndex() == diffuseParams.getSpectralBandIndex()) {
                    System.out.println("Output file for direct radiation (" + f.getName() + ") already exist");
                    runDirect = IOHelpers.promptUserDecision("Would you like to re-run the solar simulation for direct radiation" +
                            " and overwrite the file?");
                    break;
                }
            }
            for (File f : fDiffuse) {
                DiffuseParameters p = IOHelpers.getDiffuseParametersFromFile(f);
                if (p.getSpectralBandIndex() == diffuseParams.getSpectralBandIndex()) {
                    System.out.println("Output file for multiple directions (" + f.getName() + ") already exists");
                    runDiffuse = IOHelpers.promptUserDecision("Would you like to re-run the solar simulation for multiple directions" +
                            " and overwrite the file?");
                    break;
                }
            }
        }
    }

    @Override
    public void runSimulation(int numThreads, boolean writeMetricsFile, Random randomProvider) {
        // Set by promptCheckOutputFiles()
        if (runDirect) {
            System.out.println("Running solar direct simulation");
            simulateDirectRadiation();
        }

        if (runDiffuse) {
            System.out.println("Running solar diffuse simulation");
            // Create contrail object
            SolarContrail contrail;
            if (randomProvider == null) {
                contrail = new SolarContrail(diffuseParams);
            } else {
                contrail = new SolarContrail(diffuseParams, randomProvider);

                // Also set random provider of scattering phase function to mock object
                contrail.getScPhFun().setRandomProvider(randomProvider);
            }

            simulateDiffuseRadiation(contrail, numThreads, writeMetricsFile);
        }
    }
}
