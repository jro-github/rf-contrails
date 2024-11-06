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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a block for a single wavelength in a Libradtran output file of uvspec format. Can also be partially
 * instantiated to represent other formats that contain only a subset of variables, e.g. twostr / rodents.
 */
public class LibradtranBlock {
    // Header block
    /**
     * Wavelength of the block [nm]
     */
    public Double lambda;

    /**
     * Direct beam irradiance w.r.t. horizontal plane [same unit as extraterrestrial irradiance]
     */
    public Double edir;

    /**
     * Diffuse down irradiance, i.e. total minus direct beam [same unit as edir]
     */
    public Double edn;

    /**
     * Diffuse up irradiance, i.e. total minus direct beam [same unit as edir]
     */
    public Double eup;

    /**
     * Direct beam contribution to the mean intensity [same unit as edir]
     */
    public Double uavgdir;

    /**
     * Diffuse downward radiation contribution to the mean intensity [same unit as edir]
     */
    public Double uavgdn;

    /**
     * Diffuse upward radiation contribution to the mean intensity [same unit as edir]
     */
    public Double uavgup;

    // Store vectors and table
    /**
     * Cosine of viewing zenith angle. Values > 0 indicate a downward-looking sensor, values < 0 indicate an
     * upward-looking sensor.
     */
    public List<Double> umu;

    /**
     * Azimuthally averaged intensity at {@code numu} user specified angles umu [unit depending on used spectrum]. Note
     * that the intensity correction included in the disort solver is not applied to u0u, thus u0u can deviate from the
     * azimuthally-averaged intensity-corrected uu.
     */
    public List<Double> u0u;

    /**
     * Radiance (intensity) at umu (rows) and phi (columns) user specified angles [unit depending on used spectrum]
     */
    public DynamicTable<Double> uu;

    public LibradtranBlock() {
        // Create objects
        this.umu = new ArrayList<>();
        this.u0u = new ArrayList<>();
        this.uu = new DynamicTable<>();
    }

    /**
     * Checks if the block has been initialized.
     *
     * @return {@code true} if initialized, {@code false} otherwise
     */
    public boolean isInitialized() {
        return uu.isInitialized() && uu.getRows().size() > 0;
    }

    /**
     * Converts a libradtran phi angle (in radians) to the domain of this program's MonteCarlo simulation output phi
     * angles. In this program's domain, phi is denoted as the angle of deviation from the aircraft heading {@code psi},
     * whereas in libradtran's output, phi is a deviation from geographical south (180° or pi in radians).
     *
     * @param psi           The aircraft heading angle in degrees
     * @param phiLibradtran Phi from libradtran in degrees
     * @return The converted angle in degrees
     */
    public static Double phiLibradtranToPhiMC(Double psi, Double phiLibradtran) {
        // First convert libradtran phi to value so that it is a deviation of geo north (0° or 0 radians)
        return phiLibradtran - 180.0 + psi;
    }

    public static void main(String[] args) {
        for (int i = 1; i <= 360; i += 2) {
            System.out.println(Math.toRadians(phiLibradtranToPhiMC(180.0, (double) i)));
        }
    }

    /**
     * Picks a radiance value from the libradtran block, which most closely matches the given angles.
     *
     * @param theta Zenith angle in radians
     * @param phi   Azimuth angle in radians
     * @param psi   The aircraft heading angle in degrees for conversion of phi angles
     * @return The closest matching radiance value
     */
    public Double matchDiffuseRadiance(double theta, double phi, double psi) {
        // theta corresponds to the inverse cos of umu (row)
        List<Double> distances = new ArrayList<>();
        for (int i = 0; i < umu.size(); i++) {
            double distance = Math.abs(Math.cos(theta) - umu.get(i));
            distances.add(distance);
        }

        // argmin gives index of minimum distance
        int row = MathHelpers.argMin(distances);

        // phi corresponds to columns of uu
        distances.clear();
        List<Double> phis = uu.getColumnNames().stream().map(Double::valueOf).collect(Collectors.toList());
        for (int i = 0; i < phis.size(); i++) {
            // phi in column headers is given in degrees
            double distance = Math.abs(Math.toDegrees(phi) - phiLibradtranToPhiMC(psi, phis.get(i)));
            distances.add(distance);
        }

        int col = MathHelpers.argMin(distances);

        return uu.getValue(row, col);
    }

    /**
     * Converts the Libradtran block into a human-readable string.
     *
     * @return The string representing the block as key-value pairs.
     */
    public String toString() {
        String res = "LIBRADTRAN_BLOCK\n";
        res += "lambda = " + lambda + ", ";
        res += "edir = " + edir + ", ";
        res += "edn = " + edn + ", ";
        res += "eup = " + eup + ", ";
        res += "uavgdir = " + uavgdir + ", ";
        res += "uavgdn = " + uavgdn + ", ";
        res += "uavgup = " + uavgup + "\n";

        String s_umu = umu.stream().map(Object::toString).collect(Collectors.joining(" "));
        res += "umu = " + s_umu + "\n";

        String s_u0u = u0u.stream().map(Object::toString).collect(Collectors.joining(" "));
        res += "u0u = " + s_u0u + "\n";

        res += "uu = " + uu;

        return res;
    }
}
