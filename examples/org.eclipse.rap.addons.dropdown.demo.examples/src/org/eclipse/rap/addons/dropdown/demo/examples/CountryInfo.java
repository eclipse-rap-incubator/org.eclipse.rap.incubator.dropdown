/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.addons.dropdown.demo.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.rwt.SingletonUtil;


public class CountryInfo {

  private static final String CHARSET = "UTF-8";

  public static CountryInfo getInstance() {
    return SingletonUtil.getSessionInstance( CountryInfo.class );
  }

  private Country[] countries;

  public Country[] getCountries() {
    return countries;
  }

  private CountryInfo() {
    try {
      countries = readCountries();
    } catch( IOException ex ) {
      throw new IllegalStateException( "Could not parse countries", ex );
    }
  }

  private static Country[] readCountries() throws IOException {
    File file = new File( getGeoDataDirectory(), "countries.txt" );
    InputStream stream = new FileInputStream( file );
    BufferedReader reader = new BufferedReader( new InputStreamReader( stream, CHARSET ) );
    List<Country> countries = new ArrayList<Country>( 252 );
    try {
      String line = reader.readLine();
      while( line != null ) {
        if( line.charAt( 0 ) != '#' ) {
          countries.add( new Country( line ) );
        }
        line = reader.readLine();
      }
    } finally {
      reader.close();
    }
    return countries.toArray( new Country[ 0 ] );
  }

  private static City[] readCities( Country country ) throws IOException {
    File file = new File( getGeoDataDirectory(), "cities/" + country.iso + ".txt" );
    InputStream stream = new FileInputStream( file );
    BufferedReader reader = new BufferedReader( new InputStreamReader( stream, CHARSET ) );
    List<City> cities = new ArrayList<City>( 250 );
    try {
      String line = reader.readLine();
      while( line != null ) {
        if( line.charAt( 0 ) != '#' ) {
          cities.add( new City( line ) );
        }
        line = reader.readLine();
      }
    } finally {
      reader.close();
    }
    return cities.toArray( new City[ 0 ] );
  }

  private static File getGeoDataDirectory() {
    // TODO [rst] Use ExampleUtil.getDataDirectory() ?
    File dataDirectory = new File( "/data/rapdemo" );
    return new File( dataDirectory, "geodata" );
  }

  static class Country {

    public final String iso;
    public final String name;
    private City[] cities = null;

    public Country( String line ) {
      String[] data = line.split( "\t" );
      iso = data[ 0 ];
      name = data[ 1 ];
    }

    @Override
    public String toString() {
      return name;
    }

    public City[] getCities() {
      if( cities == null ) {
        try {
          cities = readCities( this );
        } catch( IOException ex ) {
          throw new IllegalStateException( "Could not parse cities for " + name, ex );
        }
      }
      return cities;
    }

  }

  static class City {

    public final String name;
    public final double latitude;
    public final double longitude;

    public City( String line ) {
      String[] data = line.split( "\t" );
      name = data[ 0 ];
      latitude = Double.parseDouble( data[ 1 ] );
      longitude = Double.parseDouble( data[ 2 ] );
    }

    @Override
    public String toString() {
      return name;
    }

  }

}
