package de.tudresden.aerospace.contrails.Configuration.Parameters;

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
 * Defines the names of parameters for serialization as {@link String} constants. The assigned constants correspond to
 * the names of the variables specified in the XML configuration file. For details on what the parameters are for, see
 * {@link XMLParameters}, {@link CommonParameters}, {@link DiffuseParameters} and {@link DirectParameters}.
 */
public class ParameterNames {
    /**
     * Private constructor to prevent creation of instance objects.
     */
    private ParameterNames() {}

    // XMLParameters.java
    public static final String COMMON = "common";
    public static final String SOLAR_DIFFUSE = "solar_diffuse";
    public static final String SOLAR_DIRECT = "solar_direct";
    public static final String TERRESTRIAL_DIFFUSE = "terrestrial_diffuse";

    // CommonParameters.java
    public static final String NUM_PHOTONS = "num_photons";
    public static final String OUT_FILE_PREFIX = "out_file_prefix";
    public static final String PSI = "psi";

    // DirectParameters.java
    public static final String SZA = "sza";
    public static final String PHI0 = "phi0";

    // DiffuseParameters.java
    public static final String BINS_PHI = "bins_phi";
    public static final String BINS_THETA = "bins_theta";
    public static final String DISTANCE = "distance";
    public static final String RESOLUTION_S = "resolution_s";
    public static final String SPECTRAL_BAND_INDEX = "spectral_band_index";
    public static final String G = "g";
    public static final String ABSORPTION_FACTOR = "absorption_factor";
    public static final String SCATTERING_FACTOR = "scattering_factor";
    public static final String RADIUS_INCIDENT = "radius_incident";
    public static final String RADIUS_DROPLET = "radius_droplet";
    public static final String NUM_SCA = "num_sca";
    public static final String SIGMA_H = "sigma_h";
    public static final String SIGMA_V = "sigma_v";
    public static final String SIGMA_S = "sigma_s";
    public static final String LAMBDA = "lambda";
    public static final String NUM_ICE = "num_ice";
}
