package de.tudresden.aerospace.contrails.Configuration;

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

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@XmlRootElement(name = "parameters")
@XmlType(propOrder = {"outputFilePrefix", "numPhotons", "numSca", "numIceParticles", "distance", "binsPhi", "binsTheta",
        "phi", "theta", "lambda", "resolutionS", "spectralBandIndex", "g", "absorptionFactor", "scatteringFactor",
        "incidentRadius", "dropletRadius", "sigmaH","sigmaV", "sigmaS"})
public class XMLParametersLegacy  implements Cloneable {
    public Integer getNumPhotons() {
        return numPhotons;
    }

    @XmlElement(name = "num_photons", required = true)
    public void setNumPhotons(Integer numPhotons) {
        this.numPhotons = numPhotons;
    }

    public Integer getBinsPhi() {
        return binsPhi;
    }

    @XmlElement(name = "bins_phi", required = true)
    public void setBinsPhi(Integer binsPhi) {
        this.binsPhi = binsPhi;
    }

    public Integer getBinsTheta() {
        return binsTheta;
    }

    @XmlElement(name = "bins_theta", required = true)
    public void setBinsTheta(Integer binsTheta) {
        this.binsTheta = binsTheta;
    }

    public Double getPhi() {
        return phi;
    }

    @XmlElement(name = "phi", required = false)
    public void setPhi(Double phi) {
        this.phi = phi;
    }

    public Double getTheta() {
        return theta;
    }

    @XmlElement(name = "theta", required = false)
    public void setTheta(Double theta) {
        this.theta = theta;
    }

    public Integer getResolutionS() {
        return resolutionS;
    }

    @XmlElement(name = "resolution_s", required = false)
    public void setResolutionS(Integer resolutionS) {
        this.resolutionS = resolutionS;
    }

    public String getOutputFilePrefix() {
        return outputFilePrefix;
    }

    @XmlElement(name = "out_file_prefix", required = true)
    public void setOutputFilePrefix(String outputFilePrefix) {
        this.outputFilePrefix = outputFilePrefix;
    }

    public Integer getSpectralBandIndex() {
        return spectralBandIndex;
    }

    @XmlElement(name = "spectral_band_index", required = false)
    public void setSpectralBandIndex(Integer index) {
        this.spectralBandIndex = index;
    }

    public Double getG() {
        return g;
    }

    @XmlElement(name = "g", required = false)
    public void setG(Double g) {
        this.g = g;
    }

    public Double getAbsorptionFactor() {
        return absorptionFactor;
    }

    @XmlElement(name = "absorption_factor", required = false)
    public void setAbsorptionFactor(Double absorptionFactor) {
        this.absorptionFactor = absorptionFactor;
    }

    public Double getScatteringFactor() {
        return scatteringFactor;
    }

    @XmlElement(name = "scattering_factor", required = false)
    public void setScatteringFactor(Double scatteringFactor) {
        this.scatteringFactor = scatteringFactor;
    }

    public Double getIncidentRadius() {
        return incidentRadius;
    }

    @XmlElement(name = "radius_incident", required = true)
    public void setIncidentRadius(Double incidentRadius) {
        this.incidentRadius = incidentRadius;
    }

    public Double getDropletRadius() {
        return dropletRadius;
    }

    @XmlElement(name = "radius_droplet", required = false)
    public void setDropletRadius(Double dropletRadius) {
        this.dropletRadius = dropletRadius;
    }

    public Integer getNumSca() {
        return numSca;
    }

    @XmlElement(name = "num_sca", required = true)
    public void setNumSca(Integer numSca) {
        this.numSca = numSca;
    }

    public Double getSigmaH() {
        return sigmaH;
    }

    @XmlElement(name = "sigma_h", required = true)
    public void setSigmaH(Double sigmaH) {
        this.sigmaH = sigmaH;
    }

    public Double getSigmaV() {
        return sigmaV;
    }

    @XmlElement(name = "sigma_v", required = true)
    public void setSigmaV(Double sigmaV) {
        this.sigmaV = sigmaV;
    }

    public Double getSigmaS() {
        return sigmaS;
    }

    @XmlElement(name = "sigma_s", required = false)
    public void setSigmaS(Double sigmaS) {
        this.sigmaS = sigmaS;
    }

    public Double getLambda() {
        return lambda;
    }

    @XmlElement(name = "lambda", required = false)
    public void setLambda(Double lambda) {
        this.lambda = lambda;
    }

    public Double getNumIceParticles() {
        return numIceParticles;
    }

    @XmlElement(name = "num_ice", required = true)
    public void setNumIceParticles(Double numIceParticles) {
        this.numIceParticles = numIceParticles;
    }

    public Double getDistance() {
        return distance;
    }

    @XmlElement(name = "distance", required = true)
    public void setDistance(Double distance) {
        this.distance = distance;
    }

    // Simulation Concern
    /**
     * Number of photons used in each step of the simulation
     */
    private Integer numPhotons = null;

    /**
     * Number of bins for incident direction phi (only for multiple direction simulation)
     */
    private Integer binsPhi = null;

    /**
     * Number of bins for incident direction theta (only for multiple direction simulation)
     */
    private Integer binsTheta = null;

    /**
     * Incident angle phi (only for single direction simulation)
     */
    private Double phi = null;

    /**
     * Incident angle theta (only for single direction simulation)
     */
    private Double theta = null;

    /**
     * Spatial resolution the simulation
     */
    private Integer resolutionS = null;

    /**
     * Prefix of the output file
     */
    private String outputFilePrefix = null;

    // Modeling concern

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
     * The length of the contrail section
     */
    private Double distance = null;

    /**
     * Copy constructor for use with {@link Cloneable} interface.
     *
     * @param obj The object to copy parameters from
     */
    protected XMLParametersLegacy(XMLParametersLegacy obj) {
        this.numPhotons = obj.numPhotons;
        this.binsPhi = obj.binsPhi;
        this.binsTheta = obj.binsTheta;
        this.phi = obj.phi;
        this.theta = obj.theta;
        this.resolutionS = obj.resolutionS;
        this.outputFilePrefix = obj.outputFilePrefix;
        this.g = obj.g;
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
        this.distance = obj.distance;
    }

    protected XMLParametersLegacy() {}

    /**
     * Determines whether a given configuration is a solar configuration or a terrestrial configuration based on the
     * presence/absence of certain parameters.
     *
     * @return {@code True} if the configuration is a solar configuration, false otherwise
     */
    @XmlAttribute(name = "solar", required = false)
    public boolean isSolar() {
        if (this.getSigmaS() == null)
            return false;
        return true;
    }

    /**
     * Writes the object into an XML file in the given directory.
     *
     * @param dir  The directory to write the XML file into
     * @param name The name of the XML file
     * @throws JAXBException
     * @throws IOException
     */
    public void marshal(String dir, String name) throws JAXBException, IOException {
        JAXBContext context = JAXBContext.newInstance(XMLParametersLegacy.class);
        Marshaller mar = context.createMarshaller();
        mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        mar.marshal(this, new File(dir, name));
    }

    /**
     * Writes the object into an XML file in the {@code config} folder.
     *
     * @param name The name of the config file
     * @throws JAXBException For errors related to parsing and marshalling the XML file
     * @throws IOException   For errors related to file I/O
     */
    public void marshal(String name) throws JAXBException, IOException {
        this.marshal(PropertiesManager.getInstance().getDirConfig().toString(), name);
    }

    /**
     * Reads a configuration in XML format from a file and creates an {@link XMLParametersLegacy} object from it.
     *
     * @param dir  The directory the XML file resides in
     * @param name The name of the XML file to read
     * @return The {@link XMLParametersLegacy} object with the read parameter values or {@code null} if an error occurred
     */
    public static XMLParametersLegacy unmarshal(String dir, String name) {
        return unmarshal(new File(dir, name));
    }

    /**
     * Reads a configuration in XML format from a file and creates an {@link XMLParametersLegacy} object from it.
     *
     * @param file The {@link File} to read in
     * @return The {@link XMLParametersLegacy} object with the read parameter values or {@code null} if an error occurred
     */
    public static XMLParametersLegacy unmarshal(File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(XMLParametersLegacy.class);
            return (XMLParametersLegacy) context.createUnmarshaller().unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Reads a configuration in XML format from a file in the config directory and creates an {@link XMLParametersLegacy}
     * object from it.
     *
     * @param name The name of the XML file to read
     * @return the {@link XMLParametersLegacy} object with the read parameter values
     */
    public static XMLParametersLegacy unmarshal(String name) {
        return XMLParametersLegacy.unmarshal(PropertiesManager.getInstance().getDirConfig().toString(), name);
    }

    @Override
    public XMLParametersLegacy clone() {
        return new XMLParametersLegacy(this);
    }

    public String toCommentString() {
        boolean solar = isSolar();

        String s = "";
        if (solar)
            s += "// Solar Simulation - Parameters:\n";
        else
            s+= "// Terrestrial Simulation - Parameters:\n";

        s += "// num_photons = " + this.numPhotons + "\n";
        s += "// bins_phi = " + this.binsPhi + "\n";
        s += "// bins_theta = " + this.binsTheta + "\n";
        s += "// resolution_s = " + this.resolutionS + "\n";

        if (solar) {
            s += "// phi = " + this.phi + "\n";
            s += "// theta = " + this.theta + "\n";
        }

        // Not always supplied, can be in place of g, absorption_factor and scattering_factor
        if (this.spectralBandIndex != null)
            s += "// spectral_band_index = " + this.spectralBandIndex + "\n";

        // Some values are calculated, only print if they have been initialized
        if (this.g != null) {
            s += "// g = " + this.g + "\n";
        }
        if (this.absorptionFactor != null) {
            s += "// absorption_factor = " + this.absorptionFactor + "\n";
        }
        if (this.scatteringFactor != null) {
            s += "// scattering_factor = " + this.scatteringFactor + "\n";
        }

        // out_file_prefix not included for now as it breaks SolarSimulationTest
        //s += "// output_file_prefix = " + this.outputFilePrefix + "\n";

        s += "// incident_radius = " + this.incidentRadius + "\n";
        s += "// droplet_radius = " + this.dropletRadius + "\n";
        s += "// num_sca = " + this.numSca + "\n";
        s += "// sigma_h = " + this.sigmaH + "\n";
        s += "// sigma_v = " + this.sigmaV + "\n";

        if (solar)
            s += "// sigma_s = " + this.sigmaS + "\n";
        else
            s += "// lambda = " + this.lambda + "\n";

        s += "// numIceParticles = " + this.numIceParticles + "\n";
        s += "// distance = " + this.distance + "\n";

        return s;
    }
}
