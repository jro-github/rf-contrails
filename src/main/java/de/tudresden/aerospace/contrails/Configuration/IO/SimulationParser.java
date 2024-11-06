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

import de.tudresden.aerospace.contrails.Configuration.Parameters.CommonParameters;
import de.tudresden.aerospace.contrails.Configuration.Parameters.DiffuseParameters;
import de.tudresden.aerospace.contrails.Configuration.Parameters.DirectParameters;
import de.tudresden.aerospace.contrails.Configuration.Parameters.ParameterNames;
import neureka.Tensor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

/**
 * Parses an output file of the simulation. The output format can be parsed with {@link CustomCSVParser}, but parameters
 * of the simulation are serialized to comments and need additional parsing logic, which this class provides.
 */
public class SimulationParser {
    /**
     * Reflects the column index of the respective value in the simulation output file
     */
    public enum TableIndex {
        THETA,
        PHI,
        NUM_ABS,
        NUM_SCATTERED,
        NUM_SCATTERED_UP,
        NUM_SCATTERED_DOWN,
        CORRECTION_FACTOR,
        AVG_SCATTERED,
        NUM_AFFECTED
    }

    private final File simulationFile;
    private final String delimiter;
    private CommonParameters commonParameters;
    private DiffuseParameters diffuseParameters;
    private DirectParameters directParameters;

    private DynamicTable<Double> parseResult = null;

    /**
     * Creates the parser for the given simulation file.
     *
     * @param simulationFile The simulation file to parse
     * @param delimiter The character sequence that separates values in the file
     */
    public SimulationParser(File simulationFile, String delimiter) {
        this.simulationFile = simulationFile;
        this.delimiter = delimiter;

        commonParameters = new CommonParameters();
        diffuseParameters = new DiffuseParameters();
        directParameters = new DirectParameters();
    }

    /**
     * Gets the parse result after parsing with {@link SimulationParser#parse()}.
     *
     * @return The parse result or {@code null} if the file has not been parsed yet
     */
    public DynamicTable<Double> getParseResult() {
        return parseResult;
    }

    /**
     * Gets the flattened parsed data table from the parse result, only containing columns within the given range.
     *
     * @param columnStartIndex The start index (inclusive) of the column range to include in the result
     * @param columnEndIndex The end index (non-inclusive) of the column range to include in the result
     * @return The flattened parsed data table within the given column range, as a flattened {@link List}
     */
    public List<Double> getRangeList(int columnStartIndex, int columnEndIndex) {
        if (parseResult == null)
            return null;

        return parseResult.getFlattened(columnStartIndex, columnEndIndex);
    }

    /**
     * Gets the parsed data table from the parse result, only containing columns within the given range.
     *
     * @param columnStartIndex The start index (inclusive) of the column range to include in the result
     * @param columnEndIndex The end index (non-inclusive) of the column range to include in the result
     * @return The parsed data table within the given column range, as a {@link Tensor} which matches the table shape
     */
    public Tensor<Double> getRangeTensor(int columnStartIndex, int columnEndIndex) {
        if (parseResult == null)
            return null;

        List<Double> flattenedValues = parseResult.getFlattened(columnStartIndex, columnEndIndex);

        return Tensor.ofDoubles()
                .withShape(parseResult.getRows().size(), columnEndIndex - columnStartIndex)
                .andFill(flattenedValues);
    }

    /**
     * Gets the parsed data table from the parse result, only containing columns from the given start index to the end.
     *
     * @param columnStartIndex The start index (inclusive) of the column range to include in the result
     * @return The parsed data table within the given column range, as a {@link Tensor} which matches the table shape
     */
    public Tensor<Double> getValueTensor(int columnStartIndex) {
        return getRangeTensor(columnStartIndex, parseResult.getColumnNames().size());
    }

    /**
     * Gets the theta column in the parsed data as a tensor.
     *
     * @return The {@link Tensor} with the theta column data or {@code null} if the file has not been parsed yet
     */
    public Tensor<Double> getThetaTensor() {
        int idTheta = TableIndex.THETA.ordinal();
        return getRangeTensor(idTheta, idTheta + 1);
    }

    /**
     * Gets the phi column in the parsed data as a tensor.
     *
     * @return The {@link Tensor} with the phi column data or {@code null} if the file has not been parsed yet
     */
    public Tensor<Double> getPhiTensor() {
        int idPhi = TableIndex.PHI.ordinal();
        return getRangeTensor(idPhi, idPhi + 1);
    }

    /**
     * Gets the common parameters of the parsed file.
     *
     * @return The {@link CommonParameters} or {@code null} if the file has not been parsed yet
     */
    public CommonParameters getCommonParameters() {
        return commonParameters;
    }

    /**
     * Gets the diffuse parameters of the parsed file.
     *
     * @return The {@link DiffuseParameters} or {@code null} if the file has not been parsed yet
     */
    public DiffuseParameters getDiffuseParameters() {
        return diffuseParameters;
    }

    /**
     * Gets the direct parameters of the parsed file.
     *
     * @return The {@link DirectParameters} or {@code null} if the file has not been parsed yet
     */
    public DirectParameters getDirectParameters() {
        return directParameters;
    }

    /**
     * Parses the simulation file and returns the parsed result table. Also initializes the parameters from specially
     * formatted comment strings, which can then be retrieved with {@link SimulationParser#getCommonParameters()},
     * {@link SimulationParser#getDirectParameters()} and {@link SimulationParser#getDiffuseParameters()}.
     *
     * @return The parsed {@link DynamicTable}
     */
    public DynamicTable<Double> parse() {
        DynamicTable<Double> table = CustomCSVParser.parse(simulationFile, delimiter, this::commentHandler).get(0);
        parseResult = table;

        return parseResult;
    }

    /**
     * Parses only the parameters of the simulation file. For quickly checking parameters without parsing file contents.
     *
     * @return The {@link DiffuseParameters} object containing the diffuse parameters of the simulation file
     */
    public DiffuseParameters parseParametersOnly() {
        try{
            Scanner scanner = new Scanner(simulationFile);

            while(!diffuseParameters.isInitialized()) {
                String l = scanner.nextLine();
                commentHandler(l);
            }

            return diffuseParameters;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Handles structured comment strings in the form of {@code // var = value}, which occur during parsing.
     *
     * @param comment The comment string to parse
     */
    private void commentHandler(String comment) {
        if (comment != null && !comment.isEmpty()) {
            comment = comment.trim();
            if (comment.startsWith("//") && comment.contains("=")) {
                comment = comment.substring(2).replaceAll("\\s+", "");
                String[] arr = comment.split("=");

                // Valid assignment, parse value
                if (arr.length == 2) {
                    switch (arr[0]) {
                        case ParameterNames.NUM_PHOTONS:
                            commonParameters.setNumPhotons(Integer.parseInt(arr[1]));
                            break;
                        case ParameterNames.PSI:
                            commonParameters.setPsi(Double.parseDouble(arr[1]));
                            break;
                        case ParameterNames.BINS_PHI:
                            diffuseParameters.setBinsPhi(Integer.parseInt(arr[1]));
                            break;
                        case ParameterNames.BINS_THETA:
                            diffuseParameters.setBinsTheta(Integer.parseInt(arr[1]));
                            break;
                        case ParameterNames.RESOLUTION_S:
                            diffuseParameters.setResolutionS(Integer.parseInt(arr[1]));
                            break;
                        case ParameterNames.DISTANCE:
                            diffuseParameters.setDistance(Double.parseDouble(arr[1]));
                            break;
                        case ParameterNames.SPECTRAL_BAND_INDEX:
                            diffuseParameters.setSpectralBandIndex(Integer.parseInt(arr[1]));
                            break;
                        case ParameterNames.G:
                            diffuseParameters.setG(Double.parseDouble(arr[1]));
                            break;
                        case ParameterNames.ABSORPTION_FACTOR:
                            diffuseParameters.setAbsorptionFactor(Double.parseDouble(arr[1]));
                            break;
                        case ParameterNames.SCATTERING_FACTOR:
                            diffuseParameters.setScatteringFactor(Double.parseDouble(arr[1]));
                            break;
                        case ParameterNames.LAMBDA:
                            diffuseParameters.setLambda(Double.parseDouble(arr[1]));
                            break;
                        case ParameterNames.RADIUS_INCIDENT:
                            diffuseParameters.setIncidentRadius(Double.parseDouble(arr[1]));
                            break;
                        case ParameterNames.RADIUS_DROPLET:
                            diffuseParameters.setDropletRadius(Double.parseDouble(arr[1]));
                            break;
                        case ParameterNames.NUM_SCA:
                            diffuseParameters.setNumSca(Integer.parseInt(arr[1]));
                            break;
                        case ParameterNames.SIGMA_H:
                            diffuseParameters.setSigmaH(Double.parseDouble(arr[1]));
                            break;
                        case ParameterNames.SIGMA_V:
                            diffuseParameters.setSigmaV(Double.parseDouble(arr[1]));
                            break;
                        case ParameterNames.SIGMA_S:
                            diffuseParameters.setSigmaS(Double.parseDouble(arr[1]));
                            break;
                        case ParameterNames.NUM_ICE:
                            diffuseParameters.setNumIceParticles(Double.parseDouble(arr[1]));
                            break;
                        case ParameterNames.SZA:
                            directParameters.setSza(Double.parseDouble(arr[1]));
                            break;
                        case ParameterNames.PHI0:
                            directParameters.setPhi0(Double.parseDouble(arr[1]));
                            break;
                    }
                }
            }
        }
    }
}
