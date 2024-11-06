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
 * This class provides an interface for parsing command-line arguments via {@link CLIArgs#parse(String[])}. Subclasses
 * can add parsed options via the template method pattern.
 */
public abstract class CLIArgs {
    protected final Options options;

    /**
     * Initializes the internal {@link Options} object.
     */
    protected CLIArgs() {
        this.options = new Options();
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
     * Parses the given CLI argument array and initializes the values of this object's internal {@link Options}. Call
     * this method before calling the getters, otherwise default options will be returned.
     *
     * @param args The CLI arguments to parse
     */
    public final void parse(String[] args) {
        CommandLineParser parser = new DefaultParser(false);
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args, true); // Stop at non-option to allow multiple parse stages

            // Subclasses can parse arguments here
            parseHook(cmd);

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Simulation", options);

            System.exit(1);
        }
    }

    /**
     * T&H hook method to be implemented by subclasses. Allows subclasses to define custom parse behavior.
     *
     * @param cmd The command line arguments
     */
    protected abstract void parseHook(CommandLine cmd) throws ParseException;
}
