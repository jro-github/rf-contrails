package de.tudresden.aerospace.contrails.Utility;

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
import de.tudresden.aerospace.contrails.Configuration.Parameters.DiffuseParameters;
import de.tudresden.aerospace.contrails.Configuration.PropertiesManager;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Utility class which provides helper methods related to I/O.
 */
public final class IOHelpers {
    /**
     * Private constructor to prevent creation of instance object.
     */
    private IOHelpers() {}

    /**
     * Finds all files in a directory, that match the given regular expression.
     *
     * @param dir The directory where files should be matched
     * @param regex The regular expression string to match files against
     * @return An array of matching files
     */
    public static File[] findMatchingFiles(File dir, String regex) {
        return dir.listFiles(f -> f.getName().matches(regex));
    }

    /**
     * Retrieves file handles to all simulation output files in a given directory with the given suffix.
     *
     * @param dir The directory to search in
     * @param prefix The prefix of the filename (can be used to distinguish based on output_file_prefix)
     * @param suffix The suffix of the filename (can be used to distinguish simulation type: solar_diff, solar_dir,
     *               terr_diff)
     * @return The array containing the {@link File} handles of found files
     */
    public static File[] getSimulationOutputFiles(File dir, String prefix, String suffix) {
        String regex = prefix + suffix + "_[+]?([0-9]*[.])?[0-9]+\\.csv";
        return findMatchingFiles(dir, regex);
    }

    /**
     * Parses the diffuse parameters from the comment string in a simulation output file.
     *
     * @param simOutFile The output file to parse parameters from
     * @return The {@link de.tudresden.aerospace.contrails.Configuration.Parameters.DiffuseParameters} object containing
     *         the read parameters
     */
    public static DiffuseParameters getDiffuseParametersFromFile(File simOutFile) {
        // Read simulation output file
        SimulationParser p = new SimulationParser(simOutFile, " ");
        return p.parseParametersOnly();
    }

    /**
     * Sorts an array of files by filename. This method modifies the given arrays directly.
     *
     * @param files The array of output files to match
     */
    public static void sortFilesByName(File[] files) {
        Arrays.sort(files, Comparator.comparing(File::getName));
    }

    /**
     * Asks the user to make a boolean decision by inputting Yes/yes (Y/y) or No/no (n/n) after a CLI prompt.
     *
     * @param prompt The prompt question
     * @return The boolean representing the user decision
     */
    public static boolean promptUserDecision(String prompt) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(prompt + " [Y/N]: ");
        try {
            String decision = br.readLine().toLowerCase();
            if (decision.equals("y") || decision.equals("yes"))
                return true;

            return false; // Nothing or invalid input means no (as it should be)
        } catch (IOException e) {
            System.out.println("Error reading user input: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tries to open a {@link FileWriter} to the given file.
     *
     * @param file The file to write to
     * @param terminate If {@code true}, the program is terminated on error, otherwise the error is only printed to
     *                  stderr
     * @return The {@link FileWriter} object on success or {@code null} on failure
     */
    public static FileWriter tryOpen(File file, boolean terminate) {
        try {
            FileWriter fwOut = new FileWriter(file);
            return fwOut;
        } catch (IOException e) {
            System.err.println("Error opening file " + file.getAbsolutePath() + " for writing");
            e.printStackTrace();

            if (terminate)
                System.exit(1);
        }

        return null;
    }

    /**
     * Wraps the {@link FileWriter#write(String)} method in a try-catch block. Exits on error.
     *
     * @param fw The {@link FileWriter} object used for writing
     * @param str The string to write
     */
    public static void tryWrite(FileWriter fw, String str) {
        try {
            fw.write(str);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Wraps the {@link FileWriter#close()} method in a try-catch block. Exits on error.
     *
     * @param fw The {@link FileWriter} to be closed
     */
    public static void tryClose(FileWriter fw) {
        try {
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Gets the absolute path string for a given path relative to {@code dir_resources_test}. Used for modifying the
     * output file prefix in tests.
     *
     * @param path The path relative to {@code dir_resources_test}
     * @return The absolute path string
     */
    public static String getTestResourcesPath(String path) {
        File dirResourcesTest = PropertiesManager.getInstance().getDirResourcesTest();

        if (path.startsWith(File.separator))
            path = path.substring(1);

        return new File(dirResourcesTest, path).getAbsolutePath();
    }
}
