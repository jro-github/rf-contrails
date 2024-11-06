package de.tudresden.aerospace.contrails;

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

import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * This test suite runs all tests in the {@link de.tudresden.aerospace.contrails.Modeling} and {@link de.tudresden.aerospace.contrails.MonteCarlo} packages with the exception of tests,
 * that require the simulation to run. Simulation tests in this class are only conducted on existing output files,
 * without re-running the simulation itself. This is intended for quickly testing changes that do not require the
 * simulation to run. To perform a full test, including simulation runs instead, use {@link RFContrailsTestSuiteFull}.
 */
@Suite
@SelectPackages({"Modeling", "MonteCarlo"})
@ExcludeTags({"deprecated", "run_simulation"})
public class RFContrailsTestSuiteCached {

}
