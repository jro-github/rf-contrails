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

import java.util.List;

/**
 * Provides implementations for the methods declared by {@link PhysicalParameters}. These calculate the physical
 * parameters lambda, qExt, qAbs, qSca and g for terrestrial radiation.
 * @see <a href="https://doi.org/10.1364/AO.44.005512">Scattering and absorption property database for
 * nonspherical ice particles in the near- through far-infrared spectral region (Paper)</a> for more details on
 * terrestrial coefficients.
 */
public class TerrestrialParameters extends PhysicalParameters {
    /**
     * For calculations in the terrestrial wavelength interval First dimension: spectral band index [0, 6]<br/> Second
     * dimension: value index<br/> 1 = eta1<br/> 2 = eta2<br/> 3 = eta3<br/> 4 = xi0<br/> 5 = xi1<br/> 6 = xi2<br/> 7 =
     * xi3<br/> 8 = zeta0<br/> 9 = zeta1<br/> 10 = zeta2<br/> 11 = zeta3
     */
    protected Double[][] table_terr1;

    /**
     * Creates the object to hold the calculated physical parameters for the terrestrial part.
     */
    public TerrestrialParameters() {
        super();

        // Read terrestrial spectrum table
        List<DynamicTable<Double>> tables = CustomCSVParser.parse("Yang2005_terrestrSpektrum.txt");
        table_terr1 = tables.get(0).toArray(Double.class);
    }

    @Override
    public double getLambda(int band) {
        return table_terr1[band][0];
    }

    /**
     * @param band Spectral band index into {@code Yang2005_terrestrSpektrum.txt}
     */
    @Override
    public double calcQext(double dMax, int band) {
        double de = Calc_de_mix(dMax);

        double eta1 = table_terr1[band][1];
        double eta2 = table_terr1[band][2];
        double eta3 = table_terr1[band][3];

        return (2. + eta1 / de) / (1. + eta2 / de + eta3 / Math.pow(de, 2.));
    }

    @Override
    public double calcQext(double dMax, int band, int shape) {
        // Not implemented for terrestrial spectrum
        return calcQext(dMax, band);
    }

    /**
     * @param band Spectral band index into {@code Yang2005_terrestrSpektrum.txt}
     */
    @Override
    public double calcQabs(double dMax, int band) {
        double de = Calc_de_mix(dMax);

        double xi0 = table_terr1[band][4];
        double xi1 = table_terr1[band][5];
        double xi2 = table_terr1[band][6];
        double xi3 = table_terr1[band][7];

        return (xi0 + xi1 / de) / (1. + xi2 / de + xi3 / Math.pow(de, 2.));
    }

    @Override
    public double calcQabs(double dMax, int band, int shape) {
        // Not implemented for terrestrial spectrum
        return calcQabs(dMax, band);
    }

    /**
     * @param band Spectral band index into {@code Yang2005_terrestrSpektrum.txt}
     */
    @Override
    public double calcQsca(double dMax, int band) {
        return calcQext(dMax, band) - calcQabs(dMax, band);
    }

    @Override
    public double calcQsca(double dMax, int band, int shape) {
        // Not implemented for terrestrial spectrum
        return calcQsca(dMax, band);
    }

    /**
     * @param d    Effective diameter in micrometers.
     * @param band Spectral band index into {@code Yang2005_terrestrSpektrum.txt}
     */
    @Override
    public double calcG(double d, int band) {
        double zeta0 = table_terr1[band][8];
        double zeta1 = table_terr1[band][9];
        double zeta2 = table_terr1[band][10];
        double zeta3 = table_terr1[band][11];

        return (zeta0 + zeta1 / d) / (1. + zeta2 / d + zeta3 / Math.pow(d, 2.));
    }

    @Override
    public double calcG(double d, int band, int shape) {
        // Not implemented for terrestrial spectrum
        return calcG(d, band);
    }
}
