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

import de.tudresden.aerospace.contrails.Utility.MathHelpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Very basic parser for output files of <a href="https://www.libradtran.org">Libradtran</a>.
 *
 * @see <a href="http://libradtran.org/doc/libRadtran.pdf">Libradtran documentation PDF</a> for more information about
 * the output file format
 */
public class LibradtranParser {
    /**
     * Holds the parser state.
     */
    private enum State {
        HEADER, // Expecting to read header next
        COL, // Expecting to read table header next
        ROW // Expecting to read table row next
    }

    /**
     * Parses a libradtran output file, assuming the format of the file is uvspec output with the disort, sdisort or
     * spsdisort solver and with umu and phi specified.
     *
     * @param inputFile The file to parse
     * @return A {@link List} containing all {@link LibradtranBlock}s of the file in parsing order (top -> bottom)
     */
    public static List<LibradtranBlock> parseUVSpec(File inputFile) {
        int lineNo = 1;
        List<LibradtranBlock> res = new ArrayList<>();

        // Read input file line by line
        try {
            Scanner scanner = new Scanner(inputFile);

            State state = State.HEADER;
            LibradtranBlock currentBlock = new LibradtranBlock();
            while (scanner.hasNextLine()) {
                String l = scanner.nextLine();

                // Skip empty lines and comments
                if (l.trim().isEmpty() || l.trim().startsWith("//")) {
                    lineNo++;
                    continue;
                }

                // All values in the file should be doubles
                String[] vals = l.trim().split("\\s+");
                double[] nums = Arrays.stream(vals).mapToDouble(Double::parseDouble).toArray();

                if (state == State.ROW) {
                    // It is possible that we expect a row but the next block is starting
                    // This is the case if the line starts with two spaces
                    if (l.startsWith("  ")) {
                        res.add(currentBlock);
                        currentBlock = new LibradtranBlock();

                        // Expect table header next
                        state = State.HEADER;

                        // No continue here since this line has to be consumed by COL logic below

                    } else {
                        // Expect row
                        // First two values are umu and u0u
                        currentBlock.umu.add(nums[0]);
                        currentBlock.u0u.add(nums[1]);

                        // The other values are table values
                        List<Double> row = new ArrayList<>();
                        for (int i = 2; i < nums.length; i++) {
                            row.add(nums[i]);
                        }
                        currentBlock.uu.addRow(row);

                        // Expect another row next -> state remains unchanged here
                        lineNo++;
                        continue;
                    }
                }

                if (state == State.HEADER) {
                    // Expect block header
                    if (nums.length != 7)
                        throw new IOException("Expected 7 values, got " + nums.length);

                    currentBlock.lambda = nums[0];
                    currentBlock.edir = nums[1];
                    currentBlock.edn = nums[2];
                    currentBlock.eup = nums[3];
                    currentBlock.uavgdir = nums[4];
                    currentBlock.uavgdn = nums[5];
                    currentBlock.uavgup = nums[6];

                    // Expect table header next
                    state = State.COL;
                } else if (state == State.COL) {
                    // Expect table header (columns)

                    // DynamicTable only supports string column names. The parsed values have to be converted back
                    // to doubles by the user
                    currentBlock.uu.setColumnNames(Arrays.asList(vals));

                    // Expect table row(s) next
                    state = State.ROW;
                }

                lineNo++;
            }

            // Done, add last block
            res.add(currentBlock);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("Error reading file at line " + lineNo);
            e.printStackTrace();
        }

        return res;
    }

    /**
     * Parses a libradtran output file, assuming the format of the file is twostr or rodents with the twostr solver.
     *
     * @param inputFile The file to parse
     * @return A {@link List} containing all {@link LibradtranBlock}s of the file in parsing order (top -> bottom)
     */
    public static List<LibradtranBlock> parseTwoStream(File inputFile) {
        int lineNo = 1;
        List<LibradtranBlock> res = new ArrayList<>();

        // Read input file line by line
        try {
            Scanner scanner = new Scanner(inputFile);
            LibradtranBlock currentBlock = new LibradtranBlock();

            while (scanner.hasNextLine()) {
                String l = scanner.nextLine();

                // Skip empty lines and comments
                if (l.trim().isEmpty() || l.trim().startsWith("//")) {
                    lineNo++;
                    continue;
                }

                // All values in the file should be doubles
                String[] vals = l.trim().split("\\s+");
                double[] nums = Arrays.stream(vals).mapToDouble(Double::parseDouble).toArray();

                // File structure: <lambda edir edown eup uavg> in each line, for a range of lambdas
                if (nums.length == 5) {
                    currentBlock.lambda = nums[0];
                    currentBlock.edir = nums[1];
                    currentBlock.edn = nums[2];
                    currentBlock.eup = nums[3];
                    currentBlock.uavgdir = nums[4];

                    res.add(currentBlock);
                    currentBlock = new LibradtranBlock();

                }
                else
                    throw new IOException("Expected 5 values, got " + nums.length);

                lineNo++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("Error reading file at line " + lineNo);
            e.printStackTrace();
        }

        return res;
    }

    /**
     * Gets a block from the read blocks, which matches the given wavelength.
     *
     * @param parsedInput The {@link List} of {@link LibradtranBlock}s among which to search
     * @param lambda The wavelength to search for
     * @return The matching block if found or {@code null} if none was found
     */
    public static LibradtranBlock getBlockByLambda(List<LibradtranBlock> parsedInput, double lambda) {
        return parsedInput.stream().filter(
                block -> MathHelpers.compareDouble(block.lambda, lambda, 0.01)).findFirst().orElse(null);
    }

}
