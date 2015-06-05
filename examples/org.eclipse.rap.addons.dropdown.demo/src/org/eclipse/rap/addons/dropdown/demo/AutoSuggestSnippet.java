/*******************************************************************************
 * Copyright (c) 2013, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.addons.dropdown.demo;

import java.util.Arrays;

import org.eclipse.rap.addons.autosuggest.ArrayDataProvider;
import org.eclipse.rap.addons.autosuggest.AutoSuggest;
import org.eclipse.rap.addons.autosuggest.ColumnDataProvider;
import org.eclipse.rap.addons.autosuggest.ColumnTemplate;
import org.eclipse.rap.addons.autosuggest.DataSource;
import org.eclipse.rap.addons.autosuggest.SuggestionSelectedListener;
import org.eclipse.rap.addons.dropdown.demo.data.KFZ;
import org.eclipse.rap.addons.dropdown.demo.data.Movies;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;


public class AutoSuggestSnippet extends AbstractEntryPoint {

  private DataSource simpleDataSource;
  private DataSource columnDataSource;
  private Text text1;

  @Override
  protected void createContents( Composite parent ) {
    parent.setLayout( new FormLayout() );
    createDataSources();
    text1 = new Text( parent, SWT.BORDER );
    text1.setLayoutData( createTextData( null ) );
    AutoSuggest autoSuggest1 = new AutoSuggest( text1 );
    autoSuggest1.setAutoComplete( true );
    autoSuggest1.setDataSource( simpleDataSource );
    autoSuggest1.addSelectionListener( new SuggestionSelectedListener() {
      @Override
      public void suggestionSelected() {
        System.out.println( "selected: " + text1.getText() );
      }
    } );

    Text text2 = new Text( parent, SWT.BORDER );
    text2.setLayoutData( createTextData( text1 ) );
    AutoSuggest autoSuggest2 = new AutoSuggest( text2 );
    autoSuggest2.setDataSource( columnDataSource );
  }

  private void createDataSources() {
    simpleDataSource = new DataSource();
    simpleDataSource.setDataProvider( new ArrayDataProvider( Movies.VALUES ) );
    columnDataSource = new DataSource();
    columnDataSource.setTemplate( new ColumnTemplate( 50, 150, 150 ) );
    columnDataSource.setDataProvider( new ColumnDataProvider<String[]>() {
      @Override
      public Iterable<String[]> getSuggestions() {
        return Arrays.asList( KFZ.DE );
      }
      @Override
      public String getValue( String[] suggestion ) {
        return suggestion[ 0 ];
      }
      @Override
      public String[] getTexts( String[] suggestion ) {
        return new String[] { suggestion[ 0 ], suggestion[ 2 ], suggestion[ 3 ] };
      }
    } );
  }

  private static FormData createTextData( Control control ) {
    FormData formData = new FormData();
    formData.top = control == null
                 ? new FormAttachment( 0, 100 )
                 : new FormAttachment( control, 50 );
    formData.left = new FormAttachment( 50, -150 );
    formData.right = new FormAttachment( 50, 150 );
    return formData;
  }

}
