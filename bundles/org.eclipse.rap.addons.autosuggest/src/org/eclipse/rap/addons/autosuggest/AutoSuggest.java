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
import org.eclipse.rap.addons.autosuggest.internal.resources.AutoSuggestScript;
import org.eclipse.rap.addons.autosuggest.internal.resources.EventDelegatorScript;
import org.eclipse.rap.addons.dropdown.DropDown;
import org.eclipse.rap.clientscripting.ClientListener;
import org.eclipse.rap.clientscripting.Script;
import org.eclipse.rap.clientscripting.WidgetDataWhiteList;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;


public class AutoSuggest {

  private static final String EVENT_TYPE_SELECTION = "suggestionSelected";
  private static final String MODEL_ID_KEY
    = "org.eclipse.rap.addons.autosuggest#Model";

  private final Text text;
  private final DropDown dropDown;
  private final Model model;
  private final List<SuggestionSelectedListener> selectionListeners;
  private final ModelListener modelListener;
  private ClientListener textClientListener;
  private int[] textClientListenerTypes;
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
    removeTextClientListeners();
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

  protected void attachClientListeners() {
    int[] dropDownEventTypes = new int[] { SWT.Show, SWT.Hide, SWT.Selection, SWT.DefaultSelection };
    attachClientListenerToDropDown( EventDelegatorScript.getInstance(), dropDownEventTypes );
    attachClientListenerToText( EventDelegatorScript.getInstance(), SWT.Modify, SWT.Verify );
    attachClientListenerToModel( AutoSuggestScript.getInstance(), "change", "accept" );
  }

  protected void attachClientListenerToText( Script script, int... types ) {
    if( textClientListener != null ) {
      throw new IllegalStateException( "AutoSuggest: Can not add listener to Text twice." );
    }
    textClientListenerTypes = types;
    textClientListener = new ClientListener( script );
    for( int type : types ) {
      text.addListener( type, textClientListener );
    }
  }

  protected void attachClientListenerToDropDown( Script script, int... types ) {
    ClientListener clientListener = new ClientListener( script );
    for( int type : types ) {
      dropDown.addListener( type, clientListener );
    }
  }

  protected void attachClientListenerToModel( Script script, String... types ) {
    ClientModelListener clientModelListener = new ClientModelListener( script );
    for( String type : types ) {
      model.addListener( type, clientModelListener );
    }
    model.set( "autoSuggestListenerId", clientModelListener.getId() );
  }

  private void connectClientObjects() {
    WidgetDataWhiteList.addKey( MODEL_ID_KEY );
    model.set( "textWidgetId", getId( text ) );
    model.set( "dropDownWidgetId", getId( dropDown ) );
    dropDown.setData( MODEL_ID_KEY, model.getId() );
    text.setData( MODEL_ID_KEY, model.getId() );
  }

  private void removeTextClientListeners() {
    for( int type : textClientListenerTypes ) {
      text.removeListener( type, textClientListener );
    }
  }

}
