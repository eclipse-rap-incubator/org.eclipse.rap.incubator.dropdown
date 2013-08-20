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

import org.eclipse.rap.addons.autosuggest.internal.resources.AutoSuggestListener;
import org.eclipse.rap.addons.autosuggest.internal.resources.EventDelegatorListener;
import org.eclipse.rap.addons.autosuggest.internal.resources.ModelResources;
import org.eclipse.rap.addons.dropdown.DropDown;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.scripting.ClientListenerUtil;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.AbstractOperationHandler;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.rap.rwt.scripting.ClientListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;


@SuppressWarnings( "restriction" )
public class AutoSuggest {

  private static final String EVENT_TYPE_SELECTION = "suggestionSelected";
  private static final String REMOTE_TYPE = "rwt.remote.Model";
  private static final String MODEL_ID_KEY = "org.eclipse.rap.addons.autosuggest#Model";

  private final Text text;
  private final DropDown dropDown;
  private final List<SuggestionSelectedListener> selectionListeners;
  private ClientListener textClientListener;
  private int[] textClientListenerTypes;
  private boolean isDisposed;
  private final RemoteObject remoteObject;

  public AutoSuggest( Text text ) {
    if( text == null ) {
      throw new NullPointerException( "Text must not be null" );
    }
    if( text.isDisposed() ) {
      throw new IllegalArgumentException( "Text must not be disposed" );
    }
    this.text = text;
    dropDown = new DropDown( text );
    ModelResources.ensure();
    remoteObject = RWT.getUISession().getConnection().createRemoteObject( REMOTE_TYPE );
    remoteObject.setHandler( new AbstractOperationHandler() {
      @Override
      public void handleNotify( String event, JsonObject properties ) {
        if( EVENT_TYPE_SELECTION.equals( event ) ) {
          notifySelectionListeners();
        }
      }
    } );
    selectionListeners = new ArrayList<SuggestionSelectedListener>( 1 );
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
    remoteObject.set( "dataSourceId", dataSource != null ? dataSource.getId() : null );
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
    remoteObject.set( "autoComplete", value );
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
      remoteObject.listen( EVENT_TYPE_SELECTION, true );
    }
  }

  public void removeSelectionListener( SuggestionSelectedListener listener ) {
    checkDisposed();
    if( listener == null ) {
      throw new NullPointerException( "Parameter was null: listener" );
    }
    selectionListeners.remove( listener );
    if( selectionListeners.size() == 0 ) {
      remoteObject.listen( EVENT_TYPE_SELECTION, false );
    }
  }

  public void dispose() {
    if( !isDisposed ) {
      isDisposed = true;
      dropDown.dispose();
      remoteObject.destroy();
      removeTextClientListeners();
    }
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
    int[] dropDownEventTypes = new int[] { SWT.Show, SWT.Hide, SWT.Selection, SWT.DefaultSelection };
    attachClientListenerToDropDown( EventDelegatorListener.getInstance(), dropDownEventTypes );
    attachClientListenerToText( EventDelegatorListener.getInstance(), SWT.Modify, SWT.Verify );
    attachClientListenerToModel( getAutoSuggestListener(), "change", "accept" );
  }

  protected ClientListener getAutoSuggestListener() {
    return AutoSuggestListener.getInstance();
  }

  private void attachClientListenerToText( ClientListener listener, int... types ) {
    textClientListenerTypes = types;
    textClientListener = listener;
    for( int type : types ) {
      text.addListener( type, listener );
    }
  }

  private void attachClientListenerToDropDown( ClientListener listener, int... types ) {
    for( int type : types ) {
      dropDown.addListener( type, listener );
    }
  }

  private void attachClientListenerToModel( ClientListener listener, String... types ) {
    String listenerId = ClientListenerUtil.getRemoteId( listener );
    for( String type : types ) {
      remoteObject.call( "addListener",
                         new JsonObject().add( "listener", listenerId ).add( "type", type ) );
    }
    remoteObject.set( "autoSuggestListenerId", listenerId );
  }

  private void connectClientObjects() {
    WidgetUtil.registerDataKeys( MODEL_ID_KEY );
    remoteObject.set( "textWidgetId", getId( text ) );
    remoteObject.set( "dropDownWidgetId", getId( dropDown ) );
    dropDown.setData( MODEL_ID_KEY, remoteObject.getId() );
    text.setData( MODEL_ID_KEY, remoteObject.getId() );
  }

  private void removeTextClientListeners() {
    for( int type : textClientListenerTypes ) {
      text.removeListener( type, textClientListener );
    }
  }

}
