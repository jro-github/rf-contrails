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

/**
 * Provides implementations for the methods declared by {@link PhysicalParameters}. These calculate the physical
 * parameters lambda, qExt, qAbs, qSca and g for solar radiation.
 * @see <a href="https://doi.org/10.1029/1999JD900755">Parameterization of the scattering
 * and absorption properties of individual ice crystals (Paper)</a> for more details on solar coefficients.
 */
public class SolarParameters extends PhysicalParameters {
    /**
     * Distribution of ice crystals with
     * 50% Bullet rosettes-6, 25% hollow columns and 25% plates
     */
    private double[] fLow = new double[]{0.25, 0., 0.25, 0., 0.5, 0.};

    /**
     * Distribution of ice crystals with
     * 30% Aggregates, 30% Bullet rosettes-6, 20% hollow columns and 20% plates
     */
    private double[] fHigh = new double[]{0.2, 0., 0.2, 0., 0.3, 0.3};

    /**
     * Creates the object to hold the calculated physical parameters for the solar part.
     */
    public SolarParameters() {
        super();
    }

    /**
     * Helper for calculation functions which returns the distribution of ice crystal shapes depending on the maximum
     * dimension of the crystals.
     *
     * @param dMax Maximum dimension in micrometers. Range: [2, 10'000]
     * @return The distribution of ice crystal shapes
     */
    private double[] getF(double dMax) {
        if (dMax < 70)
            return fLow;
        else
            return fHigh;
    }

    /**
     * Helper for calculating solar extinction efficiency of a sphere.
     *
     * @param dMax  Maximum dimension in micrometers. Range: [2, 10'000]
     * @param band  Spectral band index. Range: [0, 5]
     * @param shape Shape index of the ice crystal. Range: [0, 5]
     * @see <a href="https://doi.org/10.1029/1999JD900755">Parameterization of the scattering
     * and absorption properties of individual ice crystals (Paper)</a> for more details on solar coefficients.
     */
    private double calcQextPrime(double dMax, int band, int shape) {
        // band ... das gewuenschte spektrale Band
        //   0 = 0.55 micrometer
        //   1 = 1.35 micrometer
        //   2 = 2.25 micrometer
        //   3 = 3.0125 micrometer
        //   4 = 3.775 micrometer
        //   5 = 4.5 micrometer
        // shape ... 0 = Plate
        //           1 = Column
        //           2 = Hollow Column
        //           3 = Bullet rosettes-4
        //           4 = Bullet rosettes-6
        //           5 = Aggregates
        double lambda = bands[band]; // wavelength [micrometer]
        double mr = table1[band][3];
        double rhoe = 2 * Math.PI * Calc_de(dMax, shape) * Math.abs(mr - 1) / lambda; //effective phase delay
        double eta1 = table3[band][shape][1];
        double rho = eta1 * rhoe;
        double beta = table3[band][shape][3];
        double Qextprime = 2. - 4. * Math.exp(-rho * Math.tan(beta)) *
                (Math.cos(beta) / rho * Math.sin(rho - beta) + Math.pow(Math.cos(beta) / rho, 2) * Math.cos(rho - 2 * beta)) +
                4. * Math.pow(Math.cos(beta) / rho, 2) * Math.cos(2 * beta);
        return Qextprime;
    }

    /**
     * Helper for calculating solar extinction efficiency for an individual non-spherical ice crystal without
     * consideration of complex ray behavior.
     *
     * @param dMax  Maximum dimension in micrometers. Range: [2, 10'000]
     * @param band  Spectral band index. Range: [0, 5]
     * @param shape Shape index of the ice crystal. Range: [0, 5]
     * @see <a href="https://doi.org/10.1029/1999JD900755">Parameterization of the scattering
     * and absorption properties of individual ice crystals (Paper)</a> for more details on solar coefficients.
     */
    private double calcQextPrimePrime(double dMax, int band, int shape) {
        // band ... das gewuenschte spektrale Band
        //   0 = 0.55 micrometer
        //   1 = 1.35 micrometer
        //   2 = 2.25 micrometer
        //   3 = 3.0125 micrometer
        //   4 = 3.775 micrometer
        //   5 = 4.5 micrometer
        // shape ... 0 = Plate
        //           1 = Column
        //           2 = Hollow Column
        //           3 = Bullet rosettes-4
        //           4 = Bullet rosettes-6
        //           5 = Aggregates
        double lambda = bands[band]; // wavelength [micrometer]
        double mr = table1[band][3];
        double rhoe = 2 * Math.PI * Calc_de(dMax, shape) * Math.abs(mr - 1) / lambda; //effective phase delay
        double eta2 = table3[band][shape][2];
        double beta2 = table3[band][shape][4];
        double alpha = table3[band][shape][5];
        double Qextprimeprime = 2 * (1 - Math.exp(-2. / 3. * rhoe * eta2 * Math.tan(beta2)) * Math.cos(2. / 3. * rhoe * eta2 + alpha));
        return Qextprimeprime;
    }

    /**
     * Helper for calculating solar absorption efficiency of a sphere.
     *
     * @param dMax  Maximum dimension in micrometers. Range: [2, 10'000]
     * @param band  Spectral band index. Range: [0, 5]
     * @param shape Shape index of the ice crystal. Range: [0, 5]
     * @see <a href="https://doi.org/10.1029/1999JD900755">Parameterization of the scattering
     * and absorption properties of individual ice crystals (Paper)</a> for more details on solar coefficients.
     */
    private double calcQabsPrime(double dMax, int band, int shape) {
        // band ... das gewuenschte spektrale Band
        //   0 = 0.55 micrometer
        //   1 = 1.35 micrometer
        //   2 = 2.25 micrometer
        //   3 = 3.0125 micrometer
        //   4 = 3.775 micrometer
        //   5 = 4.5 micrometer
        // shape ... 0 = Plate
        //           1 = Column
        //           2 = Hollow Column
        //           3 = Bullet rosettes-4
        //           4 = Bullet rosettes-6
        //           5 = Aggregates
        double lambda = bands[band]; // wavelength [micrometer]
        double mi = table1[band][4];
        double chie = 2 * Math.PI * Calc_de(dMax, shape) / 2 / lambda; //effective size parameter of nonspherical ice crystal
        double gammae = 4 * chie * mi;
        double eta1_abs = table4[band][shape][1];
        double Qabsprime = 1 + 2. * Math.exp(-1. * gammae * eta1_abs) / (gammae * eta1_abs) + 2. * (Math.exp(-1 * gammae * eta1_abs) - 1) / Math.pow(gammae * eta1_abs, 2);
        return Qabsprime;
    }

    /**
     * Helper for calculating solar absorption efficiency without consideration of complex ray behavior.
     *
     * @param dMax  Maximum dimension in micrometers. Range: [2, 10'000]
     * @param band  Spectral band index. Range: [0, 5]
     * @param shape Shape index of the ice crystal. Range: [0, 5]
     * @see <a href="https://doi.org/10.1029/1999JD900755">Parameterization of the scattering
     * and absorption properties of individual ice crystals (Paper)</a> for more details on solar coefficients.
     */
    private double calcQabsPrimePrime(double dMax, int band, int shape) {
        // band ... das gewuenschte spektrale Band
        //   0 = 0.55 micrometer
        //   1 = 1.35 micrometer
        //   2 = 2.25 micrometer
        //   3 = 3.0125 micrometer
        //   4 = 3.775 micrometer
        //   5 = 4.5 micrometer
        // shape ... 0 = Plate
        //           1 = Column
        //           2 = Hollow Column
        //           3 = Bullet rosettes-4
        //           4 = Bullet rosettes-6
        //           5 = Aggregates
        double lambda = bands[band]; // wavelength [micrometer]
        double mi = table1[band][4];
        double chie = 2. * Math.PI * Calc_de(dMax, shape) / 2. / lambda; //effective size parameter of nonspherical ice crystal
        double gammae = 4. * chie * mi;
        double eta2_abs = table4[band][shape][2];

        double Qabsprimeprime = 1 - Math.exp(-2. / 3. * gammae * eta2_abs);
        return Qabsprimeprime;
    }

    @Override
    public double getLambda(int band) {
        return table1[band][0];
    }

    @Override
    public double calcQext(double dMax, int band) {
        double[] f = getF(dMax);

        double numerator = 0.;
        double denominator = 0.;
        for (int i = 0; i < 6; i++) {
            numerator += f[i] * Math.pow(Calc_Da(dMax, i), 2) * calcQext(dMax, band, i);
            denominator += f[i] * Math.pow(Calc_Da(dMax, i), 2);
        }

        double Qext_sol = numerator / denominator;
        return Qext_sol;
    }

    @Override
    public double calcQext(double dMax, int band, int shape) {
        double Qextprime = calcQextPrime(dMax, band, shape);
        double Qextprimeprime = calcQextPrimePrime(dMax, band, shape);
        double xi_ext = table3[band][shape][6];

        double Qext_sol = (1. - xi_ext) * Qextprime + xi_ext * Qextprimeprime;
        return Qext_sol;
    }

    @Override
    public double calcQabs(double dMax, int band) {
        double[] f = getF(dMax);

        double numerator = 0.;
        double denominator = 0.;
        for (int i = 0; i < 6; i++) {
            numerator += f[i] * Math.pow(Calc_Da(dMax, i), 2) * calcQabs(dMax, band, i);
            denominator += f[i] * Math.pow(Calc_Da(dMax, i), 2);
        }

        double Qabs_sol = numerator / denominator;
        return Qabs_sol;
    }

    @Override
    public double calcQabs(double dMax, int band, int shape) {
        double Qabsprime = calcQabsPrime(dMax, band, shape);
        double Qabsprimeprime = calcQabsPrimePrime(dMax, band, shape);
        double xi1_abs = table4[band][shape][3];
        double xi2_abs = table4[band][shape][4];

        double Qabs_sol = (1 - xi1_abs) * ((1 - xi2_abs) * Qabsprime + xi2_abs * Qabsprimeprime);
        return Qabs_sol;
    }

    @Override
    public double calcQsca(double dMax, int band) {
        return calcQext(dMax, band) - calcQabs(dMax, band);
    }

    @Override
    public double calcQsca(double dMax, int band, int shape) {
        return calcQext(dMax, band, shape) - calcQabs(dMax, band, shape);
    }

    @Override
    public double calcG(double d, int band) {
        double[] f = getF(d);

        double numerator = 0.;
        double denominator = 0.;
        for (int i = 0; i < 6; i++) {
            numerator += f[i] * Math.pow(Calc_Da(d, i), 2) * calcQsca(d, band, i) * calcG(d, band, i);
            denominator += f[i] * Math.pow(Calc_Da(d, i), 2) * calcQsca(d, band, i);
        }

        double g_sol = numerator / denominator;
        return g_sol;
    }

    @Override
    public double calcG(double d, int band, int shape) {
        double lambda = bands[band]; // wavelength [micrometer]
        double chie = 2. * Math.PI * Calc_de(d, shape) / 2. / lambda; // Effective size of non-spherical ice crystal
        double xi1_g = table5[band][shape][1];
        double xi2_g = table5[band][shape][2];
        double xi3_g = table5[band][shape][3];
        double xi4_g = table5[band][shape][4];
        double xi5_g = table5[band][shape][5];
        double xi6_g = table5[band][shape][6];
        double xi7_g = table5[band][shape][7];

        double f1 = (1 - xi1_g) * (1 - (1 - Math.exp(-1. * xi2_g * (chie + xi3_g))) / (xi2_g * (chie + xi3_g)));
        double f2 = (1 - xi4_g) * (1 - Math.exp(-1. * xi5_g * (chie + xi6_g)));
        double g_sol = (1 - xi7_g) * f1 + xi7_g * f2; // Solar asymmetry factor
        return g_sol;
    }
}
