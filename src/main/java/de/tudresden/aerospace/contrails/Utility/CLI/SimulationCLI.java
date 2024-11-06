package de.tudresden.aerospace.contrails.Utility.CLI;

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

import org.apache.commons.cli.*;

/**
 * This class parses arguments related to the simulation part of the program.
 */
public class SimulationCLI extends CLIArgs {
    // Set default option values here
    private int numThreads = 1; // Default value is overridden at runtime by number of available logical processors
    private String configFileName = "";
    private boolean overwrite = false;
    private boolean writeMetricsFile = false;

    /**
     * Creates the CLI arguments object for the simulation part.
     */
    public SimulationCLI() {
        super();

        // Initialize options
        // If existing options are changed or new ones are added, the parse() method has to be adjusted
        // Internal variables and getters should also be modified or added accordingly
        Option oNumThreads = new Option("t", "threads", true,
                "Number of threads [optional; default: number of available logical processors]");
        oNumThreads.setRequired(false);
        oNumThreads.setType(Integer.class);
        options.addOption(oNumThreads);

        Option oConfigFileName = new Option("c", "config", true,
                "Configuration file name [required]");
        oConfigFileName.setRequired(true);
        options.addOption(oConfigFileName);

        Option oOverwrite = new Option("f", "force", false,
                "Force overwrite existing output files [optional; default: false]");
        oOverwrite.setRequired(false);
        options.addOption(oOverwrite);

        Option oWriteMetricsFile = new Option("m", "metrics", false,
                "Write metrics file [optional; default: false]");
        oWriteMetricsFile.setRequired(false);
        options.addOption(oWriteMetricsFile);

        // Part is added here since the whole argument string is passed, which contains p and parsing would stop
        // if p is not part of options.
        Option oPart = new Option("p", "part", true,
                "Specify which part of the simulation to run " +
                        "[0 - solar, 1 - terrestrial, 2 - both; default: both]");
        oPart.setType(Integer.class);
        options.addOption(oPart);
    }

    /**
     * Gets all CLI options.
     *
     * @return A mutable {@link Options} object with the CLI options
     */
    public Options getOptions() {
        return options;
    }

    /**
     * Gets the value of the {@code threads} CLI argument.
     *
     * @return The number of threads
     */
    public int getNumThreads() {
        return numThreads;
    }

    /**
     * Gets the value of the {@code config} CLI argument.
     *
     * @return The name of the config file
     */
    public String getConfigFileName() {
        return configFileName;
    }

    /**
     * Gets the value of the {@code force} CLI argument.
     *
     * @return A boolean indicating whether existing output files should be overwritten
     */
    public boolean doesOverwrite() {
        return overwrite;
    }

    /**
     * Gets the value of the {@code metrics} CLI argument.
     *
     * @return A boolean indicating whether a metrics file should be written
     */
    public boolean writesMetricsFile() {
        return writeMetricsFile;
    }

    @Override
    protected void parseHook(CommandLine cmd) throws ParseException {
        numThreads = cmd.getParsedOptionValue("t", Runtime.getRuntime().availableProcessors());

        if (numThreads <= 0)
            throw new ParseException("Number of threads must be greater than 0");

        configFileName = cmd.getOptionValue("c");
        overwrite = cmd.hasOption("f");
        writeMetricsFile = cmd.hasOption("m");
    }
}
