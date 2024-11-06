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

/**
 * Represents a scattered photon by direction and the number of scattering events that occurred.
 *
 * @author Thomas Rosenow
 */
public class RePhoton {
   /**
    * The zenith angle (direction w.r.t. z-axis) of the scattered photon
    */
   public double theta;

   /**
    * The azimuth angle (direction w.r.t. x-axis) of the scattered photon
    */
   public double phi;

   /**
    * The number of scattering events that occurred
    */
   public double Scat_events;

   /**
    * Creates a scattered photon object.
    *
    * @param theta Angle to z-coordinate axis (upwards vector). Range: [0, PI]
    * @param phi Angle to x-coordinate axis (direction of flight). Range: [0, 2*PI]
    * @param scat_events The number of scattering events that occurred
    */
   public RePhoton(double theta, double phi, int scat_events) {
      this.theta = theta;
      this.phi = phi;
      this.Scat_events = (double)scat_events;
   }
}
