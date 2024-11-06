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

import de.tudresden.aerospace.contrails.Configuration.Parameters.DiffuseParameters;
import de.tudresden.aerospace.contrails.Configuration.Parameters.XMLParameters;
import de.tudresden.aerospace.contrails.Configuration.PropertiesManager;
import de.tudresden.aerospace.contrails.Modeling.*;
import de.tudresden.aerospace.contrails.Utility.IOHelpers;

import java.io.File;
import java.util.*;

public class TerrestrialSimulation extends Simulation {

    /**
     * Creates the terrestrial simulation object with the given XML parameters.
     *
     * @param params The {@link XMLParameters} to initialize simulation parameters with
     */
    public TerrestrialSimulation(XMLParameters params) {
        // Terrestrial simulation has no direct part
        super(params.getCommon(), params.getTerrestrialDiffuse(), null, suffixDiffuseTerrestrial);
    }

    @Override
    public void promptCheckOutputFiles(boolean overwrite) {
        File dir_out = PropertiesManager.getInstance().getDirOutput();
        File[] fOut = IOHelpers.getSimulationOutputFiles(dir_out, commonParams.getOutputFilePrefix(), suffixDiffuse);

        // If files should not be overwritten by default, prompt user
        if (!overwrite) {
            for (File f : fOut) {
                DiffuseParameters p = IOHelpers.getDiffuseParametersFromFile(f);
                if (p.getSpectralBandIndex() == diffuseParams.getSpectralBandIndex()) {
                    System.out.println("Output file (" + f.getName() + ") already exists");
                    runDirect = IOHelpers.promptUserDecision("Would you like to re-run the terrestrial simulation" +
                            " and overwrite the file?");
                    break;
                }
            }
        }
    }

    @Override
    public void runSimulation(int numThreads, boolean writeMetricsFile, Random randomProvider) {
        if (runDirect) {
            // Create contrail object
            TerrestrialContrail contrail;
            if (randomProvider == null) {
                contrail = new TerrestrialContrail(diffuseParams);
            } else {
                contrail = new TerrestrialContrail(diffuseParams, randomProvider);

                // Also set random provider of scattering phase function to mock object
                contrail.getScPhFun().setRandomProvider(randomProvider);
            }

            simulateDiffuseRadiation(contrail, numThreads, writeMetricsFile);
        }
    }
}
