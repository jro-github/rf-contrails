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

import de.tudresden.aerospace.contrails.Configuration.IO.*;
import de.tudresden.aerospace.contrails.Configuration.Parameters.CommonParameters;
import de.tudresden.aerospace.contrails.Configuration.Parameters.DiffuseParameters;
import de.tudresden.aerospace.contrails.Configuration.PropertiesManager;
import de.tudresden.aerospace.contrails.Utility.IOHelpers;
import de.tudresden.aerospace.contrails.Utility.MathHelpers;
import neureka.Tensor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * This class contains methods to compute the radiative forcing of a contrail. It takes the outputs of one or more
 * simulation runs (with different spectral indices / wavelengths) as well as Libradtran uvspec and twostream files as
 * inputs.
 */
public class RadiativeForcing {

    private final Tensor<Double> solarDirect;
    private final Tensor<Double> solarDiffuse;
    private final Tensor<Double> terrestrialDiffuse;
    private final List<LibradtranBlock> libradtranSolar, libradtranTerr;

    private final CommonParameters paramsCommon;
    private final DiffuseParameters paramsSolar, paramsSolarDir, paramsTerrestrial;

    private final SimulationParser solarParser, terrestrialParser;

    // Notice: for better readability, most of the variable names in this class contain underscores instead of being
    // written in conventional camelCase
    private final double lambda_sol_dir, lambda_sol_diff, lambda_terr_diff;

    // Helper variables
    private final double d_phi_sol, d_theta_sol, d_phi_terr, d_theta_terr;

    /**
     * Helper class which stores the results of calculation steps.
     */
    public class RFStepResult {
        /**
         * The result vector for upwards scattered photons.
         */
        public Tensor<Double> p_up;

        /**
         * The result vector for downwards scattered photons.
         */
        public Tensor<Double> p_down;

        /**
         * The result vector of absorbed photons.
         */
        public Tensor<Double> p_abs;

        /**
         * Initializes the step result with the given parameters.
         *
         * @param p_up The result vector for upwards scattered photons
         * @param p_down The result vector for downwards scattered photons
         * @param p_abs The result vector of absorbed photons
         */
        public RFStepResult(Tensor<Double> p_up, Tensor<Double> p_down, Tensor<Double> p_abs) {
            this.p_up = p_up;
            this.p_down = p_down;
            this.p_abs = p_abs;
        }

        /**
         * Performs an integration step by adding the given values to the current values. This modifies the internally
         * stored values. Normalization should be handled by the client.
         *
         * @param other The values to integrate
         */
        public void integralStep(RFStepResult other) {
            this.p_up = this.p_up.plus(other.p_up);
            this.p_down = this.p_down.plus(other.p_down);
            this.p_abs = this.p_abs.plus(other.p_abs);
        }
    }

    /**
     * Creates the {@link RadiativeForcing} object with the given parameters.
     *
     * @param solarDiffuse The simulation output file containing the diffuse solar part
     * @param solarDirect The simulation output file containing the direct solar part
     * @param terrestrialDiffuse The simulation output file containing the diffuse terrestrial part
     * @param libradtranUVSpec The Libradtran UVSpec output file
     * @param libradtranTwoStr The Libradtran TwoStream output file
     */
    public RadiativeForcing(File solarDiffuse, File solarDirect, File terrestrialDiffuse,
                            File libradtranUVSpec, File libradtranTwoStr) {

        // Parse output files: reuse CustomCSVParser and DynamicTable
        // Values in all files are separated by whitespaces
        String delimiter = "\\s+";

        // Single table files
        solarParser = new SimulationParser(solarDiffuse, delimiter);
        SimulationParser directParser = new SimulationParser(solarDirect, delimiter);
        terrestrialParser = new SimulationParser(terrestrialDiffuse, delimiter);

        solarParser.parse();
        directParser.parse();
        terrestrialParser.parse();

        // Convert tables
        int num_values = SimulationParser.TableIndex.values().length;
        this.solarDirect = directParser.getValueTensor(0);
        this.solarDiffuse = solarParser.getValueTensor(num_values);
        this.terrestrialDiffuse = terrestrialParser.getValueTensor(num_values);

        // Obtain parameters
        // Assume terrestrial and solar parts are conducted with the same number of photons and bins for now
        paramsCommon = solarParser.getCommonParameters();
        paramsSolar = solarParser.getDiffuseParameters();
        paramsSolarDir = directParser.getDiffuseParameters();
        paramsTerrestrial = terrestrialParser.getDiffuseParameters();

        // Libradtran output contains blocks
        this.libradtranSolar = LibradtranParser.parseUVSpec(libradtranUVSpec);
        this.libradtranTerr = LibradtranParser.parseTwoStream(libradtranTwoStr);

        // Lambdas in libradtran are given in nm, lambdas in this program are given in microns
        this.lambda_sol_dir = directParser.getDiffuseParameters().getLambda() * 1000.0;
        this.lambda_sol_diff = paramsSolar.getLambda() * 1000.0;
        this.lambda_terr_diff = paramsTerrestrial.getLambda() * 1000.0;

        // Terrestrial wavelengths may differ
        if (paramsSolarDir.getSpectralBandIndex() != paramsSolar.getSpectralBandIndex())
            throw new RuntimeException("solar direct and solar diffuse simulation output must have the same spectral " +
                    "band indices and wavelengths");

        // Compute helper variables
        d_phi_sol = 2 * Math.PI / paramsSolar.getBinsPhi();
        d_theta_sol = Math.PI / paramsSolar.getBinsTheta();
        d_phi_terr = 2 * Math.PI / paramsTerrestrial.getBinsPhi();
        d_theta_terr = Math.PI / paramsTerrestrial.getBinsTheta();
    }

    /**
     * Gets the spectral band index of the solar diffuse simulation output.
     *
     * @return The spectral band index as an {@code int}
     */
    public int getSpectralBandIndexSolDiff() {
        return paramsSolar.getSpectralBandIndex();
    }

    /**
     * Gets the spectral band index of the terrestrial diffuse simulation output.
     *
     * @return The spectral band index as an {@code int}
     */
    public int getSpectralBandIndexTerrDiff() {
        return paramsTerrestrial.getSpectralBandIndex();
    }

    /**
     * Gets the wavelength lambda of the solar direct simulation output.
     *
     * @return The wavelength as a {@code double}
     */
    public double getLambda_sol_dir() {
        return lambda_sol_dir;
    }

    /**
     * Gets the wavelength lambda of the solar diffuse simulation output.
     *
     * @return The wavelength as a {@code double}
     */
    public double getLambda_sol_diff() {
        return lambda_sol_diff;
    }

    /**
     * Gets the wavelength lambda of the terrestrial direct simulation output.
     *
     * @return The wavelength as a {@code double}
     */
    public double getLambda_terr_diff() {
        return lambda_terr_diff;
    }

    /**
     * Computes the powers of scattered and absorbed photons for the direct part of solar radiation. This part has no
     * downward scattering, therefore its values are zero.
     *
     * @return A {@link RFStepResult} containing upward scattered photon power, downward scattered photon power and
     * absorbed photon power
     */
    public RFStepResult calcSolarDirect() {
        /*
        P_up_dir (sza, phi0, lambda) = S_up_dir (sza, phi0, lambda) * I_dir (phi0, lambda)
        P_abs_dir (sza, phi0, lambda) = S_abs_dir (sza, phi0, lambda) * I_dir (phi0, lambda)
         */

        LibradtranBlock libradtranBlock = LibradtranParser.getBlockByLambda(libradtranSolar, lambda_sol_dir);

        // Block with lambda not found
        if (libradtranBlock == null)
            throw new RuntimeException(String.format("Lambda = %.2f not found in libradtran uvspec file",
                    lambda_sol_dir));

        double i_dir = libradtranBlock.edir;


        // Correction factor needs to be applied to account for direction
        // Converts N_XXX values into S_XXX values
        Tensor<Double> correction_factor = solarDirect.getAt(SimulationParser.TableIndex.CORRECTION_FACTOR.ordinal());

        // Index represents different theta values
        Tensor<Double> s_up_dir = solarDirect.getAt(SimulationParser.TableIndex.NUM_SCATTERED_UP.ordinal());
        s_up_dir = s_up_dir.multiply(i_dir).times(correction_factor);

        // Index represents different theta values
        Tensor<Double> s_abs_dir = solarDirect.getAt(SimulationParser.TableIndex.NUM_ABS.ordinal());
        s_abs_dir = s_abs_dir.multiply(i_dir).times(correction_factor);

        // These tensors currently hold single value
        Tensor<Double> p_down = Tensor.like(s_up_dir).all(0.0); // Direct part has no p_down -> zero
        return new RFStepResult(s_up_dir, p_down, s_abs_dir);
    }

    /**
     * Computes the powers of scattered and absorbed photons for the diffuse part of solar radiation.
     *
     * @return A {@link RFStepResult} containing upward scattered photon power, downward scattered photon power and
     * absorbed photon power
     */
    public RFStepResult calcSolarDiffuse(RFStepResult resultDirect) {
        /*
        P**_up (theta, phi, lambda) = S_up (theta, phi, lambda) * I_diff (theta, phi, lambda)* omega für 0<theta<=90 grad
        P**_abs (theta, phi, lambda) = S_abs (theta, phi, lambda) * I_diff (theta, phi, lambda) * omega für 0<theta<=180 grad
        P*_down (theta, phi, lambda) = S_up (theta, phi, lambda) * I_diff (theta, phi, lambda) * omega für 90<theta<=180 grad
         */

        // === Retrieve required values ===
        // Compute the following on lists instead of directly with tensor operations, as
        // different behavior is required depending on theta

        // Lists with angle pairs contain duplicates, such that iterating over both
        // at the same time yields all possible pairs
        int id = SimulationParser.TableIndex.THETA.ordinal();
        List<Double> thetas = solarParser.getRangeList(id, id + 1);
        id = SimulationParser.TableIndex.PHI.ordinal();
        List<Double> phis = solarParser.getRangeList(id, id + 1);

        id = SimulationParser.TableIndex.NUM_SCATTERED_UP.ordinal();
        List<Double> s_up = solarParser.getRangeList(id, id + 1);
        id = SimulationParser.TableIndex.NUM_ABS.ordinal();
        List<Double> s_abs = solarParser.getRangeList(id, id + 1);

        // Apply correction factor
        id = SimulationParser.TableIndex.CORRECTION_FACTOR.ordinal();
        List<Double> correction_factors = solarParser.getRangeList(id, id + 1);

        LibradtranBlock libradtranBlock = LibradtranParser.getBlockByLambda(libradtranSolar, lambda_sol_diff);

        // Block with lambda not found
        if (libradtranBlock == null)
            throw new RuntimeException(String.format("Lambda = %.2f not found in libradtran uvspec file",
                    lambda_sol_diff));

        // Direct part is added to index which most closely matches the angle
        // Make sure to specify sza and phi0 as close as possible to a combination of (theta, phi) in the diffuse output
        double sza = solarDirect.getDataAt(SimulationParser.TableIndex.THETA.ordinal());
        double phi0 = solarDirect.getDataAt(SimulationParser.TableIndex.PHI.ordinal());
        double min_delta = Double.MAX_VALUE;
        int min_index = -1;

        // === Calculate prerequisites ===
        List<Double> p_pp_up = new ArrayList<>();
        List<Double> p_pp_abs = new ArrayList<>();
        List<Double> p_p_down = new ArrayList<>();
        for (int i = 0; i < thetas.size(); i++) {
            double p = phis.get(i); // phi value
            double t = thetas.get(i); // theta value
            double u = s_up.get(i); // s_up value (phi, theta, lambda)
            double a = s_abs.get(i); // s_abs value (phi, theta, lambda)

            double o = Math.sin(t) * d_phi_sol * d_theta_sol; // omega value (theta)
            double f = correction_factors.get(i); // correction factor (phi, theta)

            // Retrieve matching radiances from libradtran for each pair (theta, phi)
            Double radiance = libradtranBlock.matchDiffuseRadiance(t, p, paramsCommon.getPsi());

            // Compute p**_up
            double td = Math.toDegrees(t);
            if (0.0 < td && td <= 90.0) {
                // P**_up (theta, phi, lambda) = S_up (theta, phi, lambda) * I_diff (theta, phi, lambda) * omega
                // for 0<theta<=90 degrees
                p_pp_up.add(u * f * o * radiance); // 👽
            } else {
                p_pp_up.add(0.0);
            }

            // Compute p*_down
            if (90.0 < td && td <= 180.0) {
                // P*_down (theta, phi, lambda) = S_up (theta, phi, lambda) * I_diff (theta, phi, lambda) * omega
                // for 90<theta<=180 degrees
                p_p_down.add(u * f * o * radiance);
            } else {
                p_p_down.add(0.0);
            }

            // Compute p**_abs for 0<theta<=180 degrees
            p_pp_abs.add(a * f * radiance * o);

            // Find (t, p) most closely matching (sza, phi0) to find the closest matching index for the direct part
            // Distance metric: absolute difference between sza and theta plus absolute difference between phi0 and p
            double distance = Math.abs(sza - t) + Math.abs(phi0 - p);
            if (distance < min_delta) {
                min_delta = distance;
                min_index = i;
            }
        }

        // Create tensors
        Tensor<Double> p_prime_prime_up = MathHelpers.oneDimfromList(p_pp_up);
        Tensor<Double> p_prime_prime_abs = MathHelpers.oneDimfromList(p_pp_abs);
        Tensor<Double> p_prime_down = MathHelpers.oneDimfromList(p_p_down);

        // === Combine prerequisites and direct parts to power tensors ===
        // Add direct value at the calculated index
        int id_up_dir = p_prime_prime_up.getNDConf().indexOfIndex(min_index);
        int id_abs_dir = p_prime_prime_abs.getNDConf().indexOfIndex(min_index);

        // P*_up (sza, phi0, lambda) = P_up_dir (sza, phi0, lambda) + P**_up (sza, phi0, lambda)
        Tensor<Double> p_prime_up = p_prime_prime_up.getMut().set(id_up_dir,
                p_prime_prime_up.getDataAt(id_up_dir) + resultDirect.p_up.getDataAt(0));

        // P*_abs (sza, phi0, lambda) = P_abs_dir (sza, phi0, lambda) + P**_abs (sza, phi0, lambda)
        Tensor<Double> p_prime_abs = p_prime_prime_abs.getMut().set(id_abs_dir,
                p_prime_prime_abs.getDataAt(id_abs_dir) + resultDirect.p_abs.getDataAt(0));

        return new RFStepResult(p_prime_up, p_prime_down, p_prime_abs);
    }

    /**
     * Computes the powers of scattered and absorbed photons for the diffuse part of terrestrial radiation.
     *
     * @return A {@link RFStepResult} containing upward scattered photon power, downward scattered photon power and
     * absorbed photon power
     */
    public RFStepResult calcTerrestrialDiffuse() {
        int id = SimulationParser.TableIndex.THETA.ordinal();
        List<Double> thetas = terrestrialParser.getRangeList(id, id + 1);
        id = SimulationParser.TableIndex.PHI.ordinal();
        List<Double> phis = terrestrialParser.getRangeList(id, id + 1);

        id = SimulationParser.TableIndex.NUM_SCATTERED_UP.ordinal();
        List<Double> s_up = terrestrialParser.getRangeList(id, id + 1);
        id = SimulationParser.TableIndex.NUM_ABS.ordinal();
        List<Double> s_abs = terrestrialParser.getRangeList(id, id + 1);

        // Apply correction factor
        id = SimulationParser.TableIndex.CORRECTION_FACTOR.ordinal();
        List<Double> correction_factors = terrestrialParser.getRangeList(id, id + 1);

        LibradtranBlock libradtranBlock = LibradtranParser.getBlockByLambda(libradtranTerr, lambda_terr_diff);

        // Block with lambda not found
        if (libradtranBlock == null)
            throw new RuntimeException(String.format("Lambda = %.2f not found in libradtran twostream file",
                    lambda_terr_diff));

        double f_up = libradtranBlock.eup;
        double f_down = libradtranBlock.edn;

        double n_phi = paramsTerrestrial.getBinsPhi();
        double n_theta = paramsTerrestrial.getBinsTheta();

        List<Double> p_p_up = new ArrayList<>();
        List<Double> p_p_abs = new ArrayList<>();
        List<Double> p_p_down = new ArrayList<>();
        for (int i = 0; i < thetas.size(); i++) {
            double t = thetas.get(i);
            double u = s_up.get(i);
            double a = s_abs.get(i);
            double o = Math.sin(t) * d_phi_terr * d_theta_terr;
            double f = correction_factors.get(i);

            // Compute p*_up
            double td = Math.toDegrees(t);
            if (0.0 < td && td <= 90.0) {
                // P*_up (theta, phi, lambda) = S_up (theta, phi, lambda) * F_down (lambda) / (n_phi*n_theta) * omega
                // for 0<theta<=90 degrees
                p_p_up.add(u * f * f_down / (n_phi * n_theta) * o);
            } else {
                p_p_up.add(0.0);
            }

            // Compute p*_down
            if (90.0 < td && td <= 180.0) {
                // P*_down (theta, phi, lambda) = S_up (theta, phi, lambda) * F_up (lambda) / (n_phi*n_theta) * omega
                // for 90<theta<=180 degrees
                p_p_down.add(u * f * f_up / (n_phi * n_theta) * o);
            } else {
                p_p_down.add(0.0);
            }

            // P*_abs (theta, phi, lambda) = S_abs (theta, phi, lambda) * F_down (lambda) / (n_phi*n_theta) * omega
            // + S_abs (theta, phi, lambda) * F_up (lambda) / (n_phi*n_theta) * omega
            // for 0<theta<=180 degrees
            p_p_abs.add(a * f * f_down / (n_phi * n_theta) * o);
        }

        // Create tensors
        Tensor<Double> p_prime_up = MathHelpers.oneDimfromList(p_p_up);
        Tensor<Double> p_prime_abs = MathHelpers.oneDimfromList(p_p_abs);
        Tensor<Double> p_prime_down = MathHelpers.oneDimfromList(p_p_down);

        return new RFStepResult(p_prime_up, p_prime_down, p_prime_abs);
    }

    /**
     * Calculates the radiative forcing from solar and terrestrial parts.
     *
     * @param rSolDiff The results for solar diffuse radiation, assuming the direct solar part to already be integrated
     * @param rTerrDiff The results for terrestrial diffuse radiation
     * @return
     */
    public static MathHelpers.Tuple<Double> calcRadiativeForcing(RFStepResult rSolDiff,
                                                          RFStepResult rTerrDiff) {
        // RF_sol = P_abs_sol + P_down_sol - P_up_sol
        Tensor<Double> rf_solar = rSolDiff.p_abs.plus(rSolDiff.p_down).minus(rSolDiff.p_up);

        // RF_terr = P_abs_terr + P_down_terr - P_up_terr
        Tensor<Double> rf_terrestrial = rTerrDiff.p_abs.plus(rTerrDiff.p_down).minus(rTerrDiff.p_up);

        // RF = RF_sol + RF_terr
        Tensor<Double> rf = rf_solar.plus(rf_terrestrial);

        // RF is sum of all RF values per angle [combination of (theta, phi)]
        return new MathHelpers.Tuple<>(rf_solar.sum().getDataAt(0), rf_terrestrial.sum().getDataAt(0),
                rf.sum().getDataAt(0));
    }

    /**
     * Writes a single line to the CSV output table. This line contains the intermediate result of an integration step.
     *
     * @param fw The {@link FileWriter} object to use for writing the line
     * @param lambda The wavelength of the input used to calculate results
     * @param part The part of the simulation as one of {@code sol_dir}, {@code sol_diff} or {@code terr_diff}.
     * @param step The integrated result values
     * @param rf_sol The radiative forcing of the solar part
     * @param rf_terr The radiative forcing of the terrestrial part
     * @param rf_total The total radiative forcing (solar part + terrestrial part)
     */
    private static void writeOutputLine(FileWriter fw, Double lambda, String part, RFStepResult step,
                                        Double rf_sol, Double rf_terr, Double rf_total) {
        String p_up = "0.0";
        String p_down = "0.0";
        String p_abs = "0.0";
        if (step != null) {
            p_up = MathHelpers.sum(step.p_up).toString();
            p_down = MathHelpers.sum(step.p_down).toString();
            p_abs = MathHelpers.sum(step.p_abs).toString();
        }

        IOHelpers.tryWrite(fw, String.join(", ",
                Double.toString(lambda),
                part,
                p_up,
                p_down,
                p_abs,
                rf_sol.toString(),
                rf_terr.toString(),
                rf_total.toString()) + "\n");
    }

    /**
     * Performs the integration over different wavelengths for all output files in the given directory. The output files
     * should contain outputs for simulation runs with different spectral band indices / wavelengths.
     *
     * @param dir The directory, in which the output files are stored
     * @param libradtranUVSpecName The output of the Libradtran UVSpec solver
     * @param libradtranTwoStreamName The output of the Libradtran TwoStream solver
     * @throws IOException If the specified directory or the given Libradtran files cannot be read
     */
    public static void integrateLambda(File dir, String libradtranUVSpecName,
                                       String libradtranTwoStreamName) throws IOException {
        if (dir == null || !dir.isDirectory())
            throw new FileNotFoundException("directory" + dir.getAbsolutePath() + " must be a valid directory");

        File[] fSolDiff = IOHelpers.getSimulationOutputFiles(dir, "^.*",
                SolarSimulation.suffixDiffuseSolar);
        File[] fSolDir = IOHelpers.getSimulationOutputFiles(dir, "^.*",
                SolarSimulation.suffixDirect);
        File[] fTerrDiff = IOHelpers.getSimulationOutputFiles(dir, "^.*",
                TerrestrialSimulation.suffixDiffuseTerrestrial);

        integrateLambda(fSolDir, fSolDiff, fTerrDiff, libradtranUVSpecName, libradtranTwoStreamName, dir);
    }

    /**
     * Performs the integration over different wavelengths for all output files in the given arrays. The output files
     * should contain outputs for simulation runs with different spectral band indices / wavelengths. All arrays should
     * be of the same size
     *
     * @param solarDir An array containing the simulation outputs of the direct solar part for each wavelength
     * @param solarDiff An array containing the simulation outputs of the diffuse solar part for each wavelength
     * @param terrDiff An array containing the simulation outputs of the diffuse terrestrial part for each wavelength
     * @param libradtranUVSpecName The libradtran UVSpec output
     * @param libradtranTwoStreamName The libradtran TwoStream output
     * @param dir_out The directory to write the output file to
     * @throws IOException If any of the files cannot be read
     */
    public static void integrateLambda(File[] solarDir, File[] solarDiff, File[] terrDiff, String libradtranUVSpecName,
                                       String libradtranTwoStreamName, File dir_out) throws IOException {
        // Load input files and prepare result lists
        PropertiesManager pm = PropertiesManager.getInstance();
        File libradtranUVSpec = new File(pm.getDirLibradtranOut(), libradtranUVSpecName);
        File libradtranTwoStream = new File(pm.getDirLibradtranOut(), libradtranTwoStreamName);
        List<RFStepResult> resSolarDiffuse = new ArrayList<>();
        List<RFStepResult> resSolarDirect = new ArrayList<>();
        List<RFStepResult> resTerrDiffuse = new ArrayList<>();

        // Serialize results in proper CSV, overwrite by default
        // Format: lambda, part [sol_diff, sol_dir, terr_diff, total], p_up, p_down, p_abs, rf_sol, rf_terr, rf_total
        // Final line has lambda = -1.0 and contains integrated results, with rf_solar and rf_terr in p_up and p_down
        // respectively
        if (dir_out == null || !dir_out.isDirectory())
            throw new FileNotFoundException("directory" + dir_out.getAbsolutePath() + " must be a valid directory");

        File fOut = new File(dir_out, "radiative_forcing.csv");
        FileWriter fwOut = IOHelpers.tryOpen(fOut, true);

        // CSV header
        IOHelpers.tryWrite(fwOut, "lambda, part, p_up, p_down, p_abs, rf_sol, rf_terr, rf_total\n");

        // Make sure numbers of files match
        if (solarDiff.length != solarDir.length || solarDir.length != terrDiff.length)
            throw new RuntimeException("Found " + solarDir.length + " solar direct files, " + solarDiff.length +
                    " solar diffuse files and " + terrDiff.length + " terrestrial diffuse files. It is required that" +
                    " the numbers of files are equal for integration over lambdas.");

        // Ensure prefixes match between indices (code assumes same prefix = same spectral_band_index for
        // solar direct and solar terrestrial part)
        // This also ensures matching terrestrial and solar parts according to the used config file, given that
        // the directory contains matchin prefixes for all types of simulation output files
        IOHelpers.sortFilesByName(solarDir);
        IOHelpers.sortFilesByName(solarDiff);
        IOHelpers.sortFilesByName(terrDiff);

        // Requires same number of simulation output files for each type!
        Set<Integer> seenIndicesSol = new HashSet<>();
        Set<Integer> seenIndicesTerr = new HashSet<>();
        for (int i = 0; i < solarDiff.length; i++) {
            // Get all output files
            File sDir = solarDir[i];
            File sDiff = solarDiff[i];
            File tDiff = terrDiff[i];

            // Create RF objects and calculate results
            System.out.printf("Calculating RF results for the given wavelengths - %d / %d\n", i + 1, solarDiff.length);
            RadiativeForcing rf = new RadiativeForcing(sDiff, sDir, tDiff, libradtranUVSpec, libradtranTwoStream);

            // If a new file contains a spectral band index that was already integrated, something went wrong
            int i_sol_diff = rf.getSpectralBandIndexSolDiff();
            int i_terr_diff = rf.getSpectralBandIndexTerrDiff();
            if (seenIndicesSol.contains(i_sol_diff))
                throw new RuntimeException("Duplicate spectral_band_index found in " + sDiff + ". " +
                        "Integration requires unique wavelengths for each output file");

            if (seenIndicesTerr.contains(i_terr_diff))
                throw new RuntimeException("Duplicate spectral_band_index found in " + tDiff + ". " +
                        "Integration requires unique wavelengths for each output file");

            seenIndicesSol.add(i_sol_diff);
            seenIndicesTerr.add(i_terr_diff);

            try {
                RFStepResult rSolDir = rf.calcSolarDirect();
                resSolarDirect.add(rSolDir);

                RFStepResult rSolDiff = rf.calcSolarDiffuse(rSolDir);
                resSolarDiffuse.add(rSolDiff);

                RFStepResult rTerrDiff = rf.calcTerrestrialDiffuse();
                resTerrDiffuse.add(rTerrDiff);

                MathHelpers.Tuple<Double> valRF = RadiativeForcing.calcRadiativeForcing(rSolDiff, rTerrDiff);

                System.out.printf("Results for lambda_sol = %.2f nm and lambda_terr = %.2f nm:\n",
                        rf.getLambda_sol_diff(), rf.getLambda_terr_diff());
                System.out.printf("Solar       RF = %g\n", valRF.get(0));
                System.out.printf("Terrestrial RF = %g\n", valRF.get(1));
                System.out.printf("Total       RF = %g\n\n", valRF.get(2));

                // Write results to file
                writeOutputLine(fwOut, rf.getLambda_sol_dir(), "sol_dir", rSolDir, 0.0, 0.0,
                        0.0);
                writeOutputLine(fwOut, rf.getLambda_sol_diff(), "sol_diff", rSolDiff, valRF.get(0), 0.0,
                        valRF.get(2));
                writeOutputLine(fwOut, rf.getLambda_terr_diff(), "terr_diff", rTerrDiff, 0.0, valRF.get(1),
                        valRF.get(2));
            } catch (RuntimeException e) {
                e.printStackTrace();
                fwOut.close();

                // Tensor calculations start background thread which does not terminate when main ends
                System.exit(1);
            }
        }

        RFStepResult integralSolar = resSolarDiffuse.get(0);
        RFStepResult integralTerrestrial = resTerrDiffuse.get(0);
        if (solarDiff.length > 1) {
            // Integrate results over lambda
            System.out.println("Integrating results...");

            for (int i = 1; i < resSolarDiffuse.size(); i++) {
                // Solar integral
                integralSolar.integralStep(resSolarDiffuse.get(i));

                // Terrestrial integral
                integralTerrestrial.integralStep(resTerrDiffuse.get(i));
            }

            // RF calculation
            MathHelpers.Tuple<Double> rf = RadiativeForcing.calcRadiativeForcing(integralSolar, integralTerrestrial);

            System.out.printf("Integrated results:\n");
            System.out.printf("Solar       RF = %g\n", rf.get(0));
            System.out.printf("Terrestrial RF = %g\n", rf.get(1));
            System.out.printf("Total       RF = %g\n\n", rf.get(2));

            // Write result to file
            writeOutputLine(fwOut, 0.0, "total", null, rf.get(0), rf.get(1), rf.get(2));
            IOHelpers.tryClose(fwOut);
        }
    }

    public static void main(String[] args) {
        // This main method illustrates how to use RadiativeForcing after a simulation has completed
        // The program also provides a CLI parameter for this
        File dir = PropertiesManager.getInstance().getDirOutput();
        try {
            RadiativeForcing.integrateLambda(dir, "solar_120621_0500_055.out",
                    "thermal120621_albedo01.out");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0); // Tensor calculations start background thread which does not terminate when main ends
    }
}
