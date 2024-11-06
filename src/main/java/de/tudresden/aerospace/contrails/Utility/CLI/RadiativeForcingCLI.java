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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import java.util.List;

/**
 * This class parses arguments related to the radiative forcing part of the program.
 */
public class RadiativeForcingCLI extends CLIArgs {
    private String directory;
    private String libradtranUVSpec;
    private String libradtranTwoStr;

    /**
     * Creates the CLI arguments object for the radiative forcing part.
     */
    public RadiativeForcingCLI() {
        super();

        Option oDirectory = new Option("d", "directory", true,
                "Simulation output file directory as either a path relative to dir_out " +
                        "or absolute path [required]");
        oDirectory.setRequired(true);
        options.addOption(oDirectory);

        Option oLibradtranUVSpec = new Option("u", "libradtran-uvspec", true,
                "Libradtran uvspec file [required]");
        oLibradtranUVSpec.setRequired(true);
        options.addOption(oLibradtranUVSpec);


        Option oLibradtranTwoStr = new Option("t", "libradtran-twostr", true,
                "Libradtran twostr file [required]");
        oLibradtranTwoStr.setRequired(true);
        options.addOption(oLibradtranTwoStr);

        // Part is added here since the whole argument string is passed, which contains p and parsing would stop
        // if p is not part of options.
        Option oPart = new Option("p", "part", true,
                "Specify which part of the simulation to run " +
                        "[0 - solar, 1 - terrestrial, 2 - both; default: both]");
        oPart.setType(Integer.class);
        options.addOption(oPart);
    }

    /**
     * Gets the Libradtran TwoStream solver output file name.
     *
     * @return The TwoStream file name
     */
    public String getLibradtranTwoStr() {
        return libradtranTwoStr;
    }

    /**
     * Gets the Libradtran UVSpec solver output file name.
     *
     * @return The UVSpec file name
     */
    public String getLibradtranUVSpec() {
        return libradtranUVSpec;
    }

    /**
     * Gets the simulation output directory.
     *
     * @return The simulation output directory
     */
    public String getDirectory() {
        return directory;
    }

    @Override
    protected void parseHook(CommandLine cmd) throws ParseException {
        directory = cmd.getOptionValue("d");
        libradtranUVSpec = cmd.getOptionValue("u");
        libradtranTwoStr = cmd.getOptionValue("t");
    }
}
