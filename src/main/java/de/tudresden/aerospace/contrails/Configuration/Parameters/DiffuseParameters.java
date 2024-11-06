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
 * Mirrors the structure of the {@code solar_diffuse} and {@code terrestrial_diffuse} subtrees of the XML config file
 * for the simulation. <a href="https://eclipse-ee4j.github.io/jaxb-ri/">Jakarta XML Binding (JAXB)</a>
 * is used to marshal/unmarshal XML config files to/from Java objects. In the process, values of this class are bound to
 * the corresponding XML values.
 */
@XmlRootElement
@XmlType(propOrder = {"binsPhi", "binsTheta", "distance", "resolutionS", "numSca", "numIceParticles", "lambda",
        "spectralBandIndex", "g", "absorptionFactor", "scatteringFactor", "incidentRadius", "dropletRadius",
        "sigmaH","sigmaV", "sigmaS"})
public class DiffuseParameters implements Cloneable {

    /**
     * Gets the number of bins in the azimuth coordinate phi.
     *
     * @return the number of bins for phi
     */
    public Integer getBinsPhi() {
        return binsPhi;
    }

    /**
     * Sets the number of bins in the zenith coordinate phi to the given value.
     *
     * @param binsPhi The new value to set
     */
    @XmlElement(name = ParameterNames.BINS_PHI, required = true)
    public void setBinsPhi(Integer binsPhi) {
        this.binsPhi = binsPhi;
    }

    /**
     * Gets the number of bins in the zenith coordinate theta.
     *
     * @return the number of bins for theta
     */
    public Integer getBinsTheta() {
        return binsTheta;
    }

    /**
     * Sets the number of bins in the azimuth coordinate theta to the given value.
     *
     * @param binsTheta The new value to set
     */
    @XmlElement(name = ParameterNames.BINS_THETA, required = true)
    public void setBinsTheta(Integer binsTheta) {
        this.binsTheta = binsTheta;
    }

    /**
     * Gets the length of the contrail.
     *
     * @return The length of the contrail
     */
    public Double getDistance() {
        return distance;
    }

    /**
     * Sets the length of the contrail to the given value.
     *
     * @param distance The new value to set
     */
    @XmlElement(name = ParameterNames.DISTANCE, required = true)
    public void setDistance(Double distance) {
        this.distance = distance;
    }

    /**
     * Gets the spatial resolution of the simulation.
     *
     * @return The spatial resolution
     */
    public Integer getResolutionS() {
        return resolutionS;
    }

    /**
     * Sets the spatial resolution of the simulation to the given value.
     *
     * @param resolutionS The new value to set
     */
    @XmlElement(name = ParameterNames.RESOLUTION_S, required = false)
    public void setResolutionS(Integer resolutionS) {
        this.resolutionS = resolutionS;
    }

    /**
     * Gets the spectral band index.
     *
     * @return The spectral band index
     */
    public Integer getSpectralBandIndex() {
        return spectralBandIndex;
    }

    /**
     * Sets the spectral band index to the given value.
     *
     * @param index The new value to set
     */
    @XmlElement(name = ParameterNames.SPECTRAL_BAND_INDEX, required = false)
    public void setSpectralBandIndex(Integer index) {
        this.spectralBandIndex = index;
    }

    /**
     * Gets the asymmetry factor g. When marshalling, this value may be {@code null}, as it is computed from other
     * parameters by the model.
     *
     * @return The asymmetry factor or {@code null} if not initialized
     */
    public Double getG() {
        return g;
    }

    /**
     * Sets the asymmetry factor g to the given value.
     *
     * @param g The new value to set
     */
    @XmlElement(name = ParameterNames.G, required = false)
    public void setG(Double g) {
        this.g = g;
    }

    /**
     * Gets the absorption factor q_abs. When marshalling, this value may be {@code null}, as it is computed from other
     * parameters by the model.
     *
     * @return The absorption factor or {@code null} if not initialized
     */
    public Double getAbsorptionFactor() {
        return absorptionFactor;
    }

    /**
     * Sets the absorption factor g to the given value.
     *
     * @param absorptionFactor The new value to set
     */
    @XmlElement(name = ParameterNames.ABSORPTION_FACTOR, required = false)
    public void setAbsorptionFactor(Double absorptionFactor) {
        this.absorptionFactor = absorptionFactor;
    }

    /**
     * Gets the scattering factor q_abs. When marshalling, this value may be {@code null}, as it is computed from other
     * parameters by the model.
     *
     * @return The scattering factor or {@code null} if not initialized
     */
    public Double getScatteringFactor() {
        return scatteringFactor;
    }

    /**
     * Sets the scattering factor g to the given value.
     *
     * @param scatteringFactor The new value to set
     */
    @XmlElement(name = ParameterNames.SCATTERING_FACTOR, required = false)
    public void setScatteringFactor(Double scatteringFactor) {
        this.scatteringFactor = scatteringFactor;
    }

    /**
     * Gets the incident radius.
     *
     * @return The incident radius
     */
    public Double getIncidentRadius() {
        return incidentRadius;
    }

    /**
     * Sets the incident radius to the given value.
     *
     * @param incidentRadius The new value to set
     */
    @XmlElement(name = ParameterNames.RADIUS_INCIDENT, required = true)
    public void setIncidentRadius(Double incidentRadius) {
        this.incidentRadius = incidentRadius;
    }

    /**
     * Gets the droplet radius.
     *
     * @return The droplet radius
     */
    public Double getDropletRadius() {
        return dropletRadius;
    }

    /**
     * Sets the droplet radius to the given value.
     *
     * @param dropletRadius The new value to set
     */
    @XmlElement(name = ParameterNames.RADIUS_DROPLET, required = false)
    public void setDropletRadius(Double dropletRadius) {
        this.dropletRadius = dropletRadius;
    }

    /**
     * Gets the number of classes of the scattering phase function.
     *
     * @return The number of classes
     */
    public Integer getNumSca() {
        return numSca;
    }

    /**
     * Sets the number of classes of the scattering phase function to the given value.
     *
     * @param numSca The new value to set
     */
    @XmlElement(name = ParameterNames.NUM_SCA, required = true)
    public void setNumSca(Integer numSca) {
        this.numSca = numSca;
    }

    /**
     * Gets the physical parameter sigma_h.
     *
     * @return The value of sigma_h
     */
    public Double getSigmaH() {
        return sigmaH;
    }

    /**
     * Sets the physical parameter sigma_h to the given value.
     *
     * @param sigmaH The new value to set
     */
    @XmlElement(name = ParameterNames.SIGMA_H, required = true)
    public void setSigmaH(Double sigmaH) {
        this.sigmaH = sigmaH;
    }

    /**
     * Gets the physical parameter sigma_v.
     *
     * @return The value of sigma_v
     */
    public Double getSigmaV() {
        return sigmaV;
    }

    /**
     * Sets the physical parameter sigma_v to the given value.
     *
     * @param sigmaV The new value to set
     */
    @XmlElement(name = ParameterNames.SIGMA_V, required = true)
    public void setSigmaV(Double sigmaV) {
        this.sigmaV = sigmaV;
    }

    /**
     * Gets the physical parameter sigma_s. When marshalling, this value may be {@code null}, as it is initialized by
     * the model in case the of terrestrial model.
     *
     * @return The value of sigma_s or {@code null} if not initialized
     */
    public Double getSigmaS() {
        return sigmaS;
    }

    /**
     * Sets the physical parameter sigma_s to the given value.
     *
     * @param sigmaS The new value to set
     */
    @XmlElement(name = ParameterNames.SIGMA_S, required = false)
    public void setSigmaS(Double sigmaS) {
        this.sigmaS = sigmaS;
    }

    /**
     * Gets the wavelength parameter lambda.
     *
     * @return The wavelength lambda
     */
    public Double getLambda() {
        return lambda;
    }

    /**
     * Sets the wavelength lambda to the given value.
     *
     * @param lambda The new value to set
     */
    @XmlElement(name = ParameterNames.LAMBDA, required = false)
    public void setLambda(Double lambda) {
        this.lambda = lambda;
    }

    /**
     * Gets the number of ice particles in the contrail.
     *
     * @return The number of ice particles
     */
    public Double getNumIceParticles() {
        return numIceParticles;
    }

    /**
     * Sets the number of ice particles in the contrail to the given value.
     *
     * @param numIceParticles The new value to set
     */
    @XmlElement(name = ParameterNames.NUM_ICE, required = true)
    public void setNumIceParticles(Double numIceParticles) {
        this.numIceParticles = numIceParticles;
    }

    /**
     * No-parameters constructor to allow client to initialize the object.
     */
    public DiffuseParameters() {}

    /**
     * Copy constructor for use with {@link Cloneable} interface.
     *
     * @param obj The object to copy parameters from.
     */
    public DiffuseParameters(DiffuseParameters obj) {
        this.binsPhi = obj.binsPhi;
        this.binsTheta = obj.binsTheta;
        this.resolutionS = obj.resolutionS;
        this.distance = obj.distance;
        this.g = obj.g;
        this.spectralBandIndex = obj.spectralBandIndex;
        this.absorptionFactor = obj.absorptionFactor;
        this.scatteringFactor = obj.scatteringFactor;
        this.incidentRadius = obj.incidentRadius;
        this.dropletRadius = obj.dropletRadius;
        this.numSca = obj.numSca;
        this.sigmaH = obj.sigmaH;
        this.sigmaV = obj.sigmaV;
        this.sigmaS = obj.sigmaS;
        this.lambda = obj.lambda;
        this.numIceParticles = obj.numIceParticles;
    }

    // Simulation concern
    /**
     * Number of bins for incident direction phi (only for multiple direction simulation)
     */
    private Integer binsPhi = null;

    /**
     * Number of bins for incident direction theta (only for multiple direction simulation)
     */
    private Integer binsTheta = null;

    // Modeling concern
    /**
     * Spatial resolution the simulation
     */
    private Integer resolutionS = null;

    /**
     * The length of the contrail section
     */
    private Double distance = null;

    /**
     * Spectral band index
     */
    private Integer spectralBandIndex = null;

    /**
     * Asymmetry factor
     */
    private Double g = null;

    /**
     * Absorption efficiency
     */
    private Double absorptionFactor = null;

    /**
     * Scattering efficiency
     */
    private Double scatteringFactor = null;

    /**
     * Radius of surrounding cylinder around droplets/ice crystals for incident photons
     */
    private Double incidentRadius = null;

    /**
     * Radius of droplets in the contrail
     */
    private Double dropletRadius = null;

    /**
     * Number of classes of the scattering phase function
     */
    private Integer numSca = null;

    /**
     * Physical property of the contrail, see paper
     */
    private Double sigmaH = null;

    /**
     * Physical property of the contrail, see paper
     */
    private Double sigmaV = null;

    /**
     * Physical property of the contrail, see paper
     */
    private Double sigmaS = null;

    /**
     * Wavelength of the evaluated radiation
     */
    private Double lambda = null;

    /**
     * Number of ice crystals in the contrail section
     */
    private Double numIceParticles = null;

    /**
     * Checks whether all parameters are initialized.
     *
     * @return {@code false} if any of the parameters are not initialized, {@code true} otherwise
     */
    public boolean isInitialized() {
        // If performance is not a concern, use reflection in superclass instead
        if (this.binsPhi == null)
            return false;
        if (this.binsTheta == null)
            return false;
        if (this.resolutionS == null)
            return false;
        if (this.distance == null)
            return false;
        if (this.g == null)
            return false;
        if (this.spectralBandIndex == null)
            return false;
        if (this.absorptionFactor == null)
            return false;
        if (this.scatteringFactor == null)
            return false;
        if (this.incidentRadius == null)
            return false;
        if (this.dropletRadius == null)
            return false;
        if (this.numSca == null)
            return false;
        if (this.sigmaH == null)
            return false;
        if (this.sigmaV == null)
            return false;
        if (this.sigmaS == null)
            return false;
        if (this.lambda == null)
            return false;
        if (this.numIceParticles == null)
            return false;

        return true;
    }

    /**
     * Performs a deep copy of the current object.
     *
     * @return The copied {@link DiffuseParameters} object
     */
    @Override
    public DiffuseParameters clone() {
        // Deep copy because all members' types are immutable classes
        return new DiffuseParameters(this);
    }

    /**
     * Performs a sanity check on this object's parameters.
     */
    public void check() {
        if (binsPhi == null) {
            System.err.println("Failed to read parameter " + ParameterNames.BINS_PHI);
            System.exit(1);
        }

        if (binsTheta == null) {
            System.err.println("Failed to read parameter " + ParameterNames.BINS_THETA);
            System.exit(1);
        }

        if (resolutionS == null) {
            System.err.println("Failed to read parameter " + ParameterNames.RESOLUTION_S);
            System.exit(1);
        }

        if (180 % resolutionS != 0) {
            System.err.println(ParameterNames.RESOLUTION_S + " must be a divisor of 180, please adjust the configuration file");
            System.exit(1);
        }

        if (distance == null) {
            System.err.println("Failed to read parameter " + ParameterNames.DISTANCE);
            System.exit(1);
        }

        if (spectralBandIndex == null) {
            System.err.println("Failed to read parameter " + ParameterNames.SPECTRAL_BAND_INDEX);
            System.exit(1);
        }

        // No g, absorption and scattering factor as they are calculated parameters

        if (incidentRadius == null) {
            System.err.println("Failed to read parameter " + ParameterNames.RADIUS_INCIDENT);
            System.exit(1);
        }

        if (dropletRadius == null) {
            System.err.println("Failed to read parameter " + ParameterNames.RADIUS_DROPLET);
            System.exit(1);
        }

        if (numSca == null) {
            System.err.println("Failed to read parameter " + ParameterNames.NUM_SCA);
            System.exit(1);
        }

        if (sigmaH == null) {
            System.err.println("Failed to read parameter " + ParameterNames.SIGMA_H);
            System.exit(1);
        }

        if (sigmaV == null) {
            System.err.println("Failed to read parameter " + ParameterNames.SIGMA_V);
            System.exit(1);
        }

        // No sigmaS check as it is not required

        if (numIceParticles == null) {
            System.err.println("Failed to read parameter " + ParameterNames.NUM_ICE);
            System.exit(1);
        }

        // No lambda as it a calculated parameter
    }

    /**
     * Returns a comment string representation of this object.
     *
     * @return The comment string representation, with every line starting with {@code //}
     */
    public String toCommentString() {
        StringBuilder sb = new StringBuilder();
        sb.append("// " + ParameterNames.BINS_PHI + " = " + this.binsPhi + "\n");
        sb.append("// " + ParameterNames.BINS_THETA + " = " + this.binsTheta + "\n");
        sb.append("// " + ParameterNames.RESOLUTION_S + " = " + this.resolutionS + "\n");
        sb.append("// " + ParameterNames.DISTANCE + " = " + this.distance + "\n");
        sb.append("// " + ParameterNames.SPECTRAL_BAND_INDEX + " = " + this.spectralBandIndex + "\n");

        // Some values are calculated, only print if they have been initialized
        if (this.g != null)
            sb.append("// " + ParameterNames.G + " = " + this.g + "\n");

        if (this.absorptionFactor != null)
            sb.append("// " + ParameterNames.ABSORPTION_FACTOR + " = " + this.absorptionFactor + "\n");

        if (this.scatteringFactor != null)
            sb.append("// " + ParameterNames.SCATTERING_FACTOR + " = " + this.scatteringFactor + "\n");

        if (this.lambda != null)
            sb.append("// " + ParameterNames.LAMBDA + " = " + this.lambda + "\n");

        sb.append("// " + ParameterNames.RADIUS_INCIDENT + " = " + this.incidentRadius + "\n");
        sb.append("// " + ParameterNames.RADIUS_DROPLET + " = " + this.dropletRadius + "\n");
        sb.append("// " + ParameterNames.NUM_SCA + " = " + this.numSca + "\n");
        sb.append("// " + ParameterNames.SIGMA_H + " = " + this.sigmaH + "\n");
        sb.append("// " + ParameterNames.SIGMA_V + " = " + this.sigmaV + "\n");

        // Calculated value
        if (this.sigmaS != null)
            sb.append("// " + ParameterNames.SIGMA_S + " = " + this.sigmaS + "\n");

        sb.append("// " + ParameterNames.NUM_ICE + " = " + this.numIceParticles + "\n");

        return sb.toString();
    }
}
