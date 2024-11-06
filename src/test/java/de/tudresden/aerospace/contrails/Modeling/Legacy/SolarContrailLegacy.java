package de.tudresden.aerospace.contrails.Modeling.Legacy;

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

import de.tudresden.aerospace.contrails.Configuration.XMLParametersLegacy;
import de.tudresden.aerospace.contrails.Modeling.Contrail;
import de.tudresden.aerospace.contrails.Modeling.RePhoton;
import de.tudresden.aerospace.contrails.Modeling.TerrestrialContrail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Groups functionality for running Monte Carlo simulations on a contrail's interaction with radiation. This class only
 * computes the solar part of radiation, the terrestrial part is handled by {@link TerrestrialContrail}.
 *
 * @author Thomas Rosenow
 */
public class SolarContrailLegacy extends ContrailLegacy {

    /**
     * Initializes the solar contrail object with the given parameters.
     *
     * @param params The parameters to use in the model.
     */
    public SolarContrailLegacy(XMLParametersLegacy params) { super(params); }

    /**
     * Initializes random with a seed to allow for reproducibility for debugging and testing purposes.
     *
     * @param params The parameters to use in the model.
     * @param randomProvider A custom {@link Random} object that is used for random number generation. Intended for
     *                       testing, e.g. with mock objects.
     */
    public SolarContrailLegacy(XMLParametersLegacy params, Random randomProvider) {
        super(params, randomProvider);
    }

    @Override
    protected void initialize() {
        // If spectral band index is supplied, calculate values with ExtinctionEfficiency
        if (params.getSpectralBandIndex() != null) {
            System.out.println("g, absorption_factor and scattering_factor not supplied, calculating them from parameter files");
            ExtinctionEfficiencyLegacy aExt = new ExtinctionEfficiencyLegacy();

            params.setLambda(aExt.getLambda_sol(params.getSpectralBandIndex()));
            params.setG(aExt.Calc_g_sol(2.0 * params.getDropletRadius() * 1e6, params.getSpectralBandIndex()));
            params.setAbsorptionFactor(aExt.Calc_Qabs_sol(2.0 * params.getDropletRadius() * 1e6, params.getSpectralBandIndex()));
            params.setScatteringFactor(aExt.Calc_Qsca_sol(2.0 * params.getDropletRadius() * 1e6, params.getSpectralBandIndex()));
        } else {
            // Compatibility mode, more recent version does not have support for configurations with
            // g, q_abs and q_sca instead of spectral_band_index. When spectral band index is not supplied, check if
            // all required values are supplied
            if (params.getG() == null)
                throw new IllegalArgumentException("Parameters should contain g if spectral_band_index is not supplied");
            if (params.getAbsorptionFactor() == null)
                throw new IllegalArgumentException("Parameters should contain absorption_factor if spectral_band_index is not supplied");
            if (params.getScatteringFactor() == null)
                throw new IllegalArgumentException("Parameters should contain scattering_factor if spectral_band_index is not supplied");
            System.out.println("g, absorption_factor and scattering_factor supplied, not using parameter files");
        }
    }

    /**
     * Performs the Monte Carlo Simulation for a given number of angles of incidence, calculating the scattering towards
     * sky and ground as well as the absorption of light caused by the contrail. Output is written to a file.
     *
     * @param configFile The configuration file.
     * @param randomProvider If this is null, the {@link Contrail} object will use a new {@link Random}
     *                       instance, otherwise it will use the given object to generate random numbers.
     */
    @Deprecated
    public static void MultipleDirections(File configFile, Random randomProvider) {
        System.out.println("\n -= Monte Carlo simulation of optical contrail properties =-");
        System.out.println("             Multiple Direction Calculation\n");

        // Read parameters from config file
        XMLParametersLegacy params = XMLParametersLegacy.unmarshal(configFile);

        // angular dependent radiance
        double d_theta = Math.PI / (params.getBinsTheta());
        double d_phi = Math.PI * 2 / (params.getBinsPhi());


        // save result to files
        FileWriter fwDataOut = null;
        try {
            fwDataOut = new FileWriter(params.getOutputFilePrefix() + "_multi.txt");
        } catch (IOException ex) {
            System.err.println("Error creating the file");
        }


        // Initialize Monte Carlo - Simulation
        SolarContrailLegacy MS_Pilnitz;
        //public mc_contrail(double g, double Qabs, double Qsca, double r_incident, int n_sca, double precission)
        if (randomProvider == null) {
            MS_Pilnitz = new SolarContrailLegacy(params);
        } else {
            MS_Pilnitz = new SolarContrailLegacy(params, randomProvider);

            // Also set random provider of scattering phase function to mock object
            MS_Pilnitz.getScPhFun().setRandomProvider(randomProvider);
        }

        // Rueckgabewerte
        RePhoton sdir;

        // incident direction
        double theta = 0.;
        double phi = 0.;

        System.out.println("\n\n  - Starting the simulation for solar radiation. - \n");

        try {
            fwDataOut.write("theta (bins_theta= " + params.getBinsTheta() + "), phi (bins_phi= " + params.getBinsPhi() + "), S_" + params.getResolutionS() + " ........ S_180 (Resolution:  " + params.getResolutionS() + "°), Abs, scat_events, N_affected, Nabs /// " + params.getBinsTheta() + "," + params.getBinsPhi() + "," + params.getResolutionS() + "\n");
        } catch (IOException ex) {
            System.err.println("Error writing to output file");
        }


        for (int i_theta = 0; i_theta < params.getBinsTheta(); i_theta += 1) {
            for (int i_phi = 0; i_phi < params.getBinsPhi(); i_phi += 1) {
                // incident direction
                theta = (0.5 + i_theta) * d_theta;
                phi = (0.5 + i_phi) * d_phi;

                // Creating Array for counting the photons
                int[] N_Array;
                N_Array = new int[180 / params.getResolutionS()];

                for (int i = 0; i < 180 / params.getResolutionS(); i++)    // Initialisiert das Array überall mit dem Wert 0
                {
                    N_Array[i] = 0;
                }


                int N_abs = 0;
                int N_trans = 0;
                int N = 0;

                // Multiple Scattering ?
                double average_scat = 0.;

                // Each photon for itself
                for (int i = 0; i < params.getNumPhotons(); i = i + 1) {
                    if (i % (params.getNumPhotons() / 100) == 0) {
                        System.out.print("\r");
                        System.out.print(" Gesamtfortschritt: " + i_theta * 100 / params.getBinsTheta() + "% *****  Theta: " + i_theta + " Bins von " + params.getBinsTheta() + " berechnet.  *****  Phi: " + i_phi + " Bins von " + params.getBinsPhi() + " berechnet.  *****  " + (i + 1) + "/ " + params.getNumPhotons() + " Photonen berechnet.");
                    }
                    sdir = MS_Pilnitz.singlePhotonIntegration(theta, phi);
                    if (sdir.theta < 0) {
                        N_abs += 1;
                    } else {
                        if (sdir.Scat_events == 0) N_trans += 1;
                        if (sdir.Scat_events > 0) {
                            N += 1;

                            for (int j = 0; j < 180 / params.getResolutionS(); j++) {
                                if (180 % params.getResolutionS() != 0) {
                                    System.out.println("Simulation abgebrochen!\nAUFLÖSUNG für S ungeeignet, bitte Teiler von 180 wählen !");
                                    System.exit(0);
                                }
                                if (sdir.theta > Math.PI) {
                                    System.out.println("Simulation abgebrochen!\nTHETA ÜBERSCHREITET WERTEBEREICH ! theta > pi");
                                    System.exit(0);
                                }
                                if (Math.toRadians(params.getResolutionS() * j) < sdir.theta && sdir.theta <= Math.toRadians(params.getResolutionS() * j + params.getResolutionS())) {
                                    N_Array[j] += 1;
                                }
                            }

                            average_scat = (average_scat * (N - 1)) + (sdir.Scat_events) * 1. / N;

                        }
                    }
                }


                // ---== Auswertung ==---

                double alpha = Math.acos(Math.sin(theta) * Math.cos(phi));
                double S_abs = N_abs * params.getIncidentRadius() * 2.0 * Math.sin(alpha) / params.getNumPhotons();

                double[] S_Array;
                S_Array = new double[180 / params.getResolutionS()];

                for (int j = 0; j < 180 / params.getResolutionS(); j++) {
                    S_Array[j] = N_Array[j] * params.getIncidentRadius() * 2.0 * Math.sin(alpha) / params.getNumPhotons();
                }


                // ---== Ausgabe ==---
                //
                // Einfallswinkel (theta und phi)
                // Streuung nach oben und unten
                // Absorption
                //
                // theta, phi, S_2 bis 180, Abs, scat_events

                try {
                    fwDataOut.write(theta + ", " + phi + ", ");
                    for (int i = 0; i < 180 / params.getResolutionS(); i++) {
                        fwDataOut.write(S_Array[i] + ", ");
                    }
                    fwDataOut.write(+S_abs + ", " + average_scat + ", " + (params.getNumPhotons() - N_trans) + ", " + N_abs + "\n");

                } catch (IOException ex) {
                    System.err.println("Fehler beim Schreiben (Multi)");
                }
            }
        }

        try {
            fwDataOut.close();
        } catch (IOException ex) {
            System.err.println("Fehler beim Schreiben (Multi)");
        }

        System.out.print("\r 100%");
        System.out.println("\n");
        System.out.println(" - Simulation finished successfully! -\n");
    }

    public static void main(String[] args) {
        //System.out.println(System.getProperty("user.dir"));
        //SingleDirection ("Config_single.txt");
        //SingleDirectionTerr ("Config_single.txt");
        //MultipleDirections("Config_multi.txt");
        MultipleDirections(new File(args[0]), null);
    }
}
