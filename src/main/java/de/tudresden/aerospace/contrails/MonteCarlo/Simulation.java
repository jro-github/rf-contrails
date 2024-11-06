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

import de.tudresden.aerospace.contrails.Configuration.IO.DynamicTable;
import de.tudresden.aerospace.contrails.Configuration.Parameters.CommonParameters;
import de.tudresden.aerospace.contrails.Configuration.Parameters.DiffuseParameters;
import de.tudresden.aerospace.contrails.Configuration.Parameters.DirectParameters;
import de.tudresden.aerospace.contrails.Configuration.PropertiesManager;
import de.tudresden.aerospace.contrails.Modeling.*;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public abstract class Simulation {
    public static final String suffixDiffuseSolar = "_solar_diffuse";
    public static final String suffixDiffuseTerrestrial = "_terrestrial_diffuse";

    /**
     * Suffix for diffuse output file. Designed to be re-defined by subclasses.
     */
    protected String suffixDiffuse = "_diffuse";

    /**
     * Set by {@link Simulation#promptCheckOutputFiles(boolean)}
     */
    protected boolean runDirect = true;

    /**
     * Inner class that stores the results of a single direction calculation step. It is used as the result type of a
     * future object.
     */
    protected static class SingleStepResult {
        public int threadID;
        public double theta, phi; // Passes the angle for which the calculation was done
        public int[] scattered_bins; // Stores number of scattered photons for each bin
        public int nScat = 0; // Number of photons that were scattered
        public int nAbs = 0; // Number of photons that were absorbed
        public int nTrans = 0; // Number of photons that passed through without interaction
        public int nScatUp = 0; // Number of photons that were scattered back towards the sky
        public int nScatDown = 0; // Number of photons that were scattered towards earth
        public int nScatMultiple = 0; // Number of photons that were scattered multiple times
        public double avgScattering = 0.0; // Average scattering probability

        public SingleStepResult(int threadID, double theta, double phi) {
            this.threadID = threadID;
            this.theta = theta;
            this.phi = phi;
        }
    }

    /**
     * Inner class which implements Callable, so that a single step can be executed in a thread.
     */
    protected class ComputeSingleStep implements Callable<SingleStepResult> {
        int taskID;
        Contrail contrail;

        int numPhotons;
        int resolution_S;
        double theta;
        double phi;

        /**
         * Constructs the callable for a single simulation step.
         *
         * @param contrail     The contrail object that performs the integration for a single photon
         * @param numPhotons   The number of photons to simulate
         * @param resolution_S The resolution of the result (lower number = more bins)
         * @param theta        The angle to the z-axis in the contrail coordinate system
         * @param phi          The angle to the x-axis in the contrail coordinate system
         */
        public ComputeSingleStep(int taskID, Contrail contrail, int numPhotons, int resolution_S, double theta, double phi) {
            this.taskID = taskID;
            this.contrail = contrail;
            this.numPhotons = numPhotons;
            this.resolution_S = resolution_S;
            this.theta = theta;
            this.phi = phi;
        }

        /**
         * Performs a single iteration of the Monte Carlo simulation, which consists of simulating radiation by
         * considering a fixed number of photons for a single angle of incidence.
         *
         * @return A {@link SingleStepResult} object that contains all result variables
         */
        @Override
        public SingleStepResult call() {
            // Create result variables
            SingleStepResult res = new SingleStepResult(taskID, theta, phi);

            res.scattered_bins = new int[180 / resolution_S];

            // Run single simulation step for n photons over a single angle of incidence
            RePhoton re;
            for (int i = 0; i < numPhotons; i++) {

                // Perform integration of a single photon
                re = contrail.singlePhotonIntegration(theta, phi);

                // Theta < 0 means absorption by convention
                if (re.theta < 0) {
                    res.nAbs += 1;
                } else {
                    // If the photon was not scattered it went through the contrail without hitting a particle
                    if (re.Scat_events == 0)
                        res.nTrans += 1;

                    // If the photon was scattered we register it instead of just counting,
                    // as this is the data we're interested in.
                    if (re.Scat_events > 0) {
                        for (int j = 0; j < 180 / resolution_S; j++) {
                            if (re.theta > Math.PI) {
                                System.err.println("Theta of scattered photon exceeded allowed maximum value of pi, aborting simulation..");
                                System.exit(1);
                            }

                            // Only count photons that do not exit along the contrail
                            if (Math.toRadians(resolution_S * j) < re.theta
                                    && re.theta <= Math.toRadians(resolution_S * j + resolution_S)) {
                                if (re.Scat_events > 1)
                                    res.nScatMultiple++;

                                res.nScat += 1;

                                // Scattered up and down
                                if (re.theta <= Math.PI / 2.)
                                    res.nScatUp++;
                                else
                                    res.nScatDown++;

                                res.scattered_bins[j] += 1;
                            }
                        }

                        res.avgScattering = (res.avgScattering * (res.nScat - 1) + re.Scat_events) / res.nScat;
                    }
                }
            }

            return res;
        }
    }

    // Store parameters separately to avoid performance penalty due to additional indirection of group object
    public CommonParameters commonParams;
    public DiffuseParameters diffuseParams;
    public DirectParameters directParams;

    /**
     * Creates the simulation object with the given parameters.
     *
     * @param common The common parameters for the simulation
     * @param diffuse The parameters for the diffuse part of the simulation
     * @param direct The parameters for the direct part of the simulation
     * @param suffixDiffuse The suffix for the output file of the diffuse calculation part
     */
    public Simulation(CommonParameters common, DiffuseParameters diffuse, DirectParameters direct,
                      String suffixDiffuse) {
        this.commonParams = common;
        this.diffuseParams = diffuse;
        this.directParams = direct;
        this.suffixDiffuse = suffixDiffuse;
    }

    /**
     * Performs the Monte Carlo Simulation for a given number of angles of incidence, calculating the scattering towards
     * sky and ground as well as the absorption of light caused by the contrail. Output is written to a file in the
     * {@code output} directory.
     *
     * @param contrail A subclass of {@link Contrail} which provides a method to compute the simulation for individual
     *                 photons
     * @param numThreads     The number of threads to run the simulation with
     * @param writeMetricsFile A boolean indicating whether to write metrics to a file or not. If false, metrics will
     *                         still be printed to stdout
     */
    protected void simulateDiffuseRadiation(Contrail contrail, int numThreads, boolean writeMetricsFile) {
        System.out.println("\n -= Monte Carlo simulation of optical contrail properties =-");
        System.out.println("Diffuse Radiation calculation\n");

        // angular dependent radiance
        double d_theta = Math.PI / diffuseParams.getBinsTheta();
        double d_phi = Math.PI * 2 / diffuseParams.getBinsPhi();

        // Make sure this is called after Contrail constructor,
        // since contrail initializes calculated parameters with ExtinctionEfficiency
        // and this piece of code then writes the parameters to the output file.
        // Results are computed incrementally and written to an output file in each step.
        // This is to preserve memory for large simulations, as storing everything in memory might not be feasible.
        // One could consider writing to storage less often to gain an additional performance increase at the cost
        // of higher memory consumption.
        FileWriter fwDataOut = getOutputWriter(suffixDiffuse);

        // Enable MT mode on contrail
        contrail.setMultiThreadedMode(true);

        // Incident direction
        double theta = 0.0;
        double phi = 0.0;

        System.out.println("\n\n - Starting the simulation for diffuse radiation. - \n");

        // Write output header
        List<String> colNames = new ArrayList<>();
        colNames.add("theta");
        colNames.add("phi");
        colNames.add("num_abs");
        colNames.add("num_scattered");
        colNames.add("num_scattered_up");
        colNames.add("num_scattered_down");
        colNames.add("correction_factor");
        colNames.add("average_scattered");
        colNames.add("num_affected");

        for (int i = diffuseParams.getResolutionS(); i <= 180; i+= diffuseParams.getResolutionS()) {
            colNames.add("S_" + i);
        }

        DynamicTable<Double> tableScattered = new DynamicTable<>("DIFFUSE_SCATTERED_RADIATION", colNames);
        tableScattered.marshal(fwDataOut, " ");

        // Threading: thread pool executor
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        // Threading: FIFO queue for threads
        List<Future<SingleStepResult>> results = new ArrayList<>();

        int taskID = 0;

        int bins_total = diffuseParams.getBinsTheta() * (int) diffuseParams.getBinsPhi();
        System.out.println(String.format("Enqueueing %d tasks", bins_total));

        /**
         * Threading aspect: this loop enqueues single step tasks to a thread pool, which executes them.
         */
        for (int i_theta = 0; i_theta < diffuseParams.getBinsTheta(); i_theta++) {
            for (int i_phi = 0; i_phi < diffuseParams.getBinsPhi(); i_phi++) {
                // Set incident direction according to step width
                theta = (0.5 + i_theta) * d_theta;
                phi = (0.5 + i_phi) * d_phi;

                Callable<SingleStepResult> task = new ComputeSingleStep(taskID, contrail, commonParams.getNumPhotons(),
                        diffuseParams.getResolutionS(), theta, phi);
                taskID++;

                // Enqueue the tasks in a thread pool instead of executing single-threaded
                Future<SingleStepResult> future = executorService.submit(task);
                results.add(future);
            }
        }

        // Simple progress report
        int progress = 0;
        long start = System.currentTimeMillis();

        // Metrics
        BigDecimal sum_n_trans = BigDecimal.ZERO;
        BigDecimal sum_n_abs = BigDecimal.ZERO;
        BigDecimal sum_n_scat = BigDecimal.ZERO;

        // Process results in FIFO order: first task submitted is waited for
        for (Future<SingleStepResult> future : results) {
            SingleStepResult res = null;
            try {
                res = future.get(); // Blocking until the result has arrived
                progress++;

                // Progress report
                float perc = progress / (float) bins_total;

                float f_elapsed = (System.currentTimeMillis() - start) / 1000.0f; // Elapsed time in s
                float f_estimated = 0L; // Estimated time to finish in seconds
                if (progress > 0) {
                    f_estimated = (bins_total * f_elapsed / (float) progress) - f_elapsed;
                }

                long l_elapsed = (long) f_elapsed;
                long l_estimated = (long) f_estimated;

                String elapsed = String.format("%02d:%02d:%02d", l_elapsed / 3600, (l_elapsed % 3600) / 60, l_elapsed % 60);
                String estimated = String.format("%02d:%02d:%02d", l_estimated / 3600, (l_estimated % 3600) / 60, l_estimated % 60);

                System.out.print(String.format("\r***** Processed %d out of %d directions of incoming light (%.2f%%) | Elapsed: %s Estimated time left: %s *****",
                        progress, bins_total, 100.0f * perc, elapsed, estimated));
            } catch (InterruptedException | ExecutionException ex) {
                if (ex instanceof InterruptedException) {
                    System.err.println("Step calculation thread was interrupted, exiting...");
                    System.exit(2); // Exit code 2 for threading exceptions
                } else if (ex instanceof ExecutionException) {
                    System.err.println(String.format(
                            "Step calculation thread threw exception %s, exiting...",
                            ((ExecutionException) ex).getCause().toString()));
                    System.exit(2); // Exit code 2 for threading exceptions
                }
            }

            // Make sure res is not null
            if (res == null) {
                System.err.println("Step calculation thread returned null");
                System.exit(1);
            }

            // Process results
            double alpha = Math.acos(Math.sin(res.theta) * Math.cos(res.phi));
            double factor = 2.0 * diffuseParams.getIncidentRadius() * Math.sin(alpha) / commonParams.getNumPhotons();

            double[] S_Array = new double[180 / diffuseParams.getResolutionS()];

            for (int j = 0; j < 180 / diffuseParams.getResolutionS(); j++) {
                S_Array[j] = res.scattered_bins[j] * factor;
            }

            // Write results as a single line to output file, consisting of:
            // theta, phi, scattered photons, sAbs, avgScattering, number of photons that interacted with the contrail, nAbs
            // Not using DynamicTable to store all results here in order to save memory
            try {
                fwDataOut.write(res.theta + " " + res.phi + " " + res.nAbs + " " + res.nScat + " " +
                        res.nScatUp + " " + res.nScatDown + " " + factor + " " + res.avgScattering + " " +
                        (commonParams.getNumPhotons() - res.nTrans) + " ");
                for (int i = 0; i < S_Array.length - 1; i++) {
                    fwDataOut.write(S_Array[i] + " ");
                }
                fwDataOut.write(S_Array[S_Array.length - 1] + "\n");

            } catch (IOException ex) {
                System.err.println("Error writing to output file");
                System.exit(1);
            }

            // Compute metrics
            sum_n_trans = sum_n_trans.add(BigDecimal.valueOf(res.nTrans));
            sum_n_abs = sum_n_abs.add(BigDecimal.valueOf(res.nAbs));
            sum_n_scat = sum_n_scat.add(BigDecimal.valueOf(res.nScat));
        }

        // Shutdown executor to properly terminate the program
        executorService.shutdown();

        // All results written to output file, close it
        try {
            fwDataOut.close();
        } catch (IOException ex) {
            System.err.println("Error closing output file");
            System.exit(1);
        }

        System.out.println("\n");
        System.out.println(" - Simulation finished successfully! -");

        // Calculate metrics
        BigDecimal b_photNo = BigDecimal.valueOf(commonParams.getNumPhotons());
        BigDecimal b_bins_total = BigDecimal.valueOf(bins_total);
        BigDecimal sum_n_phot = b_photNo.multiply(b_bins_total);
        BigDecimal sum_n_affected = b_bins_total.multiply(b_photNo).subtract(sum_n_trans);

        BigDecimal avg_n_trans = sum_n_trans.divide(b_bins_total, 4, RoundingMode.HALF_UP);
        BigDecimal avg_n_abs = sum_n_abs.divide(b_bins_total, 4, RoundingMode.HALF_UP);
        BigDecimal avg_n_scat = sum_n_scat.divide(b_bins_total, 4, RoundingMode.HALF_UP);
        BigDecimal avg_n_affected = b_photNo.subtract(sum_n_trans.divide(b_bins_total, 4, RoundingMode.HALF_UP));

        // Print metrics
        Metrics m = new Metrics();
        m.n_phot_per_angle = commonParams.getNumPhotons();
        m.num_bins_phi = diffuseParams.getBinsPhi();
        m.num_bins_theta = diffuseParams.getBinsTheta();
        m.n_phot = sum_n_phot;
        m.sum_n_abs = sum_n_abs;
        m.sum_n_affected = sum_n_affected;
        m.sum_n_trans = sum_n_trans;
        m.sum_n_scat = sum_n_scat;
        m.avg_n_trans = avg_n_trans;
        m.avg_n_abs = avg_n_abs;
        m.avg_n_scat = avg_n_scat;
        m.avg_n_affected = avg_n_affected;

        System.out.println(m);

        // Write metrics to file if specified
        if (writeMetricsFile) {
            String fileName = String.format("%d_%d_%d_", commonParams.getNumPhotons(), diffuseParams.getBinsPhi(), diffuseParams.getBinsTheta());
            fileName += new SimpleDateFormat("yyyyMMddHHmmss'.txt'").format(new Date());

            m.writeToFile(fileName);
        }
    }

    /**
     * Prompts the user whether files should be overwritten if they exist. To be implemented by subclasses.
     *
     * @param overwrite Specifies whether existing output files should be overwritten
     */
    public abstract void promptCheckOutputFiles(boolean overwrite);

    /**
     * Runs the simulation. To be implemented by subclasses.
     *
     * @param num_threads The number of threads to run the simulation with
     * @param writeMetricsFile Specifies whether metrics should be saved to a file
     * @param randomProvider If this is null, the {@link Contrail} object will use a new {@link Random}
     *                       instance, otherwise it will use the given object to generate random numbers
     */
    public abstract void runSimulation(int num_threads, boolean writeMetricsFile, Random randomProvider);

    /**
     * Gets the output file writer for any output file of the Monte Carlo Simulation.
     *
     * @param outputFile The {@link File} to get the output writer for
     * @return The {@link FileWriter} object for the output file
     */
    protected FileWriter getOutputWriter(File outputFile) {
        FileWriter fwDataOut = null;
        try {
            if (!outputFile.getParentFile().isDirectory()) {
                outputFile.getParentFile().mkdirs();
            }

            fwDataOut = new FileWriter(outputFile);

            // Write input parameters as comment for reference
            fwDataOut.write(commonParams.toCommentString());
            fwDataOut.write(diffuseParams.toCommentString() + "\n");
        } catch (IOException ex) {
            System.err.println("Error creating the file " + outputFile);
        }

        return fwDataOut;
    }

    /**
     * Gets the output file writer for any output file of the Monte Carlo Simulation.
     *
     * @param additionalSuffix If there are multiple output files, e.g. for direct and diffuse parts, a suffix for the
     *                         respective part can be specified here
     * @return The {@link FileWriter} object for the output file
     */
    protected FileWriter getOutputWriter(String additionalSuffix) {
        // Add lambda suffix
        String suffixLambda = String.format(Locale.US, "_%.2f", diffuseParams.getLambda());
        return getOutputWriter(getOutputFile(additionalSuffix + suffixLambda));
    }

    /**
     * Gets a file handle to an output file of the Monte Carlo simulation.
     *
     * @param additionalSuffix If there are multiple output files, e.g. for direct and diffuse parts, a suffix for the
     *                         respective part can be specified here
     * @return The {@link File} object representing the output file
     */
    protected File getOutputFile(String additionalSuffix) {
        String fileName = commonParams.getOutputFilePrefix() + additionalSuffix + ".csv";
        if (commonParams.getOutputFilePrefix().contains(File.separator)) {
            return new File(fileName);
        } else {
            File dir_out = PropertiesManager.getInstance().getDirOutput();
            return new File(dir_out, fileName);
        }
    }
}
