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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Singleton that manages {@link Properties} file(s). Loads properties from {@code rf-contrails.properties} and provides
 * simple access to them with methods.
 */
public class PropertiesManager {
    private static PropertiesManager INSTANCE;
    private Path rootPath;
    private Properties properties;

    /**
     * Initializes this Singleton, loading properties from {@code rf-contrails.properties} in the project root
     */
    private PropertiesManager() {
        rootPath = Paths.get(System.getProperty("user.dir"));
        properties = new Properties();

        try {
            loadProperties(getProjectName() + ".properties");
        } catch (IOException e) {
            System.err.println("Could not read properties file " + getProjectName() + ".properties" + " in" + rootPath);
            System.out.println("Attempting to load default properties");
            loadDefaultProperties();
        }

        System.out.println("Successfully loaded properties ");
    }

    /**
     * Getter for the Singleton instance. Creates the Singleton or, if it exists already, returns the existing instance.
     *
     * @return The singleton instance
     */
    public static PropertiesManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PropertiesManager();
        }

        return INSTANCE;
    }

    /**
     * Determines whether the Singleton instance is created or not.
     *
     * @return {@code true} if an instance exists, {@code false} otherwise
     */
    public boolean isInitialized() {
        return INSTANCE != null;
    }

    /**
     * Gets the project's root path.
     *
     * @return The {@link Path} object containing the root path
     */
    public Path getRootPath() {
        return rootPath;
    }

    /**
     * Gets the project name from the final directory name of the project root path.
     *
     * @return The project name
     */
    public String getProjectName() {
        return rootPath.getFileName().toString();
    }

    /**
     * Gets a property from the loaded properties file by key.
     *
     * @param key The key of the property
     * @return The property value or {@code null} if the key was not found
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Gets the parameters directory property as a {@link File}.
     *
     * @return The {@link File} with the directory
     */
    public File getDirParameters() {
        return getDir("dir_parameters");
    }

    /**
     * Gets the config directory property as a {@link File}.
     *
     * @return The {@link File} with the directory
     */
    public File getDirConfig() {
        return getDir("dir_config");
    }

    /**
     * Gets the output directory property as a {@link File}.
     *
     * @return The {@link File} with the directory
     */
    public File getDirOutput() {
        return getDir("dir_out");
    }

    /**
     * Gets the libradtran output directory property as a {@link File}.
     *
     * @return The {@link File} with the directory
     */
    public File getDirLibradtranOut() {
        return getDir("dir_libradtran_out");
    }

    /**
     * Gets the metrics directory property as a {@link File}.
     *
     * @return The {@link File} with the directory
     */
    public File getDirMetrics() {
        return getDir("dir_metrics");
    }

    /**
     * Gets the resources directory property as a {@link File}.
     *
     * @return The {@link File} with the directory
     */
    public File getDirResources() {
        return getDir("dir_resources");
    }

    /**
     * Gets the test resources directory property as a {@link File}.
     *
     * @return The {@link File} with the directory
     */
    public File getDirResourcesTest() {
        return getDir("dir_resources_test");
    }

    /**
     * Helper for directory property getters.
     *
     * @param key The key of the directory to get
     * @return The {@link File} with the directory
     */
    private File getDir(String key) {
        return new File(rootPath.toString(), properties.getProperty(key));
    }

    /**
     * Loads {@link Properties} from a file in the project root folder.
     *
     * @param fileName The name of the properties file
     * @throws IOException If an error occurred reading the file
     */
    private void loadProperties(String fileName) throws IOException {
        properties.load(new FileInputStream(new File(rootPath.toString(), fileName)));
    }

    /**
     * Attempts to load default properties from {@code default.properties} in the project root folder.
     */
    public void loadDefaultProperties() {
        try {
            loadProperties("default.properties");
        } catch (IOException e) {
            System.err.println("Error reading default.properties");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
