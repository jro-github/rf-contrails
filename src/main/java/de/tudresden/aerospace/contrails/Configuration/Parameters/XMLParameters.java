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

import de.tudresden.aerospace.contrails.Configuration.PropertiesManager;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@XmlRootElement(name = "parameters")
@XmlType(propOrder = {"common", "solarDiffuse", "solarDirect", "terrestrialDiffuse"})
public class XMLParameters  implements Cloneable {
    /**
     * Common parameters used for both terrestrial and solar simulation parts
     */
    private CommonParameters common;

    /**
     * Parameters used for solar diffuse part
     */
    private DiffuseParameters solarDiffuse;

    /**
     * Parameters used for solar direct part
     */
    private DirectParameters solarDirect;

    /**
     * Parameters used for terrestrial diffuse part
     */
    private DiffuseParameters terrestrialDiffuse;

    /**
     * Gets the common parameters.
     *
     * @return The {@link CommonParameters} object
     */
    public CommonParameters getCommon() {
        return common;
    }

    /**
     * Sets the common parameters to the given parameters.
     *
     * @param common The new {@link CommonParameters} object to store
     */
    @XmlElement(name = ParameterNames.COMMON, required = true)
    public void setCommon(CommonParameters common) {
        this.common = common;
    }

    /**
     * Gets the solar diffuse parameters.
     *
     * @return The {@link DiffuseParameters} object for the solar part
     */
    public DiffuseParameters getSolarDiffuse() {
        return solarDiffuse;
    }

    /**
     * Sets the parameters for the solar diffuse part to the given parameters.
     *
     * @param solarDiffuse The new {@link DiffuseParameters} object to store
     */
    @XmlElement(name = ParameterNames.SOLAR_DIFFUSE, required = true)
    public void setSolarDiffuse(DiffuseParameters solarDiffuse) {
        this.solarDiffuse = solarDiffuse;
    }

    /**
     * Gets the solar direct parameters.
     *
     * @return The {@link DirectParameters} object for the solar part
     */
    public DirectParameters getSolarDirect() {
        return solarDirect;
    }

    /**
     * Sets the parameters for the solar direct part to the given parameters.
     *
     * @param solarDirect The new {@link DirectParameters} object to store
     */
    @XmlElement(name = ParameterNames.SOLAR_DIRECT, required = true)
    public void setSolarDirect(DirectParameters solarDirect) {
        this.solarDirect = solarDirect;
    }

    /**
     * Gets the terrestrial diffuse parameters.
     *
     * @return The {@link DiffuseParameters} object for the terrestrial part
     */
    public DiffuseParameters getTerrestrialDiffuse() {
        return terrestrialDiffuse;
    }

    /**
     * Sets the parameters for the terrestrial diffuse part to the given parameters.
     *
     * @param terrestrialDiffuse The new {@link DiffuseParameters} object to store
     */
    @XmlElement(name = ParameterNames.TERRESTRIAL_DIFFUSE, required = true)
    public void setTerrestrialDiffuse(DiffuseParameters terrestrialDiffuse) {
        this.terrestrialDiffuse = terrestrialDiffuse;
    }

    /**
     * Copy constructor for use with {@link Cloneable} interface. Internally creates a shallow copy.
     *
     * @param obj The object to copy parameters from
     */
    protected XMLParameters(XMLParameters obj) {
        this.common = obj.common;
        this.solarDiffuse = obj.solarDiffuse;
        this.solarDirect = obj.solarDirect;
        this.terrestrialDiffuse = obj.terrestrialDiffuse;
    }

    /**
     * No-parameters constructor to allow client to initialize the object.
     */
    public XMLParameters() {}

    /**
     * Writes the object into an XML file in the given directory.
     *
     * @param dir  The directory to write the XML file into
     * @param name The name of the XML file
     * @throws JAXBException If the file could not be marshalled into an {@link XMLParameters} object
     * @throws IOException If the file could not be read
     */
    public void marshal(String dir, String name) throws JAXBException, IOException {
        JAXBContext context = JAXBContext.newInstance(XMLParameters.class);
        Marshaller mar = context.createMarshaller();
        mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        mar.marshal(this, new File(dir, name));
    }

    /**
     * Writes the object into an XML file in the {@code config} folder.
     *
     * @param name The name of the config file
     * @throws JAXBException If the file could not be marshalled into an {@link XMLParameters} object
     * @throws IOException If the file could not be read
     */
    public void marshal(String name) throws JAXBException, IOException {
        this.marshal(PropertiesManager.getInstance().getDirConfig().toString(), name);
    }

    /**
     * Reads a configuration file in XML format from a file and creates an {@link XMLParameters} object from it.
     *
     * @param dir  The directory the XML file resides in
     * @param name The name of the XML file to read
     * @return The {@link XMLParameters} object with the read parameter values or {@code null} if an error occurred
     */
    public static XMLParameters unmarshal(String dir, String name) {
        return unmarshal(new File(dir, name));
    }

    /**
     * Reads a configuration in XML format from a file and creates an {@link XMLParameters} object from it.
     *
     * @param file The {@link File} to read in
     * @return The {@link XMLParameters} object with the read parameter values or {@code null} if an error occurred
     */
    public static XMLParameters unmarshal(File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(XMLParameters.class);
            return (XMLParameters) context.createUnmarshaller().unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Reads a configuration file in XML format from a file in the config directory and creates a {@link XMLParameters}
     * object from it.
     *
     * @param name The name of the XML file to read
     * @return the {@link XMLParameters} object with the read parameter values
     */
    public static XMLParameters unmarshal(String name) {
        return XMLParameters.unmarshal(PropertiesManager.getInstance().getDirConfig().toString(), name);
    }

    /**
     * Performs a shallow copy of the current object. The internal parameter objects are only linked.
     *
     * @return The copied {@link XMLParameters} object
     */
    @Override
    public XMLParameters clone() {
        return new XMLParameters(this);
    }

    public void check() {
        if (common == null) {
            System.err.println("Failed to read common parameters");
            System.exit(1);
        }

        if (solarDiffuse == null) {
            System.err.println("Failed to read solar diffuse parameters");
            System.exit(1);
        }

        if (solarDirect == null) {
            System.err.println("Failed to read solar direct parameters");
            System.exit(1);
        }

        if (terrestrialDiffuse == null) {
            System.err.println("Failed to read terrestrial diffuse parameters");
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

        sb.append(common.toCommentString());
        sb.append(solarDiffuse.toCommentString());
        sb.append(solarDirect.toCommentString());
        sb.append(terrestrialDiffuse.toCommentString());

        return sb.toString();
    }
}
