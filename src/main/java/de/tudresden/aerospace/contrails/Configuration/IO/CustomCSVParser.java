package de.tudresden.aerospace.contrails.Configuration.IO;

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

import de.tudresden.aerospace.contrails.Configuration.PropertiesManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.Consumer;

/**
 * Very basic parser for physical parameters located in the {@code Parameter} folder. Files are formatted similarly to
 * ordinary CSV files, but with spaces instead of commas as separators and a single datatype for all table entries. A
 * single file can contain multiple named tables with the same column format, with the table names being denoted by a
 * single line above each table starting with {@code //TABLE}. Additionally, single-line comments denoted by {@code //}
 * at the start of the line are allowed.
 */
public class CustomCSVParser {

    /**
     * Parses a file with custom CSV format. Allows the user to specify custom logic for comments.
     *
     * @param file The file to parse
     * @param delimiter The separator between values
     * @param commentCallback A callback function that handles structured comments
     * @return A map with the names of the tables (or indices if unnamed) as keys and the tables as values
     */
    public static List<DynamicTable<Double>> parse(File file, String delimiter,
                                                   Consumer<String> commentCallback) {
        int lineNo = 1;
        List<String> header = new ArrayList<>();
        List<DynamicTable<Double>> res = new ArrayList<>();

        try {
            Scanner scanner = new Scanner(file);

            DynamicTable<Double> currentTable = new DynamicTable<>();
            while (scanner.hasNextLine()) {
                String l = scanner.nextLine();

                // Set table name
                if (l.startsWith("//TABLE")) {
                    String name = l.substring("//TABLE".length()).trim();
                    if (currentTable.getName().isEmpty()) {
                        // First table, set name
                        currentTable.setName(name);
                    } else {
                        // n-th table, append current table to results and create new table with name
                        res.add(currentTable);
                        currentTable = new DynamicTable<>();
                        currentTable.setName(name);
                    }
                    lineNo++;
                    continue;
                }

                // Skip empty lines
                if (l.trim().isEmpty()) {
                    lineNo++;
                    continue;
                }

                // Handle comments
                if (l.trim().startsWith("//")) {
                    // Call comment handler
                    if (commentCallback != null)
                        commentCallback.accept(l);
                    lineNo++;
                    continue;
                }

                // Read table values
                List<String> values = Arrays.asList(l.trim().split(delimiter));

                // Header has to be first in file
                if (header.isEmpty()) {
                    header.addAll(values);
                    lineNo++;
                    continue;
                }

                // Set table header if already read
                if (!currentTable.isInitialized() && !header.isEmpty()) {
                    currentTable.setColumnNames(header);
                }

                // Read values
                List<Double> row = new ArrayList<>();
                for (String s : values) {
                    Double d = Double.parseDouble(s);
                    row.add(d);
                }
                currentTable.addRow(row);

                lineNo++;
            }

            // Done, add last table
            res.add(currentTable);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error reading file at line " + lineNo);
            e.printStackTrace();
        }

        return res;
    }

    /**
     * Parses a file with custom CSV format.
     *
     * @param file The file to parse
     * @param delimiter The separator between values
     * @return A map with the names of the tables (or indices if unnamed) as keys and the tables as values
     */
    public static List<DynamicTable<Double>> parse(File file, String delimiter) {
        return parse(file, delimiter, null);
    }

    /**
     * Parses a file with custom CSV format (Format description see {@link CustomCSVParser}) in the {@code Parameter}
     * directory.
     *
     * @param fileName The name of the file to parse
     * @return A map with the names of the tables (or indices if unnamed) as keys and the tables as values
     */
    public static List<DynamicTable<Double>> parse (String fileName) {
        File file = new File(PropertiesManager.getInstance().getDirParameters(), fileName);
        return parse(file, "\\s+");
    }

    public static void main(String[] args) {
        // For testing purposes
        if (args.length < 1) {
            System.out.println("Please provide a CSV file path as argument");
            System.exit(1);
        }

        List<DynamicTable<Double>> tables = CustomCSVParser.parse(args[0]);
        for (DynamicTable<Double> table : tables) {
            System.out.println(table);
        }
    }
}
