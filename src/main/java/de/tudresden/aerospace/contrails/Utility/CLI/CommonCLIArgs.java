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

import java.util.ArrayList;
import java.util.List;

/**
 * This class parses common CLI arguments and allows clients to implement different behavior depending on the
 * {@link CommonCLIArgs#part} parameter.
 */
public class CommonCLIArgs extends CLIArgs {
    // Simulation modes
    public static final String PART_SOLAR = "sol";
    public static final String PART_TERRESTRIAL = "terr";
    public static final String PART_BOTH = "both";

    // Other modes
    public static final String PART_RF = "rf"; // Radiative forcing

    private final Option oPart;
    private final List<String> parts;
    private String part;

    /**
     * Creates the CLI arguments object for arguments common to both the simulation and radiative forcing parts.
     */
    public CommonCLIArgs() {
        super();
        this.parts = new ArrayList<>();
        parts.add(PART_SOLAR);
        parts.add(PART_TERRESTRIAL);
        parts.add(PART_BOTH);
        parts.add(PART_RF);

        // Initialize options
        // If existing options are changed or new ones are added, the parse() method has to be adjusted
        // Internal variables and getters should also be modified or added accordingly
        oPart = new Option("p", "part", true,
                "Specify which part of the simulation to run [" +
                        PART_SOLAR + " - solar simulation, " +
                        PART_TERRESTRIAL + " - terrestrial simulation, " +
                        PART_BOTH + " - both simulation parts; default: both]");
        options.addOption(oPart);
    }

    /**
     * Gets the part of the simulation the user wants to run.
     *
     * @return The string representing the part of the simulation
     */
    public String getPart() {
        return part;
    }

    public boolean isSimulation() {
        return part.equals(PART_SOLAR) || part.equals(PART_TERRESTRIAL) || part.equals(PART_BOTH);
    }

    @Override
    protected void parseHook(CommandLine cmd) throws ParseException {
        part = cmd.getOptionValue("p", "both");
        if (!parts.contains(part))
            throw new ParseException("Invalid value for argument " + oPart.getArgName() + ": " + part);
    }
}
