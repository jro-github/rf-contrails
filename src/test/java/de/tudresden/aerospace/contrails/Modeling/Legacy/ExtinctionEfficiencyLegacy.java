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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;


/**
 * Groups functionality to compute the extinction efficiency of ice crystals, depending on
 * their size. The methods are annotated by the type of radiation and crystals for which they compute the values.
 * For the solar spectrum and individual ice crystals use Calc_*_sol_i(..) methods, for mixtures use
 * Calc_*_sol(..) methods. For the terrestrial spectrum use Calc_*_terr(..) methods.
 * @see <a href="https://doi.org/10.1029/1999JD900755">Parameterization of the scattering
 * and absorption properties of individual ice crystals (Paper)</a> for more details on solar coefficients.
 * @see <a href="https://doi.org/10.1364/AO.44.005512">Scattering and absorption property database for
 * nonspherical ice particles in the near- through far-infrared spectral region (Paper)</a> for more details
 * on terrestrial coefficients.
 */
public class ExtinctionEfficiencyLegacy
{
 private String fn_output;

 // ====== Arrays for wavelength and shape lookup ======
 /**
  * Wavelength of the selected spectral band<br/>
  * 0 = 0.55 micrometer<br/>
  * 1 = 1.35 micrometer<br/>
  * 2 = 2.25 micrometer<br/>
  * 3 = 2.75 micrometer<br/>
  * 4 = 3.0125 micrometer<br/>
  * 5 = 3.775 micrometer<br/>
  * 6 = 4.5 micrometer
  */
 private double[] bands;

 /**
  * First dimension: spectral band index<br/>
  * Second dimension: value index<br/>
  * 0 = lambda<br/>
  * 1 = lambda1<br/>
  * 2 = lambda2<br/>
  * 3 = mr<br/>
  * 4 = mi<br/>
  * 5 = dS/S<br/>
  */
 private double[][] table1;

 /**
  * First dimension: spectral band index [0, 6]<br/>
  * Second dimension: shape index<br/>
  * 0 = plate<br/>
  * 1 = column<br/>
  * 2 = hollow column<br/>
  * 3 = bullet rosettes-4<br/>
  * 4 = bullet rosettes-6<br/>
  * 5 = aggregates<br/>
  * Third dimension: value index<br/>
  * 0 = lambda<br/>
  * 1 = eta1_ext<br/>
  * 2 = eta2_ext<br/>
  * 3 = beta1<br/>
  * 4 = beta2<br/>
  * 5 = alpha<br/>
  * 6 = xi_ext<br/>
  */
 private double[][][] table3;

 /**
  * First dimension: spectral band index [0, 6]<br/>
  * Second dimension: shape index [0, 5]<br/>
  * Third dimension: value index<br/>
  * 0 = lambda<br/>
  * 1 = eta1_abs<br/>
  * 2 = eta2_abs<br/>
  * 3 = xi1_abs<br/>
  * 4 = xi2_abs<br/>
  */
 private double[][][] table4;

 /**
  * First dimension: spectral band index [0, 6]<br/>
  * Second dimension: shape index [0, 5]<br/>
  * Third dimension: value index<br/>
  * 0 = lambda<br/>
  * 1 = xi1_g<br/>
  * 2 = xi2_g<br/>
  * 3 = xi3_g<br/>
  * 4 = xi4_g<br/>
  * 5 = xi5_g<br/>
  * 6 = xi6_g<br/>
  * 7 = xi7_g
  */
 private double[][][] table5;

 /**
  * For calculations in the terrestrial wavelength interval
  * First dimension: spectral band index [0, 6]<br/>
  * Second dimension: value index<br/>
  * 1 = eta1<br/>
  * 2 = eta2<br/>
  * 3 = eta3<br/>
  * 4 = xi0<br/>
  * 5 = xi1<br/>
  * 6 = xi2<br/>
  * 7 = xi3<br/>
  * 8 = zeta0<br/>
  * 9 = zeta1<br/>
  * 10 = zeta2<br/>
  * 11 = zeta3
  */
 private double[][] table_terr1;

 /**
  * Reads in values and initializes tables.
  */
 public ExtinctionEfficiencyLegacy(){
  bands = new double[] {0.55, 1.35, 2.25, 2.75, 3.0125, 3.775, 4.5}; //[micrometer]


  // Read table 1
  // Index 1: selected spectral band [0...6]
  // Index 2: 0 = lambda
  //          1 = lambda1
  //          2 = lambda2
  //          3 = mr
  //          4 = mi
  //          5 = dS/S
  BufferedReader file= null;
  try {
   file=new BufferedReader(new FileReader("Parameter/Yang2000_OpticalConstants.txt"));
   System.out.println("Datei Parameter/Yang2000_OpticalConstants.txt gefunden" );
  } catch(IOException e) {System.err.println("Datei Parameter/Yang2000_OpticalConstants.txt nicht gefunden" );};

  //Datei ist geoeffnet und kann ueber file gelesen werden
  // Zerlegen der Eingangsdaten
  String eineZeile; // zum Zeilenweise einlesen der Datei
  Pattern p = Pattern.compile( "\\s" ); // wird zum Teilen der Zeilen verwendet
  ArrayList<String> list= new ArrayList<String>(); // wird mit den Messwerten gefuellt
  try {
   // ignore header (1 line)
   eineZeile= file.readLine();
   while (( eineZeile= file.readLine())!=null){ // Solange noch eine Zeile gelesen werden kann
    list.add(eineZeile);
   }
   file.close(); // schliessen der Datei
  } catch (IOException ex){System.err.println("Fehler beim Einlesen (Table1)");} // Falls ein Fehler auftrat

  // lege Daten in Array table1 ab
  table1= new double[7][6]; // Initialisieren des Arrays mit der richtigen Groesse
  for (int i=0; i<7; i++) {
   try {
    String InfoString[] = p.split( list.get(i) ); // Teile die Zeile in ein Array aus Stringvariablen
    for (int j = 0; j<6; j++){
     table1[i][j]=Double.valueOf(InfoString[j]);
    };
   } catch(IndexOutOfBoundsException e){
    System.err.println("Fehler beim Ablegen der Daten in Table1,");
    System.err.println("wahrscheinlich ist ein Formatierungsfehler in Parameter/Yang2000_OpticalConstants.txt" );
   };
  };


  // ---== Read Table 3 ==---
  // Index 1: das gewaehlte spektrale Band [0...6]
  // Index 2: shape 0 = Plate
  //           1 = Column
  //           2 = Hollow Column
  //           3 = Bullet rosettes-4
  //           4 = Bullet rosettes-6
  //           5 = Aggregates
  // Index 3: 0 = lambda
  //          1 = eta1_ext
  //          2 = eta2_ext
  //          3 = beta1
  //          4 = beta2
  //          5 = alpha
  //          6 = xi_ext
  file= null;
  try {
   file=new BufferedReader(new FileReader("Parameter/Yang2000_Extinction.txt"));
   System.out.println("Datei Parameter/Yang2000_Extinction.txt gefunden" );
  } catch(IOException e) {System.err.println("Datei Parameter/Yang2000_Extinction.txt nicht gefunden" );};

  //Datei ist geoeffnet und kann ueber file gelesen werden
  // Zerlegen der Eingangsdaten
  p = Pattern.compile( "\\s" ); // wird zum Teilen der Zeilen verwendet
  list= new ArrayList<String>(); // wird mit den Messwerten gefuellt
  try {
   // ignore header (2 lines)
   eineZeile= file.readLine();
   eineZeile= file.readLine();
   int i = 1;
   while (( eineZeile= file.readLine())!=null){ // Solange noch eine Zeile gelesen werden kann
    if ((i%8)!=0){
     list.add(eineZeile);
    }
    i++;
   }
   file.close(); // schliessen der Datei
  } catch (IOException ex){System.err.println("Fehler beim Einlesen (Table 3)");} // Falls ein Fehler auftrat

  // lege Daten in Array table3 ab
  table3= new double[7][6][7]; // Initialisieren des Arrays mit der richtigen Groesse
  for (int i=0; i<7; i++) { // das spektrale Band
   for (int j=0; j<6; j++) { // shape
    try {
     String InfoString[] = p.split( list.get(i+7*j) ); // Teile die Zeile in ein Array aus Stringvariablen
     for (int k = 0; k<7; k++) { // Parameter
      table3[i][j][k]=Double.valueOf(InfoString[k]);
     };
    } catch(IndexOutOfBoundsException e) {
     System.err.println("Fehler beim Ablegen der Daten in Table3,");
     System.err.println("wahrscheinlich ist ein Formatierungsfehler in Parameter/Yang2000_Extinction.txt" );
    };
   };
  };


  // ---== Read Table 4 ==---
  // Index 1: das gewaehlte spektrale Band [0...6]
  // Index 2: shape 0 = Plate
  //           1 = Column
  //           2 = Hollow Column
  //           3 = Bullet rosettes-4
  //           4 = Bullet rosettes-6
  //           5 = Aggregates
  // Index 3: 0 = lambda
  //          1 = eta1_abs
  //          2 = eta2_abs
  //          3 = xi1_abs
  //          4 = xi2_abs
  file= null;
  try {
   file=new BufferedReader(new FileReader("Parameter/Yang2000_Absorption.txt"));
   System.out.println("Datei Parameter/Yang2000_Absorption.txt gefunden" );
  } catch(IOException e) {System.err.println("Datei Parameter/Yang2000_Absorption.txt nicht gefunden" );};

  //Datei ist geoeffnet und kann ueber file gelesen werden
  // Zerlegen der Eingangsdaten
  p = Pattern.compile( "\\s" ); // wird zum Teilen der Zeilen verwendet
  list= new ArrayList<String>(); // wird mit den Messwerten gefuellt
  try {
   // ignore header (2 lines)
   eineZeile= file.readLine();
   eineZeile= file.readLine();
   int i = 1;
   while (( eineZeile= file.readLine())!=null){ // Solange noch eine Zeile gelesen werden kann
    if ((i%8)!=0){
     list.add(eineZeile);
    }
    i++;
   }
   file.close(); // schliessen der Datei
  } catch (IOException ex){System.err.println("Fehler beim Einlesen (Table 4)");} // Falls ein Fehler auftrat

  // lege Daten in Array table4 ab
  table4= new double[7][6][5]; // Initialisieren des Arrays mit der richtigen Groesse
  for (int i=0; i<7; i++) { // das spektrale Band
   for (int j=0; j<6; j++) { // shape
    try {
     String InfoString[] = p.split( list.get(i+7*j) ); // Teile die Zeile in ein Array aus Stringvariablen
     for (int k = 0; k<5; k++) { // Parameter
      table4[i][j][k]=Double.valueOf(InfoString[k]);
     };
    } catch(IndexOutOfBoundsException e) {
     System.err.println("Fehler beim Ablegen der Daten in Table4,");
     System.err.println("wahrscheinlich ist ein Formatierungsfehler in Parameter/Yang2000_Absorption.txt" );
    };
   };
  };


  // ---== Read Table 5 ==---
  // Index 1: das gewaehlte spektrale Band [0...6]
  // Index 2: shape 0 = Plate
  //           1 = Column
  //           2 = Hollow Column
  //           3 = Bullet rosettes-4
  //           4 = Bullet rosettes-6
  //           5 = Aggregates
  // Index 3: 0 = lambda
  //          1 = xi1_g
  //          2 = xi2_g
  //          3 = xi3_g
  //          4 = xi4_g
  //          5 = xi5_g
  //          6 = xi6_g
  //          7 = xi7_g
  file= null;
  try {
   file=new BufferedReader(new FileReader("Parameter/Yang2000_Asymmetry.txt"));
   System.out.println("Datei Parameter/Yang2000_Asymmetry.txt gefunden" );
  } catch(IOException e) {System.err.println("Datei Parameter/Yang2000_Asymmetry.txt nicht gefunden" );};

  //Datei ist geoeffnet und kann ueber file gelesen werden
  // Zerlegen der Eingangsdaten
  p = Pattern.compile( "\\s" ); // wird zum Teilen der Zeilen verwendet
  list= new ArrayList<String>(); // wird mit den Messwerten gefuellt
  try {
   // ignore header (2 lines)
   eineZeile= file.readLine();
   eineZeile= file.readLine();
   int i = 1;
   while (( eineZeile= file.readLine())!=null){ // Solange noch eine Zeile gelesen werden kann
    if ((i%8)!=0){
     list.add(eineZeile);
    }
    i++;
   }
   file.close(); // schliessen der Datei
  } catch (IOException ex){System.err.println("Fehler beim Einlesen (Table 5)");} // Falls ein Fehler auftrat

  // lege Daten in Array table5 ab
  table5= new double[7][6][8]; // Initialisieren des Arrays mit der richtigen Groesse
  for (int i=0; i<7; i++) { // das spektrale Band
   for (int j=0; j<6; j++) { // shape
    try {
     String InfoString[] = p.split( list.get(i+7*j) ); // Teile die Zeile in ein Array aus Stringvariablen
     for (int k = 0; k<8; k++) { // Parameter
      table5[i][j][k]=Double.valueOf(InfoString[k]);
     };
    } catch(IndexOutOfBoundsException e) {
     System.err.println("Fehler beim Ablegen der Daten in Table5,");
     System.err.println("wahrscheinlich ist ein Formatierungsfehler in Parameter/Yang2000_Asymmetry.txt" );
    };
   };
  };


  // ---== Read table_terr1 ==---
  // Fuer die Berechnung im terrestrischen Wellenlaengenbereich
  // Index 1: das gewaehlte spektrale Band [0...6]
  // Index 2: 0 = lambda
  //          1 = eta1
  //          2 = eta2
  //          3 = eta3
  //          4 = xi0
  //          5 = xi1
  //          6 = xi2
  //          7 = xi3
  //          8 = zeta0
  //          9 = zeta1
  //         10 = zeta2
  //         11 = zeta3
  file= null;
  try {
   file=new BufferedReader(new FileReader("Parameter/Yang2005_terrestrSpektrum.txt"));
   System.out.println("Datei Parameter/Yang2005_terrestrSpektrum.txt gefunden" );
  } catch(IOException e) {System.err.println("Datei Parameter/Yang2005_terrestrSpektrum.txt nicht gefunden" );};

  //Datei ist geoeffnet und kann ueber file gelesen werden
  // Zerlegen der Eingangsdaten
  p = Pattern.compile( "\\s" ); // wird zum Teilen der Zeilen verwendet
  list= new ArrayList<String>(); // wird mit den Messwerten gefuellt
  try {
   // ignore header (1 line)
   eineZeile= file.readLine();
   while (( eineZeile= file.readLine())!=null){ // Solange noch eine Zeile gelesen werden kann
    list.add(eineZeile);
   }
   file.close(); // schliessen der Datei
  } catch (IOException ex){System.err.println("Fehler beim Einlesen (table_terr1)");} // Falls ein Fehler auftrat

  // lege Daten in Array table1 ab
  table_terr1= new double[49][12]; // Initialisieren des Arrays mit der richtigen Groesse
  for (int i=0; i<49; i++) {
   try {
    String InfoString[] = p.split( list.get(i) ); // Teile die Zeile in ein Array aus Stringvariablen
    for (int j = 0; j<12; j++){
     table_terr1[i][j]=Double.valueOf(InfoString[j]);
    };
   } catch(IndexOutOfBoundsException e){
    System.err.println("Fehler beim Ablegen der Daten in table_terr1,");
    System.err.println("wahrscheinlich ist ein Formatierungsfehler in Parameter/Yang2005_terrestrSpektrum.txt" );
   };
  };

  return;
 }

 // ====== Helper functions ======

 /**
  * Helper for calculating the spherical diameter with equivalent projected
  * area of an ice crystal from its maximum dimensions and shape.
  * @param Dmax Maximum dimension in micrometers. Range: [2, 10'000]
  * @param shape Shape index of the ice crystal. Range: [0, 5]
  */
 private double Calc_Da(double Dmax, int shape){
  // shape ... 0 = Plate
  //           1 = Column
  //           2 = Hollow Column
  //           3 = Bullet rosettes-4
  //           4 = Bullet rosettes-6
  //           5 = Aggregates
  double[][] a = {{0.43773, 0.33401, 0.33401, 0.15909, 0.14195, -0.47737},
          {0.75497, 0.36477, 0.36477, 0.84308, 0.84394, 0.10026e1},
          {0.19033e-1, 0.30855, 0.30855, 0.70161e-2, 0.72125e-2, -0.10030e-2},
          {0.35191e-3, -0.55631e-1, -0.55631e-1, -0.11003e-2, -0.11219e-2, 0.15166e-3},
          {-0.70782e-4, 0.30162e-2, 0.30162e-2, 0.45161e-4, 0.45819e-4, -0.78433e-5}};
  double Da = 0;
  double help = 0;
  for (int n = 0; n<5; n+=1) {
   help += a[n][shape] * Math.pow(Math.log(Dmax),n);
  };
  Da = Math.exp(help);
  return Da;
 }

 /**
  * Helper for calculating the spherical diameter with equivalent volume
  * of an ice crystal from its maximum dimensions and shape.
  * @param Dmax Maximum dimension in micrometers. Range: [2, 10'000]
  * @param shape Shape index of the ice crystal. Range: [0, 5]
  */
 private double Calc_Dv(double Dmax, int shape){
  // shape ... 0 = Plate
  //           1 = Column
  //           2 = Hollow Column
  //           3 = Bullet rosettes-4
  //           4 = Bullet rosettes-6
  //           5 = Aggregates
  double[][] b = {{0.31228, 0.30581, 0.24568, -0.97940e-1, -0.10318, -0.70160},
          {0.80874, 0.26252, 0.26202, 0.85683, 0.86290, 0.99215},
          {0.29287e-2, 0.35458, 0.35479, 0.29483e-2, 0.70665e-3, 0.29322e-2},
          {-0.44378e-3, -0.63202e-1, -0.63236e-1, -0.14341e-2, -0.11055e-2, -0.40492e-3},
          {0.23109e-4, 0.33755e-2, 0.33773e-2, 0.74627e-4, 0.57906e-4, 0.18841e-4}};
  double Dv = 0;
  double help = 0;
  for (int n = 0; n<5; n+=1) {
   help += b[n][shape] * Math.pow(Math.log(Dmax),n);
  };
  Dv = Math.exp(help);
  return Dv;
 }

 /**
  * Helper for calculating the effective diameter of an ice crystal from its
  * maximum dimensions and shape.
  * @param Dmax Maximum dimension in micrometers. Range: [2, 10'000]
  * @param shape Shape index of the ice crystal. Range: [0, 5]
  */
 private double Calc_de(double Dmax, int shape){
  // shape ... 0 = Plate
  //           1 = Column
  //           2 = Hollow Column
  //           3 = Bullet rosettes-4
  //           4 = Bullet rosettes-6
  //           5 = Aggregates
  double de = Math.pow(Calc_Dv(Dmax, shape),3)/Math.pow(Calc_Da(Dmax, shape),2);
  return de;
 }

 /**
  * Helper for calculating the effective diameter of an ice crystal from its
  * maximum dimensions.
  * @param Dmax Maximum dimension in micrometers. Range: [2, 10'000]
  */
 private double Calc_de_mix(double Dmax){
  // Dmax ... maximum dimension [micrometer]
  double de = 0;
  if (Dmax<70){
   // 50% Bullet rosettes-6
   // 25% hollow columns
   // 25% plates
   de = (0.5*Math.pow(Calc_Dv(Dmax, 4),3)+0.25*Math.pow(Calc_Dv(Dmax, 2),3)+0.25*Math.pow(Calc_Dv(Dmax, 0),3))/
           (0.5*Math.pow(Calc_Da(Dmax, 4),2)+0.25*Math.pow(Calc_Da(Dmax, 2),2)+0.25*Math.pow(Calc_Da(Dmax, 0),2));
  } else {
   // 30% Aggregates
   // 30% Bullet rosettes-6
   // 20% hollow columns
   // 20% plates
   de = (0.3*Math.pow(Calc_Dv(Dmax, 4),3)+0.3*Math.pow(Calc_Dv(Dmax, 4),3)+0.2*Math.pow(Calc_Dv(Dmax, 2),3)+0.2*Math.pow(Calc_Dv(Dmax, 0),3))/
           (0.3*Math.pow(Calc_Da(Dmax, 4),2)+0.3*Math.pow(Calc_Da(Dmax, 4),2)+0.2*Math.pow(Calc_Da(Dmax, 2),2)+0.2*Math.pow(Calc_Da(Dmax, 0),2));
  };
  return de;
 }

 public double getLambda_sol(int band) {
  return table1[band][0];
 }

 public double getLambda_terr(int band) {
  return table_terr1[band][0];
 }

 /**
  * Helper for calculating solar extinction efficiency of a sphere.
  * @param Dmax Maximum dimension in micrometers. Range: [2, 10'000]
  * @param band Spectral band index. Range: [0, 5]
  * @param shape Shape index of the ice crystal. Range: [0, 5]
  * @see <a href="https://doi.org/10.1029/1999JD900755">Parameterization of the scattering
  * and absorption properties of individual ice crystals (Paper)</a> for more details on solar coefficients.
  */
 private double Calc_Qextprime(double Dmax, int band, int shape){
  // band ... das gewuenschte spektrale Band
  //   0 = 0.55 micrometer
  //   1 = 1.35 micrometer
  //   2 = 2.25 micrometer
  //   3 = 3.0125 micrometer
  //   4 = 3.775 micrometer
  //   5 = 4.5 micrometer
  // shape ... 0 = Plate
  //           1 = Column
  //           2 = Hollow Column
  //           3 = Bullet rosettes-4
  //           4 = Bullet rosettes-6
  //           5 = Aggregates
  double lambda = bands[band]; // wavelength [micrometer]
  double mr = table1[band][3];
  double rhoe = 2*Math.PI*Calc_de(Dmax, shape)*Math.abs(mr-1)/lambda; //effective phase delay
  double eta1 = table3[band][shape][1];
  double rho = eta1 * rhoe;
  double beta = table3[band][shape][3];
  double Qextprime = 2. - 4.*Math.exp(-rho*Math.tan(beta)) *
          (Math.cos(beta)/rho*Math.sin(rho-beta) + Math.pow(Math.cos(beta)/rho,2) * Math.cos(rho-2*beta)) +
          4.*Math.pow(Math.cos(beta)/rho,2)*Math.cos(2*beta);
  return Qextprime;
 }

 /**
  * Helper for calculating solar extinction efficiency for an individual non-spherical ice
  * crystal without consideration of complex ray behavior.
  * @param Dmax Maximum dimension in micrometers. Range: [2, 10'000]
  * @param band Spectral band index. Range: [0, 5]
  * @param shape Shape index of the ice crystal. Range: [0, 5]
  * @see <a href="https://doi.org/10.1029/1999JD900755">Parameterization of the scattering
  * and absorption properties of individual ice crystals (Paper)</a> for more details on solar coefficients.
  */
 private double Calc_Qextprimeprime(double Dmax, int band, int shape){
  // band ... das gewuenschte spektrale Band
  //   0 = 0.55 micrometer
  //   1 = 1.35 micrometer
  //   2 = 2.25 micrometer
  //   3 = 3.0125 micrometer
  //   4 = 3.775 micrometer
  //   5 = 4.5 micrometer
  // shape ... 0 = Plate
  //           1 = Column
  //           2 = Hollow Column
  //           3 = Bullet rosettes-4
  //           4 = Bullet rosettes-6
  //           5 = Aggregates
  double lambda = bands[band]; // wavelength [micrometer]
  double mr = table1[band][3];
  double rhoe = 2*Math.PI*Calc_de(Dmax, shape)*Math.abs(mr-1)/lambda; //effective phase delay
  double eta2 = table3[band][shape][2];
  double beta2 = table3[band][shape][4];
  double alpha = table3[band][shape][5];
  double Qextprimeprime = 2*(1-Math.exp(-2./3.*rhoe*eta2*Math.tan(beta2))*Math.cos(2./3.*rhoe*eta2+alpha));
  return Qextprimeprime;
 }

 /**
  * Helper for calculating solar absorption efficiency of a sphere.
  * @param Dmax Maximum dimension in micrometers. Range: [2, 10'000]
  * @param band Spectral band index. Range: [0, 5]
  * @param shape Shape index of the ice crystal. Range: [0, 5]
  * @see <a href="https://doi.org/10.1029/1999JD900755">Parameterization of the scattering
  * and absorption properties of individual ice crystals (Paper)</a> for more details on solar coefficients.
  */
 private double Calc_Qabsprime(double Dmax, int band, int shape){
  // band ... das gewuenschte spektrale Band
  //   0 = 0.55 micrometer
  //   1 = 1.35 micrometer
  //   2 = 2.25 micrometer
  //   3 = 3.0125 micrometer
  //   4 = 3.775 micrometer
  //   5 = 4.5 micrometer
  // shape ... 0 = Plate
  //           1 = Column
  //           2 = Hollow Column
  //           3 = Bullet rosettes-4
  //           4 = Bullet rosettes-6
  //           5 = Aggregates
  double lambda = bands[band]; // wavelength [micrometer]
  double mi = table1[band][4];
  double chie = 2*Math.PI*Calc_de(Dmax, shape)/2/lambda; //effective size parameter of nonspherical ice crystal
  double gammae = 4*chie*mi;
  double eta1_abs = table4[band][shape][1];
  double Qabsprime = 1+2.*Math.exp(-1.*gammae*eta1_abs)/(gammae*eta1_abs)+2.*(Math.exp(-1*gammae*eta1_abs)-1)/Math.pow(gammae*eta1_abs,2);
  return Qabsprime;
 }

 /**
  * Helper for calculating solar absorption efficiency without consideration of complex ray behavior.
  * @param Dmax Maximum dimension in micrometers. Range: [2, 10'000]
  * @param band Spectral band index. Range: [0, 5]
  * @param shape Shape index of the ice crystal. Range: [0, 5]
  * @see <a href="https://doi.org/10.1029/1999JD900755">Parameterization of the scattering
  * and absorption properties of individual ice crystals (Paper)</a> for more details on solar coefficients.
  */
 private double Calc_Qabsprimeprime(double Dmax, int band, int shape){
  // band ... das gewuenschte spektrale Band
  //   0 = 0.55 micrometer
  //   1 = 1.35 micrometer
  //   2 = 2.25 micrometer
  //   3 = 3.0125 micrometer
  //   4 = 3.775 micrometer
  //   5 = 4.5 micrometer
  // shape ... 0 = Plate
  //           1 = Column
  //           2 = Hollow Column
  //           3 = Bullet rosettes-4
  //           4 = Bullet rosettes-6
  //           5 = Aggregates
  double lambda = bands[band]; // wavelength [micrometer]
  double mi = table1[band][4];
  double chie = 2.*Math.PI*Calc_de(Dmax, shape)/2./lambda; //effective size parameter of nonspherical ice crystal
  double gammae = 4.*chie*mi;
  double eta2_abs = table4[band][shape][2];

  double Qabsprimeprime = 1-Math.exp(-2./3.*gammae*eta2_abs);
  return Qabsprimeprime;
 }

 // ====== Solar spectrum ======

 /**
  * Calculates the solar extinction efficiency for an individual non-spherical ice
  * crystal under consideration of complex ray behavior.
  * @param Dmax Maximum dimension in micrometers. Range: [2, 10'000]
  * @param band Spectral band index. Range: [0, 5]
  * @param shape Shape index of the ice crystal. Range: [0, 5]
  */
 public double Calc_Qext_sol_i(double Dmax, int band, int shape){
  // band ... das gewuenschte spektrale Band
  //   0 = 0.55 micrometer
  //   1 = 1.35 micrometer
  //   2 = 2.25 micrometer
  //   3 = 3.0125 micrometer
  //   4 = 3.775 micrometer
  //   5 = 4.5 micrometer
  // shape ... 0 = Plate
  //           1 = Column
  //           2 = Hollow Column
  //           3 = Bullet rosettes-4
  //           4 = Bullet rosettes-6
  //           5 = Aggregates
  double Qextprime = Calc_Qextprime(Dmax, band, shape);
  double Qextprimeprime = Calc_Qextprimeprime(Dmax, band, shape);
  double xi_ext = table3[band][shape][6];

  double Qext_sol =  (1.-xi_ext) * Qextprime + xi_ext * Qextprimeprime;
  return Qext_sol;
 }

 /**
  * Calculates the solar extinction efficiency for a non-spherical ice crystal
  * under consideration of complex ray behavior.
  * @param Dmax Maximum dimension in micrometers. Range: [2, 10'000]
  * @param band Spectral band index. Range: [0, 5]
  */
 public double Calc_Qext_sol(double Dmax, int band){
  // band ... das gewuenschte spektrale Band
  //   0 = 0.55 micrometer
  //   1 = 1.35 micrometer
  //   2 = 2.25 micrometer
  //   3 = 3.0125 micrometer
  //   4 = 3.775 micrometer
  //   5 = 4.5 micrometer
  // shape ... 0 = Plate
  //           1 = Column
  //           2 = Hollow Column
  //           3 = Bullet rosettes-4
  //           4 = Bullet rosettes-6
  //           5 = Aggregates
  double[] f = {0., 0., 0., 0., 0., 1.};
  if (Dmax<70){
   // 50% Bullet rosettes-6
   // 25% hollow columns
   // 25% plates
   f = new double[] {0.25, 0., 0.25, 0., 0.5, 0.};
  } else {
   // 30% Aggregates
   // 30% Bullet rosettes-6
   // 20% hollow columns
   // 20% plates
   f = new double[] {0.2, 0., 0.2, 0., 0.3, 0.3};
  };
  double zaehler =0.;
  double nenner =0.;
  for (int i = 0; i<6; i++) {
   zaehler += f[i]*Math.pow(Calc_Da(Dmax,i),2)*Calc_Qext_sol_i(Dmax, band, i);
   nenner += f[i]*Math.pow(Calc_Da(Dmax,i),2);
  };

  double Qext_sol =  zaehler/nenner;
  return Qext_sol;
 }

 /**
  * Calculates the solar absorption efficiency for an individual non-spherical ice
  * crystal under consideration of complex ray behavior.
  * @param Dmax Maximum dimension in micrometers. Range: [2, 10'000]
  * @param band Spectral band index. Range: [0, 5]
  * @param shape Shape index of the ice crystal. Range: [0, 5]
  */
 public double Calc_Qabs_sol_i(double Dmax, int band, int shape){
  // band ... das gewuenschte spektrale Band
  //   0 = 0.55 micrometer
  //   1 = 1.35 micrometer
  //   2 = 2.25 micrometer
  //   3 = 3.0125 micrometer
  //   4 = 3.775 micrometer
  //   5 = 4.5 micrometer
  // shape ... 0 = Plate
  //           1 = Column
  //           2 = Hollow Column
  //           3 = Bullet rosettes-4
  //           4 = Bullet rosettes-6
  //           5 = Aggregates
  double Qabsprime = Calc_Qabsprime(Dmax, band, shape);
  double Qabsprimeprime = Calc_Qabsprimeprime(Dmax, band, shape);
  double xi1_abs = table4[band][shape][3];
  double xi2_abs = table4[band][shape][4];

  double Qabs_sol = (1-xi1_abs)*((1-xi2_abs)*Qabsprime + xi2_abs*Qabsprimeprime);
  return Qabs_sol;
 }

 /**
  * Calculates the solar absorption efficiency for a non-spherical ice crystal
  * under consideration of complex ray behavior.
  * @param Dmax Maximum dimension in micrometers. Range: [2, 10'000]
  * @param band Spectral band index. Range: [0, 5]
  */
 public double Calc_Qabs_sol(double Dmax, int band){
  // band ... das gewuenschte spektrale Band
  //   0 = 0.55 micrometer
  //   1 = 1.35 micrometer
  //   2 = 2.25 micrometer
  //   3 = 3.0125 micrometer
  //   4 = 3.775 micrometer
  //   5 = 4.5 micrometer
  double[] f = {0., 0., 0., 0., 0., 1.};
  if (Dmax<70){
   // 50% Bullet rosettes-6
   // 25% hollow columns
   // 25% plates
   f = new double[] {0.25, 0., 0.25, 0., 0.5, 0.};
  } else {
   // 30% Aggregates
   // 30% Bullet rosettes-6
   // 20% hollow columns
   // 20% plates
   f = new double[] {0.2, 0., 0.2, 0., 0.3, 0.3};
  };
  double zaehler =0.;
  double nenner =0.;
  for (int i = 0; i<6; i++) {
   zaehler += f[i]*Math.pow(Calc_Da(Dmax,i),2)*Calc_Qabs_sol_i(Dmax, band, i);
   nenner += f[i]*Math.pow(Calc_Da(Dmax,i),2);
  };

  double Qabs_sol =  zaehler/nenner;
  return Qabs_sol;
 }

 /**
  * Calculates the solar scattering efficiency for an individual non-spherical ice
  * crystal under consideration of complex ray behavior.
  * @param Dmax Maximum dimension in micrometers. Range: [2, 10'000]
  * @param band Spectral band index. Range: [0, 5]
  * @param shape Shape index of the ice crystal. Range: [0, 5]
  */
 public double Calc_Qsca_sol_i(double Dmax, int band, int shape){
  // band ... das gewuenschte spektrale Band
  //   0 = 0.55 micrometer
  //   1 = 1.35 micrometer
  //   2 = 2.25 micrometer
  //   3 = 3.0125 micrometer
  //   4 = 3.775 micrometer
  //   5 = 4.5 micrometer
  // shape ... 0 = Plate
  //           1 = Column
  //           2 = Hollow Column
  //           3 = Bullet rosettes-4
  //           4 = Bullet rosettes-6
  //           5 = Aggregates
  double Qsca_sol = Calc_Qext_sol_i(Dmax, band, shape) - Calc_Qabs_sol_i(Dmax, band, shape);
  return Qsca_sol;
 }

 /**
  * Calculates the solar scattering efficiency for a non-spherical ice crystal
  * under consideration of complex ray behavior.
  * @param Dmax Maximum dimension in micrometers. Range: [2, 10'000]
  * @param band Spectral band index. Range: [0, 5]
  */
 public double Calc_Qsca_sol(double Dmax, int band){
  // band ... das gewuenschte spektrale Band
  //   0 = 0.55 micrometer
  //   1 = 1.35 micrometer
  //   2 = 2.25 micrometer
  //   3 = 3.0125 micrometer
  //   4 = 3.775 micrometer
  //   5 = 4.5 micrometer
  double Qsca_sol = Calc_Qext_sol(Dmax, band) - Calc_Qabs_sol(Dmax, band);
  return Qsca_sol;
 }

 /**
  * Calculates the solar asymmetry factor for an individual non-spherical ice
  * crystal under consideration of complex ray behavior.
  * @param Dmax Maximum dimension in micrometers. Range: [2, 10'000]
  * @param band Spectral band index. Range: [0, 5]
  * @param shape Shape index of the ice crystal. Range: [0, 5]
  */
 public double Calc_g_sol_i(double Dmax, int band, int shape){
  // band ... das gewuenschte spektrale Band
  //   0 = 0.55 micrometer
  //   1 = 1.35 micrometer
  //   2 = 2.25 micrometer
  //   3 = 3.0125 micrometer
  //   4 = 3.775 micrometer
  //   5 = 4.5 micrometer
  // shape ... 0 = Plate
  //           1 = Column
  //           2 = Hollow Column
  //           3 = Bullet rosettes-4
  //           4 = Bullet rosettes-6
  //           5 = Aggregates
  double lambda = bands[band]; // wavelength [micrometer]
  double chie = 2.*Math.PI*Calc_de(Dmax, shape)/2./lambda; //effective size parameter of nonspherical ice crystal
  double xi1_g = table5[band][shape][1];
  double xi2_g = table5[band][shape][2];
  double xi3_g = table5[band][shape][3];
  double xi4_g = table5[band][shape][4];
  double xi5_g = table5[band][shape][5];
  double xi6_g = table5[band][shape][6];
  double xi7_g = table5[band][shape][7];

  double f1 = (1-xi1_g) * ( 1 - (1-Math.exp(-1.*xi2_g*(chie + xi3_g))) / (xi2_g*(chie+xi3_g)) );
  double f2 = (1-xi4_g) * (1 - Math.exp(-1.*xi5_g*(chie+xi6_g)));
  double g_sol = (1-xi7_g)*f1 + xi7_g*f2; //solarer Asymmetriefaktor
  return g_sol;
 }

 /**
  * Calculates the solar asymmetry factor for a non-spherical ice crystal
  * under consideration of complex ray behavior.
  * @param Dmax Maximum dimension in micrometers. Range: [2, 10'000]
  * @param band Spectral band index. Range: [0, 5]
  */
 public double Calc_g_sol(double Dmax, int band){
  // band ... das gewuenschte spektrale Band
  //   0 = 0.55 micrometer
  //   1 = 1.35 micrometer
  //   2 = 2.25 micrometer
  //   3 = 3.0125 micrometer
  //   4 = 3.775 micrometer
  //   5 = 4.5 micrometer
  double[] f = {0., 0., 0., 0., 0., 1.};
  if (Dmax<70){
   // 50% Bullet rosettes-6
   // 25% hollow columns
   // 25% plates
   f = new double[] {0.25, 0., 0.25, 0., 0.5, 0.};
  } else {
   // 30% Aggregates
   // 30% Bullet rosettes-6
   // 20% hollow columns
   // 20% plates
   f = new double[] {0.2, 0., 0.2, 0., 0.3, 0.3};
  };
  double zaehler =0.;
  double nenner =0.;
  for (int i = 0; i<6; i++) {
   zaehler += f[i]*Math.pow(Calc_Da(Dmax,i),2)*Calc_Qsca_sol_i(Dmax, band, i)*Calc_g_sol_i(Dmax, band, i);
   nenner += f[i]*Math.pow(Calc_Da(Dmax,i),2)*Calc_Qsca_sol_i(Dmax, band, i);
  };

  double g_sol =  zaehler/nenner;
  return g_sol;
 }

 // ====== Terrestrial spectrum ======

 /**
  * Calculates the extinction efficiency for a non-spherical ice crystal
  * under consideration of complex ray behavior.
  * @param Dmax Maximum dimension in micrometers. Range: [2, 10'000]
  * @param lambda Wavelength in micrometers. Range: [3.08, 99.99]
  */
 public double Calc_Qext_terr(double Dmax, double lambda){
  if (lambda < 3.08) System.out.println("Error Calculating Qext_terr: Lambda "+lambda+" < 3.08\n Result should not be trusted.");
  if (lambda > 99.9) System.out.println("Error Calculating Qext_terr: Lambda "+lambda+" > 99.9\n Result should not be trusted.");

  double de = Calc_de_mix(Dmax);
  // Find parameters in table_terr1
  int band = 0;
  while ((lambda > (table_terr1[band][0]+table_terr1[band+1][0])/2.) && (band < 47)) {
   band++;
  };
  if (lambda > (table_terr1[47][0]+table_terr1[48][0])/2.) band = 48;
  double eta1 = table_terr1[band][1];
  double eta2 = table_terr1[band][2];
  double eta3 = table_terr1[band][3];

  double Qext_terr = (2.+eta1/de)/(1.+eta2/de+eta3/Math.pow(de,2.));
  return Qext_terr;
 }

 /**
  * Calculates the absorption efficiency for a non-spherical ice crystal
  * under consideration of complex ray behavior.
  * @param Dmax Maximum dimension in micrometers. Range: [2, 10'000]
  * @param lambda Wavelength in micrometers. Range: [3.08, 99.99]
  */
 public double Calc_Qabs_terr(double Dmax, double lambda){
  if (lambda < 3.08) System.out.println("Error Calculating Qabs_terr: Lambda "+lambda+" < 3.08\n Result should not be trusted.");
  if (lambda > 99.9) System.out.println("Error Calculating Qabs_terr: Lambda "+lambda+" > 99.9\n Result should not be trusted.");

  double de = Calc_de_mix(Dmax);
  // Find parameters in table_terr1
  int band = 0;
  while ((lambda > (table_terr1[band][0]+table_terr1[band+1][0])/2.) && (band < 47)) {
   band++;
  };
  if (lambda > (table_terr1[47][0]+table_terr1[48][0])/2.) band = 48;

  double xi0 = table_terr1[band][4];
  double xi1 = table_terr1[band][5];
  double xi2 = table_terr1[band][6];
  double xi3 = table_terr1[band][7];

  double Qabs_terr = (xi0+xi1/de)/(1.+xi2/de+xi3/Math.pow(de,2.));
  return Qabs_terr;
 }

 /**
  * Calculates the scattering efficiency for a non-spherical ice crystal
  * under consideration of complex ray behavior.
  * @param Dmax Maximum dimension in micrometers. Range: [2, 10'000]
  * @param lambda Wavelength in micrometers. [Range: 3.08, 99.99]
  */
 public double Calc_Qsca_terr(double Dmax, double lambda){
  if (lambda < 3.08) System.out.println("Error Calculating Qsca_terr: Lambda "+lambda+" < 3.08\n Result should not be trusted.");
  if (lambda > 99.9) System.out.println("Error Calculating Qsca_terr: Lambda "+lambda+" > 99.9\n Result should not be trusted.");

  double Qsca_terr = Calc_Qext_terr(Dmax, lambda)-Calc_Qabs_terr(Dmax, lambda);
  return Qsca_terr;
 }

 /**
  * Calculates the asymmetry factor for a non-spherical ice crystal
  * under consideration of complex ray behavior
  * @param de Effective diameter in micrometers.
  * @param lambda Wavelength in micrometers. Range: [3.08, 99.99]
  */
 public double Calc_g_terr(double de, double lambda){
  if (lambda < 3.08) System.out.println("Error Calculating g_terr: Lambda "+lambda+" < 3.08\n Result should not be trusted.");
  if (lambda > 99.9) System.out.println("Error Calculating g_terr: Lambda "+lambda+" > 99.9\n Result should not be trusted.");

  // Find parameters in table_terr1
  int band = 0;
  while ((lambda > (table_terr1[band][0]+table_terr1[band+1][0])/2.) && (band < 47)) {
   band++;
  };
  if (lambda > (table_terr1[47][0]+table_terr1[48][0])/2.) band = 48;

  double zeta0 = table_terr1[band][8];
  double zeta1 = table_terr1[band][9];
  double zeta2 = table_terr1[band][10];
  double zeta3 = table_terr1[band][11];

  double g_terr = (zeta0+zeta1/de)/(1.+zeta2/de+zeta3/Math.pow(de,2.));
  return g_terr;
 }

 public static void main (String[] args) {
  ExtinctionEfficiencyLegacy a = new ExtinctionEfficiencyLegacy();
   
   /*double d = 0.;
   while (d<=100) {
     System.out.println(Math.exp(0.069*d) + "\t"+a.Calc_g_sol(Math.exp(0.069*d), 0));
     d=d+1;
   };*/

  //System.out.println("Q_ext_sol_mix "+a.Calc_Qext_sol(10., 0));
  //System.out.println("Q_abs_sol_mix "+a.Calc_Qabs_sol(10., 0));
  //System.out.println("Q_sca_sol_mix "+a.Calc_Qsca_sol(10., 0));
  //System.out.println("g_sol_mix "+a.Calc_g_sol(10., 0));
  //  public double Calc_Qext_sol(double Dmax, int band)
// public double Calc_Qabs_sol(double Dmax, int band)
// public double Calc_Qsca_sol(double Dmax, int band)
// public double Calc_g_sol(double Dmax, int band)
  /* System.out.println("Q_ext_sol_mix "+a.Calc_Qext_sol(11.2, 0));
   System.out.println("Q_ext_sol_mix "+a.Calc_Qext_sol(11.2, 1));
   System.out.println("Q_ext_sol_mix "+a.Calc_Qext_sol(11.2, 2));
   System.out.println("Q_ext_sol_mix "+a.Calc_Qext_sol(11.2, 3));
   System.out.println("Q_ext_sol_mix "+a.Calc_Qext_sol(11.2, 4));
   System.out.println("Q_ext_sol_mix "+a.Calc_Qext_sol(11.2, 5));
   System.out.println("Q_ext_sol_mix "+a.Calc_Qext_sol(11.2, 6)+"\n");
   
   System.out.println("Q_ext_sol_mix "+a.Calc_Qabs_sol(11.2, 0));
   System.out.println("Q_ext_sol_mix "+a.Calc_Qabs_sol(11.2, 1));
   System.out.println("Q_ext_sol_mix "+a.Calc_Qabs_sol(11.2, 2));
   System.out.println("Q_ext_sol_mix "+a.Calc_Qabs_sol(11.2, 3));
   System.out.println("Q_ext_sol_mix "+a.Calc_Qabs_sol(11.2, 4));
   System.out.println("Q_ext_sol_mix "+a.Calc_Qabs_sol(11.2, 5));
   System.out.println("Q_ext_sol_mix "+a.Calc_Qabs_sol(11.2, 6)+"\n");
   
   System.out.println("Q_ext_sol_mix "+a.Calc_Qsca_sol(11.2, 0));
   System.out.println("Q_ext_sol_mix "+a.Calc_Qsca_sol(11.2, 1));
   System.out.println("Q_ext_sol_mix "+a.Calc_Qsca_sol(11.2, 2));
   System.out.println("Q_ext_sol_mix "+a.Calc_Qsca_sol(11.2, 3));
   System.out.println("Q_ext_sol_mix "+a.Calc_Qsca_sol(11.2, 4));
   System.out.println("Q_ext_sol_mix "+a.Calc_Qsca_sol(11.2, 5));
   System.out.println("Q_ext_sol_mix "+a.Calc_Qsca_sol(11.2, 6)+"\n");
   
   System.out.println("Q_ext_sol_mix "+a.Calc_g_sol(11.2, 0));
   System.out.println("Q_ext_sol_mix "+a.Calc_g_sol(11.2, 1));
   System.out.println("Q_ext_sol_mix "+a.Calc_g_sol(11.2, 2));
   System.out.println("Q_ext_sol_mix "+a.Calc_g_sol(11.2, 3));
   System.out.println("Q_ext_sol_mix "+a.Calc_g_sol(11.2, 4));
   System.out.println("Q_ext_sol_mix "+a.Calc_g_sol(11.2, 5));
   System.out.println("Q_ext_sol_mix "+a.Calc_g_sol(11.2, 6)+"\n");*/

  // System.out.println("Q_abs_sol "+a.Calc_Qabs_sol_i(1500., 4, 0));
  //System.out.println("Q_abs_sol "+a.Calc_Qabs_sol_i(1500., 4, 1));
  //System.out.println("Q_abs_sol "+a.Calc_Qabs_sol_i(1500., 4, 2));
  //System.out.println("Q_abs_sol "+a.Calc_Qabs_sol_i(1500., 4, 3));
  //System.out.println("Q_abs_sol "+a.Calc_Qabs_sol_i(1500., 4, 4));
  //System.out.println("Q_abs_sol "+a.Calc_Qabs_sol_i(1500., 4, 5));
  //System.out.println("Q_abs_sol_mix "+a.Calc_Qabs_sol(1500., 4)+"\n");
  //System.out.println("g_sol "+a.Calc_g_sol_i(1500., 4, 0));
  //System.out.println("g_sol "+a.Calc_g_sol_i(1500., 4, 1));
  //System.out.println("g_sol "+a.Calc_g_sol_i(1500., 4, 2));
  //System.out.println("g_sol "+a.Calc_g_sol_i(1500., 4, 3));
  //System.out.println("g_sol "+a.Calc_g_sol_i(1500., 4, 4));
  //System.out.println("g_sol "+a.Calc_g_sol_i(1500., 4, 5));
  //System.out.println("g_sol_mix "+a.Calc_g_sol(1500., 4)+"\n");

  System.out.println("Q_ext_terr "+a.Calc_Qext_terr(23.6,5.6));
  System.out.println("Q_ext_terr "+a.Calc_Qext_terr(23.6,6));
  System.out.println("Q_ext_terr "+a.Calc_Qext_terr(23.6,6.8));
  System.out.println("Q_ext_terr "+a.Calc_Qext_terr(23.6,7.2));
  System.out.println("Q_ext_terr "+a.Calc_Qext_terr(23.6,7.7));
  System.out.println("Q_ext_terr "+a.Calc_Qext_terr(23.6,9));
  System.out.println("Q_ext_terr "+a.Calc_Qext_terr(23.6,9.5));
  System.out.println("Q_ext_terr "+a.Calc_Qext_terr(23.6,10.5));
  System.out.println("Q_ext_terr "+a.Calc_Qext_terr(23.6,11.2));
  System.out.println("Q_ext_terr "+a.Calc_Qext_terr(23.6,11.8));
  System.out.println("Q_ext_terr "+a.Calc_Qext_terr(23.6,13));
  System.out.println("Q_ext_terr "+a.Calc_Qext_terr(23.6,14));
  System.out.println("Q_ext_terr "+a.Calc_Qext_terr(23.6,17.5));
  System.out.println("Q_ext_terr "+a.Calc_Qext_terr(23.6,21.5)+"\n");
  System.out.println("Q_abs_terr "+a.Calc_Qabs_terr(23.6,5.6));
  System.out.println("Q_abs_terr "+a.Calc_Qabs_terr(23.6,6.0));
  System.out.println("Q_abs_terr "+a.Calc_Qabs_terr(23.6,6.8));
  System.out.println("Q_abs_terr "+a.Calc_Qabs_terr(23.6,7.2));
  System.out.println("Q_abs_terr "+a.Calc_Qabs_terr(23.6,7.7));
  System.out.println("Q_abs_terr "+a.Calc_Qabs_terr(23.6,9));
  System.out.println("Q_abs_terr "+a.Calc_Qabs_terr(23.6,9.5));
  System.out.println("Q_abs_terr "+a.Calc_Qabs_terr(23.6,10.5));
  System.out.println("Q_abs_terr "+a.Calc_Qabs_terr(23.6,11.2));
  System.out.println("Q_abs_terr "+a.Calc_Qabs_terr(23.6,11.8));
  System.out.println("Q_abs_terr "+a.Calc_Qabs_terr(23.6,13));
  System.out.println("Q_abs_terr "+a.Calc_Qabs_terr(23.6,14));
  System.out.println("Q_abs_terr "+a.Calc_Qabs_terr(23.6,17.5));
  System.out.println("Q_abs_terr "+a.Calc_Qabs_terr(23.6,21.5)+"\n");
  System.out.println("g_terr "+a.Calc_g_terr(23.6,5.6));
  System.out.println("g_terr "+a.Calc_g_terr(23.6,6));
  System.out.println("g_terr "+a.Calc_g_terr(23.6,6.8));
  System.out.println("g_terr "+a.Calc_g_terr(23.6,7.2));
  System.out.println("g_terr "+a.Calc_g_terr(23.6,7.7));
  System.out.println("g_terr "+a.Calc_g_terr(23.6,9));
  System.out.println("g_terr "+a.Calc_g_terr(23.6,9.5));
  System.out.println("g_terr "+a.Calc_g_terr(23.6,10.5));
  System.out.println("g_terr "+a.Calc_g_terr(23.6,11.2));
  System.out.println("g_terr "+a.Calc_g_terr(23.6,11.8));
  System.out.println("g_terr "+a.Calc_g_terr(23.6,13));
  System.out.println("g_terr "+a.Calc_g_terr(23.6,14));
  System.out.println("g_terr "+a.Calc_g_terr(23.6,17.5));
  System.out.println("g_terr "+a.Calc_g_terr(23.6,21.5)+"\n");
 }
}
