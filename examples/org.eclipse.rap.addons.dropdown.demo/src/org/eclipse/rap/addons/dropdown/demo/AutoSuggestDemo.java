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

import org.eclipse.rap.addons.autosuggest.AutoSuggest;
import org.eclipse.rap.addons.autosuggest.DataProvider;
import org.eclipse.rap.addons.autosuggest.DataSource;
import org.eclipse.rap.addons.autosuggest.SuggestionSelectedListener;
import org.eclipse.rap.addons.dropdown.demo.data.KFZ;
import org.eclipse.rap.addons.dropdown.demo.scripts.CustomAutoSuggestClientListener;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.scripting.ClientListener;
import org.eclipse.rap.rwt.widgets.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;


public class AutoSuggestDemo extends AbstractEntryPoint {

  private Text text;
  private AutoSuggest autoSuggest;
  private DataSource de;
  private DataSource at;
  private boolean useCustomScripts = false;
  private boolean useAutoComplete = false;
  private DataSource currentDataSource;
  private Table table;

  @Override
  protected void createContents( Composite parent ) {
    createDataSources();
    setupWidgetDataWhiteList();
    Composite composite = new Composite( parent, SWT.NONE );
    composite.setLayout( new GridLayout( 3, false ) );
    createTextArea( composite );
    createAutoSuggest();
    createLocationArea( composite );
    createConfigArea( composite );
    createLogArea( composite );
    createLogger();
    createDisposeButton( composite );
  }

  private void createDataSources() {
    de = new DataSource();
    de.setDataProvider( new DataProvider<String[]>() {
      @Override
      public Iterable<String[]> getSuggestions() {
        return Arrays.asList( KFZ.DE );
      }
      @Override
      public String getValue( String[] suggestion ) {
        return suggestion[ 2 ] + " (" + suggestion[ 0 ] + ")";
      }
    } );
    at = new DataSource();
    at.setDataProvider( new DataProvider<String[]>() {
      @Override
      public Iterable<String[]> getSuggestions() {
        return Arrays.asList( KFZ.AT );
      }
      @Override
      public String getValue( String[] suggestion ) {
        return suggestion[ 1 ] + " (" + suggestion[ 0 ] + ")";
      }
    } );
    currentDataSource = de;
  }

  private void createTextArea( Composite parent ) {
    text = new Text( parent, SWT.BORDER );
    text.setData( RWT.CUSTOM_VARIANT, "dropdown" );
    text.setCursor( text.getDisplay().getSystemCursor( SWT.CURSOR_ARROW ) );
    GridData gridData = new GridData( 200, 23 );
    gridData.verticalAlignment = SWT.TOP;
    text.setLayoutData( gridData );
    text.setMessage( "City" );
    text.setFocus();
  }

  public void createAutoSuggest() {
    if( useCustomScripts ) {
      autoSuggest = new CustomAutoSuggest( text );
    } else {
      autoSuggest = new AutoSuggest( text );
    }
    autoSuggest.setDataSource( currentDataSource );
    autoSuggest.setAutoComplete( useAutoComplete );
  }

  private void createLocationArea( Composite parent ) {
    Group location = new Group( parent, SWT.NONE );
    location.setText( "Location" );
    GridData layoutData = new GridData( SWT.CENTER, SWT.FILL, false, true );
    location.setLayoutData( layoutData );
    layoutData.verticalSpan = 2;
    location.setLayout( new GridLayout( 1, true ) );
    final Button germany = new Button( location, SWT.RADIO );
    germany.setText( "Germany" );
    germany.setSelection( true );
    germany.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( Event event ) {
        if( germany.getSelection() ) {
          currentDataSource = de;
          autoSuggest.setDataSource( de );
        }
      }
    } );
    final Button austria = new Button( location, SWT.RADIO );
    austria.setText( "Austria" );
    austria.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( Event event ) {
        if( austria.getSelection() ) {
          currentDataSource = at;
          autoSuggest.setDataSource( at );
        }
      }
    } );
  }

  private void createConfigArea( Composite parent ) {
    Group location = new Group( parent, SWT.NONE );
    location.setText( "Config" );
    GridData layoutData = new GridData( SWT.CENTER, SWT.FILL, false, true );
    location.setLayoutData( layoutData );
    layoutData.verticalSpan = 2;
    location.setLayout( new GridLayout( 1, true ) );
    final Button autoComplete = new Button( location, SWT.CHECK );
    autoComplete.setText( "AutoComplete" );
    autoComplete.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( Event event ) {
        useAutoComplete = autoComplete.getSelection();
        autoSuggest.setAutoComplete( useAutoComplete );
      }
    } );
    final Button customScripts = new Button( location, SWT.CHECK );
    customScripts.setText( "Custom Scripts" );
    customScripts.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( Event event ) {
        autoSuggest.dispose();
        useCustomScripts = customScripts.getSelection();
        createAutoSuggest();
        createLogger();
      }
    } );
    customScripts.setEnabled( false ); // Broken in Demo due to Bug 415485
  }

  private void createLogArea( Composite parent ) {
    table = new Table( parent, SWT.BORDER | SWT.HIDE_SELECTION );
    new TableColumn( table, SWT.NONE ).setText( "KFZ" );
    new TableColumn( table, SWT.NONE ).setText( "Area" );
    table.getColumn( 0 ).setWidth( 200 );
    table.getColumn( 1 ).setWidth( 40 );
    GridData gridData = new GridData( 250, 60 );
    table.setLayoutData( gridData );
  }

  private void createLogger() {
    autoSuggest.addSelectionListener( new SuggestionSelectedListener() {
      @Override
      public void suggestionSelected() {
        TableItem item = new TableItem( table, SWT.NONE );
        item.setText( text.getText() );
        table.setTopIndex( table.indexOf( item ) );
      }
    } );
  }

  private void createDisposeButton( Composite parent ) {
    Button button = new Button( parent, SWT.PUSH );
    button.setText( "Dispose!" );
    button.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( Event event ) {
        autoSuggest.setDataSource( at );
        text.dispose();
      }
    } );
  }

  private static void setupWidgetDataWhiteList() {
    WidgetUtil.registerDataKeys( "dropdown", "text", "data" );
  }

  private class CustomAutoSuggest extends AutoSuggest {

    public CustomAutoSuggest( Text text ) {
      super( text );
    }

    @Override
    protected ClientListener getAutoSuggestListener() {
      return CustomAutoSuggestClientListener.getInstance();
    }

  }

}
