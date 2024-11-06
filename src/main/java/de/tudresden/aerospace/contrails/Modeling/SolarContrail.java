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

import de.tudresden.aerospace.contrails.Configuration.Parameters.DiffuseParameters;
import de.tudresden.aerospace.contrails.Modeling.Parameters.SolarParameters;

import java.lang.*;
import java.util.*;

/**
 * Groups functionality for running Monte Carlo simulations on a contrail's interaction with radiation. This class only
 * computes the solar part of radiation, the terrestrial part is handled by {@link TerrestrialContrail}.
 *
 * @author Thomas Rosenow
 */
public class SolarContrail extends Contrail {
    /**
     * Initializes the solar contrail object with the given parameters.
     *
     * @param params The parameters to use in the model.
     */
    public SolarContrail(DiffuseParameters params) { super(params); }

    /**
     * Initializes random with a seed to allow for reproducibility for debugging and testing purposes.
     *
     * @param params The parameters to use in the model.
     * @param randomProvider A custom {@link Random} object that is used for random number generation. Intended for
     *                       testing, e.g. with mock objects.
     */
    public SolarContrail(DiffuseParameters params, Random randomProvider) {
        super(params, randomProvider);
    }

    @Override
    protected void initialize() {
        // Initialize with solar parameters
        initializePhysicalParameters(new SolarParameters());
    }
}
