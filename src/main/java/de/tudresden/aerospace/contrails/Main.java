package de.tudresden.aerospace.contrails;

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
import de.tudresden.aerospace.contrails.MonteCarlo.RadiativeForcing;
import de.tudresden.aerospace.contrails.MonteCarlo.Simulation;
import de.tudresden.aerospace.contrails.MonteCarlo.SolarSimulation;
import de.tudresden.aerospace.contrails.MonteCarlo.TerrestrialSimulation;
import de.tudresden.aerospace.contrails.Utility.CLI.CommonCLIArgs;
import de.tudresden.aerospace.contrails.Utility.CLI.RadiativeForcingCLI;
import de.tudresden.aerospace.contrails.Utility.CLI.SimulationCLI;

import java.io.File;
import java.io.IOException;

/**
 * This class provides the entry point for both the simulation and radiative forcing calculation and handles CLI args.
 */
public final class Main {
    /**
     * This is the main method of the simulation. Behavior varies depending on the selected mode (via CLI arguments).
     * In the simulation modes, the simulation is run and terrestrial and/or solar parts for direct and diffuse
     * radiation are computed. In the RF mode, the radiative forcing is computed based on simulation results.
     *
     * @param args The command line arguments for the simulation
     */
    public static void main(String[] args) {
        // Parse CLI arguments
        CommonCLIArgs cli = new CommonCLIArgs();
        cli.parse(args);

        // Parse arguments specific to selected mode
        // === SIMULATION MODE ===
        if (cli.isSimulation()) {
            String part = cli.getPart();

            SimulationCLI simArgs = new SimulationCLI();
            simArgs.parse(args);

            XMLParameters params = XMLParameters.unmarshal(simArgs.getConfigFileName());
            params.check();

            Simulation sim;
            if (part.equals(CommonCLIArgs.PART_SOLAR)) {
                params.getSolarDiffuse().check();
                sim = new SolarSimulation(params);
                sim.promptCheckOutputFiles(simArgs.doesOverwrite());
                sim.runSimulation(simArgs.getNumThreads(), simArgs.writesMetricsFile(), null);
            } else if (part.equals(CommonCLIArgs.PART_TERRESTRIAL)) {
                params.getTerrestrialDiffuse().check();
                sim = new TerrestrialSimulation(params);
                sim.promptCheckOutputFiles(simArgs.doesOverwrite());
                sim.runSimulation(simArgs.getNumThreads(), simArgs.writesMetricsFile(), null);
            } else {
                params.getSolarDiffuse().check();
                params.getTerrestrialDiffuse().check();

                sim = new TerrestrialSimulation(params);
                sim.promptCheckOutputFiles(simArgs.doesOverwrite());

                // Prompt for overwrite on both first
                SolarSimulation sim_solar = new SolarSimulation(params);
                sim_solar.promptCheckOutputFiles(simArgs.doesOverwrite());

                // Run both
                sim.runSimulation(simArgs.getNumThreads(), simArgs.writesMetricsFile(), null);
                sim_solar.runSimulation(simArgs.getNumThreads(), simArgs.writesMetricsFile(), null);
            }
        }
        // === RADIATIVE FORCING MODE ===
        else if (cli.getPart().equals(CommonCLIArgs.PART_RF)) {
            RadiativeForcingCLI rfArgs = new RadiativeForcingCLI();
            rfArgs.parse(args);

            // args: -d, -u, -t
            File directory = new File(rfArgs.getDirectory());
            if (!directory.isDirectory()) {
                // If directory (-d) is not an absolute path, try relative path to dir_out
                directory = new File(PropertiesManager.getInstance().getDirOutput(), rfArgs.getDirectory());
                if (!directory.isDirectory()) {
                    System.err.println("The given directory (-d) could not be found under " + directory.getAbsolutePath());
                    System.exit(1);
                }
            }

            try {
                // Calculate radiative forcing
                RadiativeForcing.integrateLambda(directory, rfArgs.getLibradtranUVSpec(),
                        rfArgs.getLibradtranTwoStr());
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            System.exit(0); // Tensor calculations start background thread which does not terminate when main ends
        }
    }
}
