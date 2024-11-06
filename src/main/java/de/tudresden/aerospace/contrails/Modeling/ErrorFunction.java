package de.tudresden.aerospace.contrails.Modeling;

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

import java.lang.Math;

/**
 * Groups functionality for computing error functions.
 *
 * @author Thomas Rosenow
 */
public class ErrorFunction {

    // Constants, see paper linked in erf JavaDoc
    private static double a1 = 0.254829592;
    private static double a2 = -0.284496736;
    private static double a3 = 1.421413741;
    private static double a4 = -1.453152027;
    private static double a5 = 1.061405429;
    private static double p = 0.3275911;

    /**
     * Computes a relational approximation error according to the formula of the <a
     * href="https://personal.math.ubc.ca/~cbm/aands/abramowitz_and_stegun.pdf"> Handbook of Mathematical Functions
     * 7.1.26</a>.
     */
    public static double erf(double x) {
        double sign = Math.signum(x);
        x = sign * x; // abs(x)

        // A&S formula 7.1.26
        double t = 1.0 / (1.0 + p * x);
        double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);

        return sign * y; // erf(-x)=-erf(x)
    }

}
