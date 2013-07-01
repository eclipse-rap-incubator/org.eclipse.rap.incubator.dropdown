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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.rap.addons.dropdown.demo.examples.CountryInfo.City;
import org.eclipse.rap.addons.dropdown.demo.examples.CountryInfo.Country;
import org.eclipse.rap.addons.dropdown.viewer.DropDownViewer;
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
    Label countryLabel = new Label( group, SWT.NONE );
    countryLabel.setText( "Country:" );
    Text countryText = new Text( group, SWT.BORDER );
    countryText.setLayoutData( new GridData( 300, SWT.DEFAULT ) );
    Label cityLabel = new Label( group, SWT.NONE );
    cityLabel.setText( "City:" );
    Text cityText = new Text( group, SWT.BORDER );
    cityText.setLayoutData( new GridData( 300, SWT.DEFAULT ) );
    DropDownViewer cityViewer = createCityViewer( cityText );
    createCountryViewer( countryText, cityViewer );
    countryText.setFocus();
    cityText.setEnabled( false );
  }

  private DropDownViewer createCityViewer( Text cityText ) {
    DropDownViewer cityViewer = new DropDownViewer( cityText );
    cityViewer.setContentProvider( new ArrayContentProvider() );
    cityViewer.setLabelProvider( new LabelProvider() );
    cityViewer.addSelectionChangedListener( new ISelectionChangedListener() {
      public void selectionChanged( SelectionChangedEvent event ) {
        IStructuredSelection selection = ( IStructuredSelection )event.getSelection();
        cityMap.visit( ( City )selection.getFirstElement() );
      }
    } );
    return cityViewer;
  }

  private void createCountryViewer( Text countryText, final DropDownViewer cityViewer ) {
    DropDownViewer countryViewer = new DropDownViewer( countryText );
    countryViewer.setContentProvider( new ArrayContentProvider() );
    countryViewer.setLabelProvider( new LabelProvider() );
    countryViewer.setInput( CountryInfo.getInstance().getCountries() );
    countryViewer.addSelectionChangedListener( new ISelectionChangedListener() {
      public void selectionChanged( SelectionChangedEvent event ) {
        IStructuredSelection selection = ( IStructuredSelection )event.getSelection();
        Country country = ( Country )selection.getFirstElement();
        Text text = ( Text )cityViewer.getControl();
        text.setText( "" );
        text.setEnabled( true );
        cityViewer.setInput( country.getCities() );
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

}
