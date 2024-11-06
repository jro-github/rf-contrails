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

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Mirrors the structure of the {@code common} subtree of the XML config file for the simulation.
 * <a href="https://eclipse-ee4j.github.io/jaxb-ri/">Jakarta XML Binding (JAXB)</a> is used to marshal/unmarshal XML
 * config files to/from Java objects. In the process, values of this class are bound to the corresponding XML values.
 */
@XmlRootElement
@XmlType(propOrder = {"outputFilePrefix", "numPhotons", "psi"})
public class CommonParameters implements Cloneable {

    /**
     * Gets the number of photons.
     *
     * @return The number of photons
     */
    public Integer getNumPhotons() {
        return numPhotons;
    }

    /**
     * Sets the number of photons to the given value.
     *
     * @param numPhotons The new value to set
     */
    @XmlElement(name = ParameterNames.NUM_PHOTONS, required = true)
    public void setNumPhotons(Integer numPhotons) {
        this.numPhotons = numPhotons;
    }

    /**
     * Gets the output file prefix.
     *
     * @return The output file prefix
     */
    public String getOutputFilePrefix() {
        return outputFilePrefix;
    }

    /**
     * Sets the output file prefix to the given value.
     *
     * @param outputFilePrefix The new value to set
     */
    @XmlElement(name = ParameterNames.OUT_FILE_PREFIX, required = true)
    public void setOutputFilePrefix(String outputFilePrefix) {
        this.outputFilePrefix = outputFilePrefix;
    }

    /**
     * Gets the aircraft heading psi.
     *
     * @return The aircraft heading azimuth direction
     */
    public Double getPsi() {
        return psi;
    }

    /**
     * Sets the aircraft heading psi to the new value.
     *
     * @param psi The new value to set
     */
    @XmlElement(name = ParameterNames.PSI, required = true)
    public void setPsi(Double psi) {
        this.psi = psi;
    }

    /**
     * No-parameters constructor to allow client to initialize the object.
     */
    public CommonParameters() {}

    /**

     * Creates the common parameters object with the given parameters.
     *
     * @param numPhotons The number of photons used in each step of the simulation
     * @param outputFilePrefix The prefix of the output file
     * @param psi The aircraft heading angle
     */
    public CommonParameters(Integer numPhotons, String outputFilePrefix, Double psi) {
        this.numPhotons = numPhotons;
        this.outputFilePrefix = outputFilePrefix;
        this.psi = psi;
    }

    /**
     * Copy constructor for use with {@link Cloneable} interface.
     *
     * @param obj The object to copy parameters from
     */
    public CommonParameters(CommonParameters obj) {
        this(obj.numPhotons, obj.outputFilePrefix, obj.psi);
    }

    /**
     * Number of photons used in each step of the simulation
     */
    private Integer numPhotons;

    /**
     * Prefix of the output file
     */
    private String outputFilePrefix;

    /**
     * Aircraft heading angle
     */
    private Double psi;


    /**
     * Performs a deep copy of the current object.
     *
     * @return The copied {@link CommonParameters} object
     */
    @Override
    public CommonParameters clone() {
        // Deep copy because all members' types are immutable classes
        return new CommonParameters(this);
    }

    /**
     * Performs a sanity check on this object's parameters.
     */
    public void check() {
        if (outputFilePrefix == null) {
            System.err.println("Failed to read parameter " + ParameterNames.OUT_FILE_PREFIX);
            System.exit(1);
        }

        if (numPhotons == null) {
            System.err.println("Failed to read parameter " + ParameterNames.NUM_PHOTONS);
            System.exit(1);
        }

        if (psi == null) {
            System.err.println("Failed to read parameter " + ParameterNames.PSI);
            System.exit(1);
        }

        if (psi < 0.0 || psi > 360.0) {
            System.err.println(ParameterNames.PSI + " must be between 0 and 360 degrees");
            System.exit(1);
        }
    }

    /**
     * Returns a comment string representation of this object.
     *
     * @return The comment string representation, with every line starting with {@code //}
     */
    public String toCommentString() {
        StringBuilder sb = new StringBuilder();
        // out_file_prefix not included for now as it breaks SolarSimulationTest
        //sb.append("// output_file_prefix = " + this.outputFilePrefix + "\n");
        sb.append("// " + ParameterNames.NUM_PHOTONS + " = " + this.numPhotons + "\n");
        sb.append("// " + ParameterNames.PSI + " = " + this.psi + "\n");

        return sb.toString();
    }
}
