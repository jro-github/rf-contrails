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

import de.tudresden.aerospace.contrails.Configuration.IO.CustomCSVParser;
import de.tudresden.aerospace.contrails.Configuration.IO.DynamicTable;
import de.tudresden.aerospace.contrails.Modeling.Legacy.ExtinctionEfficiencyLegacy;
import de.tudresden.aerospace.contrails.Modeling.Parameters.PhysicalParameters;
import de.tudresden.aerospace.contrails.Modeling.Parameters.SolarParameters;
import de.tudresden.aerospace.contrails.Modeling.Parameters.TerrestrialParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

public class ExinctionEfficiencyTest {

    private ExtinctionEfficiencyLegacy eiLegacy;
    private PhysicalParameters eiSol;
    private PhysicalParameters eiTerr;
    private Random rand;

    @BeforeEach
    void setup() {
        eiLegacy = new ExtinctionEfficiencyLegacy();
        eiSol = new SolarParameters();
        eiTerr = new TerrestrialParameters();

        rand = new Random();
    }

    /**
     * Helper to generate a random test value for d_max within the legal interval.
     * @return d_max within range [2, 10000] (inclusive)
     */
    double dMaxRand() {
        return rand.nextDouble() * 9998.0 + 2.0;
    }

    @Test
    @DisplayName("ExtinctionEfficiency calculations correct")
    void testExtinctionEfficiency() {

        // Compare against original class
        // Test 10 random dMax values and iterative combinations of band and shape
        for (int k = 0; k < 10; k++) {
            double d = dMaxRand();

            // Test solar functions for all indices
            for (int i = 0; i < 6; i++) {
                assert(eiLegacy.Calc_g_sol(d, i) == eiSol.calcG(d, i));
                assert(eiLegacy.Calc_Qabs_sol(d, i) == eiSol.calcQabs(d, i));
                assert(eiLegacy.Calc_Qext_sol(d, i) == eiSol.calcQext(d, i));
                assert(eiLegacy.Calc_Qsca_sol(d, i) == eiSol.calcQsca(d, i));

                for (int j = 0; j < 6; j++) {
                    assert(eiLegacy.Calc_g_sol_i(d, i, j) == eiSol.calcG(d, i, j));
                    assert(eiLegacy.Calc_Qabs_sol_i(d, i, j) == eiSol.calcQabs(d, i, j));
                    assert(eiLegacy.Calc_Qext_sol_i(d, i, j) == eiSol.calcQext(d, i, j));
                    assert(eiLegacy.Calc_Qsca_sol_i(d, i, j) == eiSol.calcQsca(d, i, j));
                }
            }

            // Test terrestrial functions for all indices
            // Load table (assumes correct parsing!)
            List<DynamicTable<Double>> table_terr = CustomCSVParser.parse("Yang2005_terrestrSpektrum.txt");
            Double[][] table_terr1 = table_terr.get(0).toArray(Double.class);

            for (int i = 0; i < 49; i++) {
                double l = table_terr1[i][0]; // Get lambda from table for legacy test
                assert(eiLegacy.Calc_g_terr(d, l) == eiTerr.calcG(d, i));
                assert(eiLegacy.Calc_Qabs_terr(d, l) == eiTerr.calcQabs(d, i));
                assert(eiLegacy.Calc_Qext_terr(d, l) == eiTerr.calcQext(d, i));
                assert(eiLegacy.Calc_Qsca_terr(d, l) == eiTerr.calcQsca(d, i));
            }
        }
    }
}
