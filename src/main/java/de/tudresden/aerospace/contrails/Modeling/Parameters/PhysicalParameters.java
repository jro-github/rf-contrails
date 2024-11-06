package de.tudresden.aerospace.contrails.Modeling.Parameters;

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

import de.tudresden.aerospace.contrails.Configuration.IO.CustomCSVParser;
import de.tudresden.aerospace.contrails.Configuration.IO.DynamicTable;

import java.lang.Math;

// for file handling
import java.util.*;


/**
 * Groups functionality to compute physical parameters of ice crystals, depending on their size and other parameters.
 * As this is separate for the solar and terrestrial spectra, this class only groups common functionality.
 */
public abstract class PhysicalParameters {
    // ====== Arrays for wavelength and shape lookup ======
    /**
     * Wavelength of the selected spectral band<br/> 0 = 0.55 micrometer<br/> 1 = 1.35 micrometer<br/> 2 = 2.25
     * micrometer<br/> 3 = 2.75 micrometer<br/> 4 = 3.0125 micrometer<br/> 5 = 3.775 micrometer<br/> 6 = 4.5 micrometer
     */
    protected Double[] bands;

    /**
     * First dimension: spectral band index<br/> Second dimension: value index<br/> 0 = lambda<br/> 1 = lambda1<br/> 2 =
     * lambda2<br/> 3 = mr<br/> 4 = mi<br/> 5 = dS/S<br/>
     */
    protected Double[][] table1;

    /**
     * First dimension: spectral band index [0, 6]<br/> Second dimension: shape index<br/> 0 = plate<br/> 1 =
     * column<br/> 2 = hollow column<br/> 3 = bullet rosettes-4<br/> 4 = bullet rosettes-6<br/> 5 = aggregates<br/>
     * Third dimension: value index<br/> 0 = lambda<br/> 1 = eta1_ext<br/> 2 = eta2_ext<br/> 3 = beta1<br/> 4 =
     * beta2<br/> 5 = alpha<br/> 6 = xi_ext<br/>
     */
    protected Double[][][] table3;

    /**
     * First dimension: spectral band index [0, 6]<br/> Second dimension: shape index [0, 5]<br/> Third dimension: value
     * index<br/> 0 = lambda<br/> 1 = eta1_abs<br/> 2 = eta2_abs<br/> 3 = xi1_abs<br/> 4 = xi2_abs<br/>
     */
    protected Double[][][] table4;

    /**
     * First dimension: spectral band index [0, 6]<br/> Second dimension: shape index [0, 5]<br/> Third dimension: value
     * index<br/> 0 = lambda<br/> 1 = xi1_g<br/> 2 = xi2_g<br/> 3 = xi3_g<br/> 4 = xi4_g<br/> 5 = xi5_g<br/> 6 =
     * xi6_g<br/> 7 = xi7_g
     */
    protected Double[][][] table5;

    /**
     * Reads in values and initializes tables.
     */
    protected PhysicalParameters() {

        List<DynamicTable<Double>> tables;

        // Read table 1: selected spectral band
        tables = CustomCSVParser.parse("Yang2000_OpticalConstants.txt");
        table1 = tables.get(0).toArray(Double.class);

        // Also read wavelengths
        List<Double> lambdas = tables.get(0).getColumn(0);
        bands = new Double[lambdas.size()];
        lambdas.toArray(bands);

        // Read table 3: extinction efficiency
        tables = CustomCSVParser.parse("Yang2000_Extinction.txt");
        table3 = new Double[7][6][7];
        for (int table_index = 0; table_index < tables.size(); table_index++) {
            List<List<Double>> rows = tables.get(table_index).getRows();
            for (int row_index = 0; row_index < rows.size(); row_index++) {
                List<Double> row = rows.get(row_index);
                for (int value_index = 0; value_index < row.size(); value_index++) {
                    table3[row_index][table_index][value_index] = row.get(value_index);
                }
            }
        }

        // Read table 4: absorption efficiency
        tables = CustomCSVParser.parse("Yang2000_Absorption.txt");
        table4 = new Double[7][6][5];
        for (int table_index = 0; table_index < tables.size(); table_index++) {
            List<List<Double>> rows = tables.get(table_index).getRows();
            for (int row_index = 0; row_index < rows.size(); row_index++) {
                List<Double> row = rows.get(row_index);
                for (int value_index = 0; value_index < row.size(); value_index++) {
                    table4[row_index][table_index][value_index] = row.get(value_index);
                }
            }
        }


        // Read table 5: asymmetry factors
        tables = CustomCSVParser.parse("Yang2000_Asymmetry.txt");
        table5 = new Double[7][6][8];
        for (int table_index = 0; table_index < tables.size(); table_index++) {
            List<List<Double>> rows = tables.get(table_index).getRows();
            for (int row_index = 0; row_index < rows.size(); row_index++) {
                List<Double> row = rows.get(row_index);
                for (int value_index = 0; value_index < row.size(); value_index++) {
                    table5[row_index][table_index][value_index] = row.get(value_index);
                }
            }
        }
    }

    /**
     * Gets the wavelength for the specified band from the associated parameter file.
     *
     * @param band The spectral band index
     * @return The wavelength for the given band
     */
    public abstract double getLambda(int band);

    // ====== Helper functions ======

    /**
     * Helper for calculating the spherical diameter with equivalent projected area of an ice crystal from its maximum
     * dimensions and shape.
     *
     * @param dMax  Maximum dimension in micrometers. Range: [2, 10'000]
     * @param shape Shape index of the ice crystal. Range: [0, 5]
     */
    protected double Calc_Da(double dMax, int shape) {
        // shape ... 0 = Plate
        //           1 = Column
        //           2 = Hollow Column
        //           3 = Bullet rosettes-4
        //           4 = Bullet rosettes-6
        //           5 = Aggregates
        double[][] a = {{0.43773, 0.33401, 0.33401, 0.15909, 0.14195, -0.47737},
                {0.75497, 0.36477, 0.36477, 0.84308, 0.84394, 0.10026e1},
                {0.19033e-1, 0.30855, 0.30855, 0.70161e-2, 0.72125e-2, -0.10030e-2},
                {0.35191e-3, -0.55631e-1, -0.55631e-1, -0.11003e-2, -0.11219e-2, 0.15166e-3},
                {-0.70782e-4, 0.30162e-2, 0.30162e-2, 0.45161e-4, 0.45819e-4, -0.78433e-5}};
        double Da = 0;
        double help = 0;
        for (int n = 0; n < 5; n += 1) {
            help += a[n][shape] * Math.pow(Math.log(dMax), n);
        }

        Da = Math.exp(help);
        return Da;
    }

    /**
     * Helper for calculating the spherical diameter with equivalent volume of an ice crystal from its maximum
     * dimensions and shape.
     *
     * @param dMax  Maximum dimension in micrometers. Range: [2, 10'000]
     * @param shape Shape index of the ice crystal. Range: [0, 5]
     */
    protected double Calc_Dv(double dMax, int shape) {
        // shape ... 0 = Plate
        //           1 = Column
        //           2 = Hollow Column
        //           3 = Bullet rosettes-4
        //           4 = Bullet rosettes-6
        //           5 = Aggregates
        double[][] b = {{0.31228, 0.30581, 0.24568, -0.97940e-1, -0.10318, -0.70160},
                {0.80874, 0.26252, 0.26202, 0.85683, 0.86290, 0.99215},
                {0.29287e-2, 0.35458, 0.35479, 0.29483e-2, 0.70665e-3, 0.29322e-2},
                {-0.44378e-3, -0.63202e-1, -0.63236e-1, -0.14341e-2, -0.11055e-2, -0.40492e-3},
                {0.23109e-4, 0.33755e-2, 0.33773e-2, 0.74627e-4, 0.57906e-4, 0.18841e-4}};
        double Dv = 0;
        double help = 0;
        for (int n = 0; n < 5; n += 1) {
            help += b[n][shape] * Math.pow(Math.log(dMax), n);
        }

        Dv = Math.exp(help);
        return Dv;
    }

    /**
     * Helper for calculating the effective diameter of an ice crystal from its maximum dimensions and shape.
     *
     * @param dMax  Maximum dimension in micrometers. Range: [2, 10'000]
     * @param shape Shape index of the ice crystal. Range: [0, 5]
     */
    protected double Calc_de(double dMax, int shape) {
        // shape ... 0 = Plate
        //           1 = Column
        //           2 = Hollow Column
        //           3 = Bullet rosettes-4
        //           4 = Bullet rosettes-6
        //           5 = Aggregates
        double de = Math.pow(Calc_Dv(dMax, shape), 3) / Math.pow(Calc_Da(dMax, shape), 2);
        return de;
    }

    /**
     * Helper for calculating the effective diameter of an ice crystal from its maximum dimensions.
     *
     * @param dMax Maximum dimension in micrometers. Range: [2, 10'000]
     */
    protected double Calc_de_mix(double dMax) {
        // dMax ... maximum dimension [micrometer]
        double de = 0;
        if (dMax < 70) {
            // 50% Bullet rosettes-6
            // 25% hollow columns
            // 25% plates
            de = (0.5 * Math.pow(Calc_Dv(dMax, 4), 3) + 0.25 * Math.pow(Calc_Dv(dMax, 2), 3) + 0.25 * Math.pow(Calc_Dv(dMax, 0), 3)) /
                    (0.5 * Math.pow(Calc_Da(dMax, 4), 2) + 0.25 * Math.pow(Calc_Da(dMax, 2), 2) + 0.25 * Math.pow(Calc_Da(dMax, 0), 2));
        } else {
            // 30% Aggregates
            // 30% Bullet rosettes-6
            // 20% hollow columns
            // 20% plates
            de = (0.3 * Math.pow(Calc_Dv(dMax, 4), 3) + 0.3 * Math.pow(Calc_Dv(dMax, 4), 3) + 0.2 * Math.pow(Calc_Dv(dMax, 2), 3) + 0.2 * Math.pow(Calc_Dv(dMax, 0), 3)) /
                    (0.3 * Math.pow(Calc_Da(dMax, 4), 2) + 0.3 * Math.pow(Calc_Da(dMax, 4), 2) + 0.2 * Math.pow(Calc_Da(dMax, 2), 2) + 0.2 * Math.pow(Calc_Da(dMax, 0), 2));
        }

        return de;
    }

    // ====== Abstract calculation functions ======

    /**
     * Calculates the extinction efficiency for a non-spherical ice crystal under consideration of complex ray behavior.
     *
     * @param dMax Maximum dimension in micrometers. Range: [2, 10'000]
     * @param band Spectral band index. Range: [0, 5]
     */
    public abstract double calcQext(double dMax, int band);

    /**
     * Calculates the solar extinction efficiency for an individual non-spherical ice crystal under consideration of
     * complex ray behavior.
     *
     * @param dMax  Maximum dimension in micrometers. Range: [2, 10'000]
     * @param band  Spectral band index. Range: [0, 5]
     * @param shape Shape index of the ice crystal. Range: [0, 5]
     */
    public abstract double calcQext(double dMax, int band, int shape);

    /**
     * Calculates the absorption efficiency for a non-spherical ice crystal under consideration of complex ray behavior.
     *
     * @param dMax Maximum dimension in micrometers. Range: [2, 10'000]
     * @param band Spectral band index. Range: [0, 5]
     */
    public abstract double calcQabs(double dMax, int band);

    /**
     * Calculates the absorption efficiency for an individual non-spherical ice crystal under consideration of complex
     * ray behavior.
     *
     * @param dMax  Maximum dimension in micrometers. Range: [2, 10'000]
     * @param band  Spectral band index. Range: [0, 5]
     * @param shape Shape index of the ice crystal. Range: [0, 5]
     */
    public abstract double calcQabs(double dMax, int band, int shape);

    /**
     * Calculates the scattering efficiency for a non-spherical ice crystal under consideration of complex ray behavior.
     *
     * @param dMax Maximum dimension in micrometers. Range: [2, 10'000]
     * @param band Spectral band index. Range: [0, 5]
     */
    public abstract double calcQsca(double dMax, int band);

    /**
     * Calculates the scattering efficiency for an individual non-spherical ice crystal under consideration of complex
     * ray behavior.
     *
     * @param dMax  Maximum dimension in micrometers. Range: [2, 10'000]
     * @param band  Spectral band index. Range: [0, 5]
     * @param shape Shape index of the ice crystal. Range: [0, 5]
     */
    public abstract double calcQsca(double dMax, int band, int shape);

    /**
     * Calculates the solar asymmetry factor for a non-spherical ice crystal under consideration of complex ray
     * behavior.
     *
     * @param d    Varies depending on subclass.
     * @param band Spectral band index. Range: [0, 5]
     */
    public abstract double calcG(double d, int band);

    /**
     * Calculates the asymmetry factor for an individual non-spherical ice crystal under consideration of complex ray
     * behavior.
     *
     * @param d     Varies depending on subclass.
     * @param band  Spectral band index. Range: [0, 5]
     * @param shape Shape index of the ice crystal. Range: [0, 5]
     */
    public abstract double calcG(double d, int band, int shape);
}
