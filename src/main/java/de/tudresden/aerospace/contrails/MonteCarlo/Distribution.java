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

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Models a simple probability distribution and offers functionality to pick random samples and retrieve values given a
 * probability.
 *
 * @author Thomas Rosenow
 */
public class Distribution {

    /**
     * The values of the distribution.
     */
    private double[] x;

    /**
     * The values of the distribution extended at the boundary by an additional step of {@link #dx}.
     */
    private double[] xn;

    /**
     * The probabilities associated with the values x[].
     */
    private double[] p;

    /**
     * The <em>uniform</em> distance between discrete values of x[].
     */
    private double dx;

    /**
     * Represents the cumulative distribution function. Each index i holds the sum of probabilities from 0 to i. The
     * final value holds the total sum.
     */
    private double[] I;

    /**
     * The total sum of probabilities in p[].
     */
    double Ihelp;

    /**
     * For picking random values from the probability distribution.
     */
    Random r;

    /**
     * Initializes the probability distribution from an array of values and a corresponding array of probabilities.
     *
     * @param x The array of values.
     * @param p The array of probabilities.
     */
    public Distribution(double[] x, double[] p) {
        // x ... center of class
        // p ... propability
        this.x = x.clone(); // create copy of x
        this.p = p.clone(); // create copy of p

        this.dx = this.x[1] - this.x[0];
        this.xn = new double[this.x.length + 2]; // new class centers
        this.I = new double[this.x.length + 2]; // sum of propability

        // fill new class centers
        for (int i = 1; i < this.xn.length - 1; i = i + 1) {
            this.xn[i] = this.x[i - 1];
        }
        ;
        this.xn[0] = this.x[0] - this.dx;
        this.xn[this.xn.length - 1] = this.xn[this.xn.length - 2] + this.dx;

        // fill sum of propability
        this.I[0] = 0;
        this.I[1] = this.p[0];
        for (int i = 1; i < this.p.length; i = i + 1) {
            this.I[i + 1] = this.I[i] + this.p[i];
        }
        ;
        this.I[this.I.length - 1] = this.I[this.I.length - 2];

        this.Ihelp = this.I[this.I.length - 1];

        // initialize random number generator
        this.r = new Random();
    }

    /**
     * Sets the random number provider that is used to sample the probability distribution. Intended for testing.
     *
     * @param r The random provider object as an instance of {@link Random}.
     */
    public void setRandomProvider(Random r) {
        this.r = r;
    }

    /**
     * Computes X(p) as the inverse of P(x) using linear interpolation.
     *
     * @param P The probability to find the corresponding value for.
     * @return The inverse of {@code P} or {@code -777} if the value is outside the range of cumulative probabilities.
     */
    private double inverse(double P) {
        double m = 0;
        double n = 0;
        for (int i = 0; i < this.I.length; i = i + 1) {
            if (this.I[i] >= P) {
                m = this.dx / (this.I[i] - this.I[i - 1]);
                n = this.xn[i] - m * this.I[i];
                return m * P + n + this.dx / 2.;
            }

        }

        return -777.;
    }

    /**
     * Picks a random sample from the probability distribution.
     *
     * @return The picked sample.
     */
    public double random() {
        double z1 = this.r.nextDouble() * this.Ihelp;
        return this.inverse(z1);
    }

    /**
     * Picks a random sample from the probability distribution using a {@link ThreadLocalRandom} generator. This is more
     * performant in multi-threaded applications and should only be used in a multi-threaded environment.
     *
     * @return
     */
    public double randomThreadSafe() {
        double z1 = ThreadLocalRandom.current().nextDouble() * this.Ihelp;
        return this.inverse(z1);
    }
}
