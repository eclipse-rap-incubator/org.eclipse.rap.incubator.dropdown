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

import java.util.Arrays;

import org.eclipse.rap.addons.autosuggest.AutoSuggest;
import org.eclipse.rap.addons.autosuggest.DataProvider;
import org.eclipse.rap.addons.autosuggest.DataSource;
import org.eclipse.rap.addons.autosuggest.SuggestionSelectedListener;
import org.eclipse.rap.addons.dropdown.demo.examples.CountryInfo.City;
import org.eclipse.rap.addons.dropdown.demo.examples.CountryInfo.Country;
import org.eclipse.rap.examples.ExampleUtil;
import org.eclipse.rap.examples.IExamplePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class DropDownExamplePage implements IExamplePage {

  private CityMap cityMap;
  private Text countryText;
  private Text cityText;
  private AutoSuggest cityAutoSuggest;
  private Country currentCountry = null;

  public void createControl( Composite parent ) {
    parent.setLayout( ExampleUtil.createMainLayout( 2 ) );
    createCitySelection( parent );
    createCityMap( parent );
  }

  private void createCitySelection( Composite parent ) {
    Group group = new Group( parent, SWT.NONE );
    group.setText( "City Selection" );
    group.setLayout( new GridLayout( 2, false ) );
    group.setLayoutData( new GridData( SWT.LEFT, SWT.TOP, false, false ) );
    createCountryText( group );
    createCityText( group );
    createCityAutoSuggest();
    createCountryAutoSuggest();
  }

  public void createCountryText( Composite parent ) {
    Label countryLabel = new Label( parent, SWT.NONE );
    countryLabel.setText( "Country:" );
    countryText = new Text( parent, SWT.BORDER );
    countryText.setLayoutData( new GridData( 300, SWT.DEFAULT ) );
    countryText.setFocus();
  }

  public void createCityText( Composite parent ) {
    Label cityLabel = new Label( parent, SWT.NONE );
    cityLabel.setText( "City:" );
    cityText = new Text( parent, SWT.BORDER );
    cityText.setLayoutData( new GridData( 300, SWT.DEFAULT ) );
    cityText.setEnabled( false );
  }

  private void createCountryAutoSuggest() {
    AutoSuggest countryAutoSuggest = new AutoSuggest( countryText );
    countryAutoSuggest.setAutoComplete( true );
    countryAutoSuggest.setDataSource( createCountriesDataSource() );
    countryAutoSuggest.addSelectionListener( new SuggestionSelectedListener() {
      public void suggestionSelected() {
        setCurrentCountry( countryText.getText() );
      }
    } );
  }

  private void setCurrentCountry( String name ) {
    cityText.setText( "" );
    cityText.setEnabled( true );
    currentCountry = CountryInfo.getInstance().findCountry( name );
    cityAutoSuggest.setDataSource( createDataSource( currentCountry ) );
  }

  private void createCityAutoSuggest() {
    cityAutoSuggest = new AutoSuggest( cityText );
    cityAutoSuggest.setAutoComplete( true );
    cityAutoSuggest.addSelectionListener( new SuggestionSelectedListener() {
      public void suggestionSelected() {
        String name = cityText.getText();
        City city = currentCountry.findCity( name );
        cityMap.visit( city );
      }
    } );
  }

  private void createCityMap( Composite parent ) {
    Group group = new Group( parent, SWT.NONE );
    group.setText( "City Map" );
    group.setLayout( new FillLayout() );
    group.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    Browser browser = new Browser( group, SWT.NONE );
    cityMap = new CityMap( browser );
  }

  // TODO [tb] : re-use data sources
  private static DataSource createCountriesDataSource() {
    DataSource countriesDataSource = new DataSource();
    countriesDataSource.setDataProvider( new DataProvider() {
      public Iterable<?> getSuggestions() {
        return Arrays.asList( CountryInfo.getInstance().getCountries() );
      }
      public String getValue( Object element ) {
        return ( ( Country )element ).name;
      }
    } );
    return countriesDataSource;
  }

   // TODO [tb] : re-use data sources
   private static DataSource createDataSource( Country country ) {
     DataSource dataSource = new DataSource();
     final City[] cities = country.getCities();
     dataSource.setDataProvider( new DataProvider() {
       public Iterable< ? > getSuggestions() {
         return Arrays.asList( cities );
       }
       public String getValue( Object element ) {
         return element.toString();
       }
    } );
     return dataSource;
   }

}
