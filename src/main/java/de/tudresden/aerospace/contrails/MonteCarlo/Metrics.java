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

import de.tudresden.aerospace.contrails.Configuration.PropertiesManager;
import de.tudresden.aerospace.contrails.Configuration.Parameters.XMLParameters;
import de.tudresden.aerospace.contrails.Utility.MathHelpers;
import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

/**
 * This class provides functionality to store and analyze certain metrics after a simulation step. Metrics can be
 * accumulated, compared and errors with respect to a baseline can be computed. For analysis purposes, utilities to run
 * the simulation and compute metrics in batches are provided as well as graphing functions. In the current version of
 * the program, unless specified by a command-line parameter, metrics will not be stored to a metrics file after
 * execution of the simulation, but they will always be computed and printed to stdout. This class was originally used
 * to evaluate the number of bins necessary for sufficient accuracy of the solar part of the simulation.
 */
public class Metrics {
    /**
     * Name of the folder where metrics files are stored
     */
    public static File FOLDER_METRICS = PropertiesManager.getInstance().getDirMetrics();
    private static File FOLDER_TEST_CONFIG = new File(PropertiesManager.getInstance().getDirResourcesTest(),
            "test_solar_accuracy");

    public int n_phot_per_angle = 0;
    public int num_bins_phi = 0;
    public int num_bins_theta = 0;
    public BigDecimal n_phot = BigDecimal.ZERO;
    public BigDecimal sum_n_trans = BigDecimal.ZERO;
    public BigDecimal sum_n_affected = BigDecimal.ZERO;
    public BigDecimal sum_n_abs = BigDecimal.ZERO;
    public BigDecimal sum_n_scat = BigDecimal.ZERO;
    public BigDecimal avg_n_trans = BigDecimal.ZERO;
    public BigDecimal avg_n_abs = BigDecimal.ZERO;
    public BigDecimal avg_n_scat = BigDecimal.ZERO;
    public BigDecimal avg_n_affected = BigDecimal.ZERO;

    /**
     * Parameterless constructor to allow client to initialize this object.
     */
    public Metrics() {
    }

    /**
     * Creates a new metrics object, which accumulates the metric values of this object and the given object.
     *
     * @param other The other object, whose metrics to add to this object's
     * @return A new {@link Metrics} object with the accumulated metrics
     */
    public Metrics add(Metrics other) {
        // Adding is only valid for metrics with the same parameters
        if (this.n_phot_per_angle != other.n_phot_per_angle
                || this.num_bins_phi != other.num_bins_phi
                || this.num_bins_theta != other.num_bins_theta) {
            return null;
        }

        Metrics res = new Metrics();

        // Some values stay unmodified
        res.n_phot_per_angle = this.n_phot_per_angle;
        res.num_bins_phi = this.num_bins_phi;
        res.num_bins_theta = this.num_bins_theta;
        res.n_phot = this.n_phot;

        res.sum_n_trans = this.sum_n_trans.add(other.sum_n_trans);
        res.sum_n_affected = this.sum_n_affected.add(other.sum_n_affected);
        res.sum_n_abs = this.sum_n_abs.add(other.sum_n_abs);
        res.sum_n_scat = this.sum_n_scat.add(other.sum_n_scat);
        res.avg_n_trans = this.avg_n_trans.add(other.avg_n_trans);
        res.avg_n_abs = this.avg_n_abs.add(other.avg_n_abs);
        res.avg_n_scat = this.avg_n_scat.add(other.avg_n_scat);
        res.avg_n_affected = this.avg_n_affected.add(other.avg_n_affected);

        return res;
    }

    /**
     * Divides all values by {@code n} to average previously summed up metrics.
     *
     * @param n The number of samples summed up in this object to divide by
     * @return A new {@link Metrics} object with the averaged values
     */
    public Metrics mean(BigDecimal n) {
        Metrics res = new Metrics();

        // Some values stay unmodified
        res.n_phot_per_angle = this.n_phot_per_angle;
        res.num_bins_phi = this.num_bins_phi;
        res.num_bins_theta = this.num_bins_theta;
        res.n_phot = this.n_phot;

        res.sum_n_trans = sum_n_trans.divide(n, 4, RoundingMode.HALF_UP);
        res.sum_n_affected = sum_n_affected.divide(n, 4, RoundingMode.HALF_UP);
        res.sum_n_abs = sum_n_abs.divide(n, 4, RoundingMode.HALF_UP);
        res.sum_n_scat = sum_n_scat.divide(n, 4, RoundingMode.HALF_UP);

        res.avg_n_trans = avg_n_trans.divide(n, 4, RoundingMode.HALF_UP);
        res.avg_n_affected = avg_n_affected.divide(n, 4, RoundingMode.HALF_UP);
        res.avg_n_abs = avg_n_abs.divide(n, 4, RoundingMode.HALF_UP);
        res.avg_n_scat = avg_n_scat.divide(n, 4, RoundingMode.HALF_UP);

        return res;
    }

    /**
     * Calculates the squared error of each average metric.
     *
     * @param other The other {@link Metrics} object to use as a reference
     * @return A new {@link Metrics} object with the error values for average metrics. The other values will be zeroed
     * out
     */
    public Metrics squared_error(Metrics other) {
        Metrics res = new Metrics();

        res.avg_n_trans = (avg_n_trans.subtract(other.avg_n_trans)).pow(2);
        res.avg_n_abs = (avg_n_abs.subtract(other.avg_n_abs)).pow(2);
        res.avg_n_scat = (avg_n_scat.subtract(other.avg_n_scat)).pow(2);
        res.avg_n_affected = (avg_n_affected.subtract(other.avg_n_affected)).pow(2);

        return res;
    }

    /**
     * Reads in the values of a metrics file and assigns them to the variables of this object.
     *
     * @param fileName The name of the metrics file
     */
    public void loadFromFile(String fileName) {
        try {
            Scanner s = new Scanner(new File(FOLDER_METRICS, fileName));

            while (s.hasNextLine()) {
                String d = s.nextLine();

                if (d.isBlank())
                    continue;

                // Test -> No further error checking
                String[] data = d.split("=");
                String key = data[0];
                String val = data[1];

                switch (key) {
                    case "n_phot_per_angle":
                        this.n_phot_per_angle = Integer.parseInt(val);
                        break;
                    case "bins_phi":
                        this.num_bins_phi = Integer.parseInt(val);
                        break;
                    case "bins_theta":
                        this.num_bins_theta = Integer.parseInt(val);
                        break;
                    case "n_phot":
                        this.n_phot = new BigDecimal(val);
                        break;
                    case "sum_n_trans":
                        this.sum_n_trans = new BigDecimal(val);
                        break;
                    case "sum_n_affected":
                        this.sum_n_affected = new BigDecimal(val);
                        break;
                    case "sum_n_abs":
                        this.sum_n_abs = new BigDecimal(val);
                        break;
                    case "sum_n_scat":
                        this.sum_n_scat = new BigDecimal(val);
                        break;
                    case "avg_n_trans":
                        this.avg_n_trans = new BigDecimal(val);
                        break;
                    case "avg_n_abs":
                        this.avg_n_abs = new BigDecimal(val);
                        break;
                    case "avg_n_scat":
                        this.avg_n_scat = new BigDecimal(val);
                        break;
                    case "avg_n_affected":
                        this.avg_n_affected = new BigDecimal(val);
                        break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Writes the metrics stored in this object to a metrics file.
     *
     * @param fileName The name of the metrics file
     */
    public void writeToFile(String fileName) {
        if (!FOLDER_METRICS.exists())
            FOLDER_METRICS.mkdirs();

        try {
            FileWriter fwMetrics = new FileWriter(new File(FOLDER_METRICS, fileName));
            fwMetrics.write(String.format("n_phot_per_angle=%s\n", this.n_phot_per_angle));
            fwMetrics.write(String.format("bins_phi=%d\n", this.num_bins_phi));
            fwMetrics.write(String.format("bins_theta=%d\n", this.num_bins_theta));

            fwMetrics.write(String.format("n_phot=%s\n", this.n_phot));
            fwMetrics.write(String.format("sum_n_trans=%s\n", this.sum_n_trans));
            fwMetrics.write(String.format("sum_n_affected=%s\n", this.sum_n_affected));
            fwMetrics.write(String.format("sum_n_abs=%s\n", this.sum_n_abs));
            fwMetrics.write(String.format("sum_n_scat=%s\n", this.sum_n_scat));

            fwMetrics.write(String.format("avg_n_trans=%s\n", this.avg_n_trans));
            fwMetrics.write(String.format("avg_n_abs=%s\n", this.avg_n_abs));
            fwMetrics.write(String.format("avg_n_scat=%s\n", this.avg_n_scat));
            fwMetrics.write(String.format("avg_n_affected=%s\n", this.avg_n_affected));

            fwMetrics.close();
        } catch (IOException e) {
            System.err.println("Error writing metrics file");
            System.exit(1);
        }
    }

    /**
     * Creates a string containing the metrics variables, which can be printed on the console.
     *
     * @return The string containing the metrics of this object
     */
    public String toString() {
        String res = "\nPrecision metrics:\n";

        res += String.format("Total number of processed photons (n_phot)  = %s\n", this.n_phot);
        res += String.format("Sum of unaffected photons (n_trans)  = %s\n", this.sum_n_trans);
        res += String.format("Sum of affected photons (n_affected) = %s\n", this.sum_n_affected);
        res += String.format("Sum of absorbed photons (n_abs)      = %s\n", this.sum_n_abs);
        res += String.format("Sum of scattered photons (n_scat)    = %s\n", this.sum_n_scat);

        res += "\nAverages per angle:\n";
        res += String.format("n_trans_avg    = %s\n", avg_n_trans.setScale(4, RoundingMode.HALF_UP));
        res += String.format("n_abs_avg      = %s\n", avg_n_abs.setScale(4, RoundingMode.HALF_UP));
        res += String.format("n_scat_avg     = %s\n", avg_n_scat.setScale(4, RoundingMode.HALF_UP));
        res += String.format("n_affected_avg = %s (out of %d photons per angle)",
                this.avg_n_affected.setScale(4, RoundingMode.HALF_UP), this.n_phot_per_angle);

        return res;
    }

    /**
     * Computes the error introduced by different runs of the same configuration. Checks for other metrics files with
     * the same number of bins and photons and computes the error for avg_n_abs and avg_n_trans.
     *
     * @return The variation error as mean squared deviation of each run from the mean
     */
    public static MathHelpers.ComparableTuple<BigDecimal, BigDecimal> variationError(int n_photons, int bins_phi, int bins_theta) {
        // Get mean
        Metrics mean = averageMetricsFiles(n_photons, bins_phi, bins_theta);

        // Calculate mean deviation from mean for avg_n_abs and avg_n_trans
        List<String> names = getResultFileNames(n_photons, bins_phi, bins_theta);

        BigDecimal mean_avg_n_abs = new BigDecimal(0).setScale(8, RoundingMode.HALF_UP);
        BigDecimal mean_avg_n_trans = new BigDecimal(0).setScale(8, RoundingMode.HALF_UP);

        // Calculate sum of squared errors
        for (int i = 0; i < names.size(); i++) {
            Metrics m = new Metrics();
            m.loadFromFile(names.get(i));
            BigDecimal sq_err_n_abs = (m.avg_n_abs.subtract(mean.avg_n_abs).pow(2));
            BigDecimal sq_err_n_trans = (m.avg_n_trans.subtract(mean.avg_n_trans).pow(2));
            mean_avg_n_abs = mean_avg_n_abs.add(sq_err_n_abs);
            mean_avg_n_trans = mean_avg_n_trans.add(sq_err_n_trans);
        }

        // Average error
        mean_avg_n_abs = mean_avg_n_abs.divide(BigDecimal.valueOf(names.size()), 8, RoundingMode.HALF_UP);
        mean_avg_n_trans = mean_avg_n_trans.divide(BigDecimal.valueOf(names.size()), 8, RoundingMode.HALF_UP);

        return new MathHelpers.ComparableTuple(mean_avg_n_abs, mean_avg_n_trans);
    }

    /**
     * Helper that finds accuracy metric files by the number of photons and bins in the result directory
     * {@link Metrics#FOLDER_METRICS}.
     *
     * @param n_photons  Number of photons used when the metrics were captured
     * @param bins_phi   Number of bins for phi used when the metrics were captured
     * @param bins_theta Number of bins for theta used when the metrics were captured
     * @return A list of files that have the given number of bins
     */
    public static List<String> getResultFileNames(int n_photons, int bins_phi, int bins_theta) {
        List<String> res = new ArrayList<>();

        if (!FOLDER_METRICS.exists()) {
            System.err.println("Results directory " + FOLDER_METRICS + " not found");
            System.exit(1);
        }

        for (File f : FOLDER_METRICS.listFiles()) {
            if (f.isDirectory())
                continue;

            String name = f.getName();
            if (name.contains(String.format("%d_%d_%d_", n_photons, bins_phi, bins_theta)))
                res.add(name);
        }

        return res;
    }

    /**
     * Averages the results of all metrics files with the given number of photons and bins and returns the result as a
     * new {@link Metrics} object.
     *
     * @param n_photons  Number of photons used when the metrics were captured
     * @param bins_phi   Number of bins for phi used when the metrics were captured
     * @param bins_theta Number of bins for theta used when the metrics were captured
     * @return The {@link Metrics} object with the averaged values
     */
    public static Metrics averageMetricsFiles(int n_photons, int bins_phi, int bins_theta) {
        List<String> names = getResultFileNames(n_photons, bins_phi, bins_theta);

        // Calculate sum
        Metrics res = new Metrics();
        res.n_phot_per_angle = n_photons;
        res.num_bins_phi = bins_phi;
        res.num_bins_theta = bins_theta;
        res.n_phot = BigDecimal.valueOf(n_photons).multiply(BigDecimal.valueOf(bins_phi).multiply(BigDecimal.valueOf(bins_theta)));
        for (int i = 0; i < names.size(); i++) {
            Metrics m = new Metrics();
            m.loadFromFile(names.get(i));
            res = res.add(m);
        }

        // Average result
        return res.mean(new BigDecimal(String.valueOf(names.size())));
    }

    /**
     * Runs a simulation n times and saves {@link Metrics} to files.
     *
     * @param simulation  The fully initialized {@link Simulation} object
     * @param num_repeats How many times the simulation should be repeated
     */
    public static void generateMetricsFiles(Simulation simulation, int num_repeats) {
        for (int i = 0; i < num_repeats; i++) {
            System.out.printf("Running simulation with metrics enabled, %d of %d repeats\n", i + 1, num_repeats);
            int numThreads = Runtime.getRuntime().availableProcessors();
            simulation.runSimulation(numThreads, true, null);
            try {
                Thread.sleep(1500); // Prevents from writing to the same file if finished in under one second
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    /**
     * Helper that evaluates Metrics files with the given values with respect to a given baseline.
     *
     * @param n_photons  Number of photons used when the metrics were captured
     * @param bins_phi   Number of bins for phi used when the metrics were captured
     * @param bins_theta Number of bins for theta used when the metrics were captured
     * @param baseline   Baseline {@link Metrics} file to compare to
     * @return A {@link Metrics} object containing the squared errors
     */
    private static Metrics evaluateMetrics(int n_photons, int bins_phi, int bins_theta, Metrics baseline) {
        Metrics m_avg = averageMetricsFiles(n_photons, bins_phi, bins_theta); // Use averages for more robustness

        System.out.printf("\n=== (%d, %d) bins compared to baseline with (%d, %d) bins ===\n", bins_phi, bins_theta,
                baseline.num_bins_phi, baseline.num_bins_theta);

        // For a more comprehensive analysis, print squared errors for all individual metrics here
        Metrics err = m_avg.squared_error(baseline);
        System.out.println("Square Errors");
        System.out.printf("avg_n_trans = %s\navg_n_abs = %s\navg_n_scat = %s\navg_n_affected = %s\n",
                err.avg_n_trans,
                err.avg_n_abs,
                err.avg_n_scat,
                err.avg_n_affected);

        return err;
    }

    /**
     * Evaluates the given range of bins by running the simulation for configurations within the range and storing
     * metrics. Configurations have the same parameters as a given reference configuration. After running each
     * configuration, the metrics are evaluated against a baseline configuration, using
     * {@link Metrics#evaluateMetrics(int, int, int, Metrics)}.
     *
     * @param referenceFileName The reference configuration file
     * @param bins_lower_phi The starting number of bins for phi (inclusive)
     * @param bins_upper_phi The final number of bins for phi (non-inclusive)
     * @param ratio_theta_phi The ratio of bins theta/phi
     * @param stepWidth The number of bins (in one direction) in between each evaluated configuration
     * @param numRepeats The number of repeats for each configuration. Repeated runs are averaged for more robust
     *                   results
     * @param numPhotons The number of photons to be evaluated in each step of the simulation
     * @param generateMetrics If set to {@code true}, the simulation will be executed, if set to {@code false}, only
     *                        evaluation of the results of previous runs will take place
     * @param baseline The {@link Metrics} of the baseline configuration to compare against
     * @return A map containing {@link MathHelpers.Tuple}s of (bins phi, bins theta) as keys and
     * {@link MathHelpers.Tuple}s of (average number of absorbed photons, average number of transferred photons)
     * as values
     */
    private static Map<MathHelpers.ComparableTuple<Integer, Integer>, MathHelpers.ComparableTuple<BigDecimal, BigDecimal>>
    evaluateBinRange(String referenceFileName, int bins_lower_phi, int bins_upper_phi,
                     double ratio_theta_phi, int stepWidth, int numRepeats,
                     int numPhotons, boolean generateMetrics, Metrics baseline) {

        // Prepare result
        Map<MathHelpers.ComparableTuple<Integer, Integer>,
                MathHelpers.ComparableTuple<BigDecimal, BigDecimal>> res = new TreeMap<>();

        // Compute metrics
        for (int i = bins_lower_phi; i < bins_upper_phi; i += stepWidth) {
            int j = (int) (i * ratio_theta_phi); // Adjust theta according to ratio
            if (generateMetrics) {
                SolarSimulation sim = (SolarSimulation) createSimulation(referenceFileName, numPhotons,
                        i, j);

                Metrics.generateMetricsFiles(sim, numRepeats);
            }

            // Print metrics and get error w.r.t. baseline
            Metrics err = evaluateMetrics(numPhotons, i, j, baseline);

            // Add result
            res.put(new MathHelpers.ComparableTuple<>(i, j),
                    new MathHelpers.ComparableTuple<>(err.avg_n_abs, err.avg_n_trans));
        }
        return res;
    }

    /**
     * Computes the Root Mean Square Error (RMSE) of the given errors of the average numbers of absorbed (n_abs) and
     * transferred (n_trans) photons.
     *
     * @param errorMap The errors as given by
     * {@link Metrics#evaluateBinRange(String, int, int, double, int, int, int, boolean, Metrics)}
     * @return A {@link MathHelpers.ComparableTuple} containing the RMSE values for (n_abs, n_trans)
     */
    public static MathHelpers.ComparableTuple<BigDecimal, BigDecimal> rootMeanSquareError(
            Map<MathHelpers.ComparableTuple<Integer, Integer>, MathHelpers.ComparableTuple<BigDecimal, BigDecimal>> errorMap) {
        BigDecimal rmse_n_abs = BigDecimal.ZERO;
        BigDecimal rmse_n_trans = BigDecimal.ZERO;
        for (Map.Entry<MathHelpers.ComparableTuple<Integer, Integer>,
                MathHelpers.ComparableTuple<BigDecimal, BigDecimal>> e : errorMap.entrySet()) {
            rmse_n_abs = rmse_n_abs.add(e.getValue().x);
            rmse_n_trans = rmse_n_trans.add(e.getValue().y);
        }
        MathContext mc = new MathContext(8, RoundingMode.HALF_UP);
        rmse_n_abs = (rmse_n_abs.divide(new BigDecimal(errorMap.size()), mc)).sqrt(mc);
        rmse_n_trans = (rmse_n_trans.divide(new BigDecimal(errorMap.size()), mc)).sqrt(mc);

        return new MathHelpers.ComparableTuple<>(rmse_n_abs, rmse_n_trans);
    }

    /**
     * Helper function to create a solar simulation object with parameters from the given configuration file. The other
     * given parameters override the read parameters from the configuration file.
     *
     * @param referenceFileName The reference configuration file to get simulation parameters from
     * @param numPhotons The number of photons to override
     * @param bins_phi The number of bins for phi to override
     * @param bins_theta The number of bins for theta to override
     * @return The {@link SolarSimulation} object with the given parameters
     */
    public static Simulation createSimulation(String referenceFileName, int numPhotons, int bins_phi, int bins_theta) {
        // Use single template config and modify parameters
        XMLParameters params = XMLParameters.unmarshal(referenceFileName);
        params.getCommon().setNumPhotons(numPhotons);
        params.getSolarDiffuse().setBinsPhi(bins_phi);
        params.getSolarDiffuse().setBinsTheta(bins_theta);

        return new SolarSimulation(params);
    }

    /**
     * Draws a plot with the given discrete x- and y-axis values. Internally uses
     * <a href="https://github.com/sh0nk/matplotlib4j">matplotlib4j</a> to create the plot via python matplotlib
     * bindings. This function assumes matplotlib is installed for the given python executable.
     *
     * @param xs The x-axis values
     * @param ys The y-axis values
     * @param title The title of the plot
     * @param yLabel The label of the y-axis
     * @param pythonPath The path to the python executable
     */
    private static void drawPlot(List<Integer> xs, List<BigDecimal> ys, String title, String yLabel,
                                 String pythonPath) {
        // Draw diagrams
        Plot plt = Plot.create(PythonConfig.pythonBinPathConfig(pythonPath));
        plt.plot().add(xs, ys, "o");
        plt.xlabel("Number of bins of phi and theta");
        plt.ylabel(yLabel);
        plt.title(title);
        plt.legend();
        try {
            plt.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs the solar simulation for multiple configurations, each with a sufficient number of photons and differing in
     * the number of bins of phi and theta. Each configuration is run multiple times in order to get robust numbers, as
     * the MonteCarlo simulation relies on random sampling. The algorithm to determine accuracy is as follows: 1)
     * Compute mean squared errors for a configuration with respect to the baseline (with 360x360 bins) avg_n_trans 2)
     * Define a configuration as accurate if it's error is acceptable (defined by parameter {@code accuracy_threshold} -
     * Maximum number of photons that avg_n_trans may deviate from avg_n_trans of baseline) 3) Compute RMSE for each
     * range of bins that was tested as an additional measure for how much the tested configurations vary. This value
     * can be compared to RMSE values of other ranges of bins to compare accuracy more generally over an interval of
     * bins.
     * <p>
     * To determine the minimum amount of bins for an accurate configuration, the following algorithm was used (not
     * reflected in this method, as runs were conducted separately with changes in between): 1) Compute metrics for
     * configs of nxn bins and baseline, each for {@code num_repeats} times. Start at 1x1 and increment by +15x15 each
     * step (1x1, 15x15, 30x30, 45x45, etc.) 2) Compute accuracy of those metrics w.r.t. baseline variation 3) Stop when
     * the first accurate configuration was found and start iterating in steps of 1 from the previous (inaccurate)
     * configuration's number of bins until an accurate configuration is found -> The found configuration is the minimum
     * accurate configuration with nxn bins. To find other accurate configurations with asymmetric bins of phi and
     * theta, test with sensible configurations are conducted manually.
     */
    public static void main(String[] args) {
        // Print comparison of metrics in metrics folder, captured with 100k photons
        String referenceFileName = FOLDER_TEST_CONFIG + "config_test_bins.txt";
        int n_photons = 100_000;
        int num_repeats = 3;
        BigDecimal accuracy_threshold = BigDecimal.valueOf(n_photons)
                .divide(BigDecimal.valueOf(100_000), 8, RoundingMode.HALF_UP); // Within x% of n_photons

        // Measure total time taken
        long startTime = System.currentTimeMillis();

        // Currently there are no checks in place for existing metrics files. To generate new ones, change the boolean
        // on the following line to true. Otherwise, existing files will be used for evaluation
        boolean generate_metrics = false;

        // Test config with 360x360 bins of phi and theta as baseline
        SolarSimulation sim;
        if (generate_metrics) {
            sim = (SolarSimulation) createSimulation(referenceFileName, n_photons, 360, 360);
            Metrics.generateMetricsFiles(sim, num_repeats);
        }
        Metrics avg_baseline = averageMetricsFiles(n_photons, 360, 360);

        // Test config with 360x180 bins of phi and theta as baseline for asymmetric bin configuration
        if (generate_metrics) {
            sim = (SolarSimulation) createSimulation(referenceFileName, n_photons, 360, 180);
            Metrics.generateMetricsFiles(sim, num_repeats);
        }
        Metrics avg_baseline_asymmetric = averageMetricsFiles(n_photons, 360, 180);

        // Compute baseline variation error and store results
        MathHelpers.ComparableTuple<BigDecimal, BigDecimal> err_var_baseline;
        MathHelpers.ComparableTuple<BigDecimal, BigDecimal> err_var_baseline_asymmetric;
        Map<MathHelpers.ComparableTuple<Integer, Integer>, MathHelpers.ComparableTuple<BigDecimal, BigDecimal>> allResults = new TreeMap<>();
        MathHelpers.ComparableTuple<BigDecimal, BigDecimal> rmse; // Root mean square error per range

        // Compute baseline variation error
        err_var_baseline = variationError(n_photons, 360, 360);
        System.out.printf("Average squared baseline variation errors (%d, %d) bins:\nerr_avg_n_abs = %s\nerr_avg_n_trans = %s\n",
                360, 360, err_var_baseline.x, err_var_baseline.y);

        err_var_baseline_asymmetric = variationError(n_photons, 360, 180);
        System.out.printf("Average squared baseline variation errors (%d, %d) bins:\nerr_avg_n_abs = %s\nerr_avg_n_trans = %s\n",
                360, 180, err_var_baseline_asymmetric.x, err_var_baseline_asymmetric.y);

        // Evaluate different ranges
        Map<MathHelpers.ComparableTuple<Integer, Integer>, MathHelpers.ComparableTuple<BigDecimal, BigDecimal>> eval_range;

        // Finally, measure configurations with bins_theta = bins_phi / 2 since this theoretically makes them
        // equal in resolution as sunlight comes from above and the 180° below are not relevant
        eval_range = evaluateBinRange(referenceFileName, 10, 181,
                0.5, 10, 3, n_photons, generate_metrics, avg_baseline_asymmetric);
        allResults.putAll(eval_range);

        // Post-processing: Compute RMSE
        rmse = rootMeanSquareError(eval_range);
        System.out.printf("Root mean square error (RMSE) of range 10 to 180 bins" +
                " is %s for avg_n_abs and %s for avg_n_trans\n", rmse.x, rmse.y);


        // Last mile asymmetric
        eval_range = evaluateBinRange(referenceFileName, 2, 101,
                0.5, 2, 3, n_photons, generate_metrics, avg_baseline_asymmetric);
        allResults.putAll(eval_range);

        // Post-processing: Compute RMSE
        rmse = rootMeanSquareError(eval_range);
        System.out.printf("Root mean square error (RMSE) of range 2 to 100 bins" +
                " is %s for avg_n_abs and %s for avg_n_trans\n", rmse.x, rmse.y);

        // Prepare axes for diagrams
        List<Integer> x_axis = new ArrayList<>();
        List<BigDecimal> y_n_abs = new ArrayList<>();
        List<BigDecimal> y_n_trans = new ArrayList<>();
        List<BigDecimal> y_var_n_abs = new ArrayList<>();
        List<BigDecimal> y_var_n_trans = new ArrayList<>();
        List<Boolean> config_accurate = new ArrayList<>();

        // Assign values to axes for plotting and compute post-processing errors
        System.out.println("\n=== Checking configuration accuracy ===");
        for (Map.Entry<MathHelpers.ComparableTuple<Integer, Integer>, MathHelpers.ComparableTuple<BigDecimal, BigDecimal>> e : allResults.entrySet()) {
            int bins_phi = e.getKey().x;
            int bins_theta = e.getKey().y;
            BigDecimal sq_err_avg_n_abs = e.getValue().x;
            BigDecimal sq_err_avg_n_trans = e.getValue().y;

            MathHelpers.ComparableTuple<BigDecimal, BigDecimal> err_var = variationError(n_photons, bins_phi, bins_theta);

            // Check if configuration is accurate via avg_n_trans
            MathContext mc = new MathContext(8, RoundingMode.HALF_UP);
            if (sq_err_avg_n_trans.sqrt(mc).compareTo(accuracy_threshold) < 0) {
                System.out.printf("Config (%d, %d) is accurate with respect to the set threshold (%s <= %s)" +
                                " photons of deviation from avg_n_trans. Variation error is %s\n",
                        bins_phi, bins_theta, sq_err_avg_n_trans.sqrt(mc),
                        accuracy_threshold.setScale(2), err_var.y.sqrt(mc));
                System.out.println("Error of n_abs is " + sq_err_avg_n_abs.sqrt(mc));
                config_accurate.add(true);
            } else {
                config_accurate.add(false);
            }

            // Entries are sorted, since TreeMap is used
            x_axis.add(bins_phi); // x-axis: number of bins (assume symmetry for now)
            y_n_abs.add(sq_err_avg_n_abs); // y-axis 1: squared errors of avg_n_abs
            y_n_trans.add(sq_err_avg_n_trans); // y-axis 2: squared errors of avg_n_trans
            y_var_n_abs.add(err_var.x); // y-axis 3: variation error of avg_n_abs
            y_var_n_trans.add(err_var.y); // y-axis 4: variation error of avg_n_trans
        }

        // Draw diagrams
        String pythonPath = "/usr/bin/python3"; // Adjust to your path
        drawPlot(x_axis, y_n_abs, "Squared Error with respect to the baseline configuration (360x180 bins)",
                "Squared Error of n_abs", pythonPath);
        drawPlot(x_axis, y_n_trans, "Squared Error with respect to the baseline configuration (360x180 bins)",
                "Squared Error of n_trans", pythonPath);

        drawPlot(x_axis, y_var_n_abs, "Variation Error with respect to the baseline configuration (360x180 bins)",
                "Variation Error of n_abs", pythonPath);
        drawPlot(x_axis, y_var_n_trans, "Variation Error with respect to the baseline configuration (360x180 bins)",
                "Variation Error of n_trans", pythonPath);

        long elapsedTime = (long) ((System.currentTimeMillis() - startTime) / 1000.0f);
        String elapsed = String.format("%02d hours, %02d minutes, %02d seconds",
                elapsedTime / 3600, (elapsedTime % 3600) / 60, elapsedTime % 60);
        System.out.printf("Completed metrics generation, took %s\n", elapsed);
    }

}
