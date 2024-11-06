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
 * Mirrors the structure of the {@code solar_direct} subtree of the XML config file for the simulation.
 * <a href="https://eclipse-ee4j.github.io/jaxb-ri/">Jakarta XML Binding (JAXB)</a> is used to marshal/unmarshal XML
 * config files to/from Java objects. In the process, values of this class are bound to the corresponding XML values.
 */
@XmlRootElement
@XmlType(propOrder = {"sza", "phi0"})
public class DirectParameters implements Cloneable {
    /**
     * Gets the solar zenith angle sza.
     *
     * @return The solar zenith angle sza
     */
    public Double getSza() {
        return sza;
    }

    /**
     * Sets the solar zenith angle to the given value.
     *
     * @param sza The new value to set
     */
    @XmlElement(name = ParameterNames.SZA, required = true)
    public void setSza(Double sza) {
        this.sza = sza;
    }

    /**
     * Gets the solar azimuth angle phi0.
     *
     * @return The solar azimuth angle phi0
     */
    public Double getPhi0() {
        return phi0;
    }

    /**
     * Sets the solar azimuth angle to the given value.
     *
     * @param phi0 The new value to set
     */
    @XmlElement(name = ParameterNames.PHI0, required = true)
    public void setPhi0(Double phi0) {
        this.phi0 = phi0;
    }

    /**
     * No-parameters constructor to allow client to initialize the object.
     */
    public DirectParameters() {}

    /**
     * Creates the solar direct parameters object with the given parameters.
     *
     * @param sza The solar zenith angle
     * @param phi0 The solar azimuth angle
     */
    public DirectParameters(Double sza, Double phi0) {
        this.sza = sza;
        this.phi0 = phi0;
    }

    /**
     * Copy constructor for use with {@link Cloneable} interface.
     *
     * @param obj The object to copy parameters from.
     */
    public DirectParameters(DirectParameters obj) {
        this(obj.sza, obj.phi0);
    }

    /**
     * Solar zenith angle for direct part of solar radiation
     */
    private Double sza = null;

    /**
     * Solar azimuth angle for direct part of solar radiation
     */
    private Double phi0 = null;

    /**
     * Performs a deep copy of the current object.
     *
     * @return The copied {@link DirectParameters} object
     */
    @Override
    public DirectParameters clone() {
        // Deep copy because all members' types are immutable classes
        return new DirectParameters(this);
    }

    /**
     * Performs a sanity check on this object's parameters.
     */
    public void check() {
        if (sza == null) {
            System.err.println("Failed to read parameter " + ParameterNames.SZA);
            System.exit(1);
        }

        if (phi0 == null) {
            System.err.println("Failed to read parameter " + ParameterNames.PHI0);
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
        sb.append("// " + ParameterNames.SZA + " = " + this.sza + "\n");
        sb.append("// " + ParameterNames.PHI0 + " = " + this.phi0 + "\n");

        return sb.toString();
    }
}
