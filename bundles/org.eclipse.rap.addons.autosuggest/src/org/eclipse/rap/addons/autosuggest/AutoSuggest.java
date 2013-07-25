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
package org.eclipse.rap.addons.autosuggest;

import static org.eclipse.rap.rwt.lifecycle.WidgetUtil.getId;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.addons.autosuggest.internal.ClientModelListener;
import org.eclipse.rap.addons.autosuggest.internal.Model;
import org.eclipse.rap.addons.autosuggest.internal.ModelListener;
import org.eclipse.rap.addons.autosuggest.internal.resources.ResourceLoaderUtil;
import org.eclipse.rap.addons.dropdown.DropDown;
import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.clientscripting.WidgetDataWhiteList;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;


public class AutoSuggest {

  private static final String EVENT_TYPE_SELECTION = "suggestionSelected";
  private final static String LISTENER_PREFIX
    = "org/eclipse/rap/addons/autosuggest/internal/resources/";
  private static final String MODEL_ID_KEY
    = "org.eclipse.rap.addons.autosuggest#Model";

  private final Text text;
  private final DropDown dropDown;
  private final Model model;
  private final List<SuggestionSelectedListener> selectionListeners;
  private final ModelListener modelListener;
  private ClientListener clientListener;
  private boolean isDisposed;

  public AutoSuggest( Text text ) {
    if( text == null ) {
      throw new NullPointerException( "Text must not be null" );
    }
    if( text.isDisposed() ) {
      throw new IllegalArgumentException( "Text must not be disposed" );
    }
    this.text = text;
    dropDown = new DropDown( text );
    model = new Model();
    selectionListeners = new ArrayList<SuggestionSelectedListener>( 1 );
    modelListener = new ModelListener() {
      public void handleEvent( JsonObject argument ) {
        notifySelectionListeners();
      }
    };
    connectClientObjects();
    attachClientListeners();
    text.addListener( SWT.Dispose, new Listener() {
      public void handleEvent( Event event ) {
        dispose();
      }
    } );
  }

  public void setDataSource( DataSource dataSource ) {
    checkDisposed();
    model.set( "dataSourceId", dataSource != null ? dataSource.getId() : null );
    if( dataSource != null ) {
      ColumnTemplate template = dataSource.getTemplate();
      if( template != null ) {
        dropDown.setData( "columns", template.getColumnWidths() );
      }
    }
  }

  public void setVisibleItemCount( int itemCount ) {
    checkDisposed();
    dropDown.setVisibleItemCount( itemCount );
  }

  public int getVisibleItemCount() {
    checkDisposed();
    return dropDown.getVisibleItemCount();
  }

  public void setAutoComplete( boolean value ) {
    checkDisposed();
    model.set( "autoComplete", value );
  }

  public void addSelectionListener( SuggestionSelectedListener listener ) {
    checkDisposed();
    if( listener == null ) {
      throw new NullPointerException( "Parameter was null: listener" );
    }
    if( !selectionListeners.contains( listener ) ) {
      selectionListeners.add( listener );
    }
    if( selectionListeners.size() == 1 ) {
      model.addListener( EVENT_TYPE_SELECTION, modelListener );
    }
  }

  public void removeSelectionListener( SuggestionSelectedListener listener ) {
    checkDisposed();
    if( listener == null ) {
      throw new NullPointerException( "Parameter was null: listener" );
    }
    selectionListeners.remove( listener );
    if( selectionListeners.isEmpty() ) {
      model.removeListener( EVENT_TYPE_SELECTION, modelListener );
    }
  }

  public void dispose() {
    isDisposed = true;
    dropDown.dispose();
    model.dispose();
    text.removeListener( SWT.Verify, clientListener );
    text.removeListener( SWT.Modify, clientListener );
  }

  public boolean isDisposed() {
    return isDisposed;
  }

  DropDown getDropDown() {
    return dropDown;
  }

  void notifySelectionListeners() {
    for( SuggestionSelectedListener listener : selectionListeners ) {
      listener.suggestionSelected();
    }
  }

  private void checkDisposed() {
    if( isDisposed ) {
      throw new IllegalStateException( "AutoSuggest is disposed" );
    }
  }

  private void attachClientListeners() {
    // TODO [tb] : share listener within session
    clientListener = createClientListener( "DataBinding.js" );
    text.addListener( SWT.Modify, clientListener );
    text.addListener( SWT.Verify, clientListener );
    dropDown.addListener( SWT.Show, clientListener );
    dropDown.addListener( SWT.Hide, clientListener );
    dropDown.addListener( SWT.Selection, clientListener );
    dropDown.addListener( SWT.DefaultSelection, clientListener );
    model.addListener( "change", createModelListener( "DataBinding.js" ) );
    ClientModelListener modelListener = createModelListener( "AutoSuggest.js" );
    model.addListener( "change", modelListener );
    model.addListener( "accept", modelListener );
  }

  private void connectClientObjects() {
    WidgetDataWhiteList.addKey( MODEL_ID_KEY );
    model.set( "textWidgetId", getId( text ) );
    model.set( "dropDownWidgetId", getId( dropDown ) );
    dropDown.setData( MODEL_ID_KEY, model.getId() );
    text.setData( MODEL_ID_KEY, model.getId() );
  }

  private static ClientListener createClientListener( String name ) {
    return new ClientListener( ResourceLoaderUtil.readTextContent( LISTENER_PREFIX + name ) );
  }

  private static ClientModelListener createModelListener( String name ) {
    return new ClientModelListener( ResourceLoaderUtil.readTextContent( LISTENER_PREFIX + name ) );
  }

}
