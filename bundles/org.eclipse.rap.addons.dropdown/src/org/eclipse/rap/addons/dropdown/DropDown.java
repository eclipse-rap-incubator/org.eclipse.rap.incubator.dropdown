/*******************************************************************************
 * Copyright (c) 2013 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.addons.dropdown;

import java.util.Arrays;

import org.eclipse.rap.addons.dropdown.internal.resources.DropDownResources;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteList;
import org.eclipse.rap.rwt.internal.protocol.IClientObjectAdapter;
import org.eclipse.rap.rwt.internal.protocol.JsonUtil;
import org.eclipse.rap.rwt.lifecycle.WidgetAdapter;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.AbstractOperationHandler;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.events.EventLCAUtil;
import org.eclipse.swt.internal.widgets.WidgetAdapterImpl;
import org.eclipse.swt.widgets.*;


@SuppressWarnings("restriction")
public class DropDown extends Widget {

  private static final String REMOTE_TYPE = "rwt.dropdown.DropDown";
  private static final String SELECTION = "Selection";
  private static final String DEFAULT_SELECTION = "DefaultSelection";

  private RemoteObject remoteObject;
  private Object widgetAdapter;
  private final Control parent;
  private final Listener disposeListener;
  private boolean visibility = false;
  private int selectionIndex = -1;
  private int itemCount = 5;

  public DropDown( Control parent ) {
    super( parent, 0 );
    this.parent = parent;
    DropDownResources.ensure();
    getRemoteObject().set( "parent", WidgetUtil.getId( parent ) );
    getRemoteObject().setHandler( new InternalOperationHandler() );
    disposeListener = new Listener() {
      public void handleEvent( Event event ) {
        DropDown.this.dispose();
      }
    };
    parent.addListener( SWT.Dispose, disposeListener );
  }

  public void setItems( String[] strings ) {
    checkWidget();
    remoteObject.set( "items", JsonUtil.createJsonArray( strings ) );
    setSelectionIndexImpl( -1 );
  }

  public int getSelectionIndex() {
    checkWidget();
    return selectionIndex;
  }

  public void setVisibleItemCount( int itemCount ) {
    checkWidget();
    this.itemCount = itemCount;
    remoteObject.set( "visibleItemCount", itemCount );
  }

  public int getVisibleItemCount() {
    checkWidget();
    return itemCount;
  }

  public void show() {
    setVisibility( true );
  }

  public void hide() {
    setVisibility( false );
  }

  public void setVisibility( boolean value ) {
    checkWidget();
    if( visibility != value ) {
      setVisibilityImpl( value );
      remoteObject.set( "visibility", value );
    }
  }

  public boolean getVisibility() {
    return visibility;
  }

  public Control getParent() {
    return parent;
  }

  @Override
  public void dispose() {
    if( !isDisposed() ) {
      super.dispose();
      parent.removeListener( SWT.Dispose, disposeListener );
      remoteObject.destroy();
    }
  }

  @Override
  public void setData( String key, Object value ) {
    super.setData( key, value );
    renderData( key, value );
  }

  @Override
  public void addListener( int eventType, Listener listener ) {
    boolean wasListening = EventLCAUtil.isListening( this, eventType );
    super.addListener( eventType, listener );
    boolean isListening = EventLCAUtil.isListening( this, eventType );
    String remoteType = eventTypeToString( eventType );
    if( remoteType != null && !wasListening && isListening ) {
      remoteObject.listen( remoteType, true );
    }
  }

  @Override
  public void removeListener( int eventType, Listener listener ) {
    boolean wasListening = EventLCAUtil.isListening( this, eventType );
    super.removeListener( eventType, listener );
    boolean isListening = EventLCAUtil.isListening( this, eventType );
    String remoteType = eventTypeToString( eventType );
    if( remoteType != null && wasListening && !isListening ) {
      remoteObject.listen( remoteType, false );
    }
  }

  //////////////
  // overwritten

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    T result;
    if( adapter == IClientObjectAdapter.class || adapter == WidgetAdapter.class ) {
      // TODO [tb] : This way of getting the right id into the WidgetAdapter is obviously
      //             not ideal. Revise once Bug 397602 (Render operations in the order of their
      //             occurrence) is fixed.
      if( widgetAdapter == null ) {
        widgetAdapter = new WidgetAdapterImpl( getProtocolId() );
      }
      result = ( T )widgetAdapter;
    } else {
      result = super.getAdapter( adapter );
    }
    return result;
  }

  //////////
  // private


  private class InternalOperationHandler extends AbstractOperationHandler {

    @Override
    public void handleSet( JsonObject properties ) {
      if( properties.get( "visibility" ) != null ) {
        setVisibilityImpl( properties.get( "visibility" ).asBoolean() );
      }
      if( properties.get( "selectionIndex" ) != null ) {
        setSelectionIndexImpl( properties.get( "selectionIndex" ).asInt() );
      }
    }

    @Override
    public void handleNotify( String type, JsonObject properties ) {
      if( SELECTION.equals( type ) || DEFAULT_SELECTION.equals( type )) {
        Event event = new Event();
        event.index = properties.get( "index" ).asInt();
        event.text = properties.get( "text" ).asString();
        notifyListeners( stringToEventType( type ), event );
      }
    }

  }

  private void setVisibilityImpl( boolean value ) {
    visibility = value;
  }

  private void setSelectionIndexImpl( int value ) {
    selectionIndex = value;
  }

  private String getProtocolId() {
    return getRemoteObject().getId();
  }

  private RemoteObject getRemoteObject() {
    if( remoteObject == null ) {
      remoteObject = RWT.getUISession().getConnection().createRemoteObject( REMOTE_TYPE );
    }
    return remoteObject;
  }

  private void renderData( String key, Object value ) {
    // TODO [tb] : could be optimized using a PhaseListener
    //             This implementation assumes the client merges the new values with the existing
    //             ones, which is the case in the WebClient
    WidgetDataWhiteList service = RWT.getClient().getService( WidgetDataWhiteList.class );
    String[] dataKeys = service == null ? null : service.getKeys();
    if( dataKeys != null && Arrays.asList( dataKeys ).contains( key ) ) {
      @SuppressWarnings( "deprecation" )
      JsonObject data = new JsonObject().add( key, JsonUtil.createJsonValue( value ) );
      remoteObject.call( "setData", data );
    }
  }

  private static String eventTypeToString( int type ) {
    String result;
    switch( type ) {
      case SWT.Selection:
        result = SELECTION;
      break;
      case SWT.DefaultSelection:
        result = DEFAULT_SELECTION;
      break;
      default:
        result = null;
      break;
    }
    return result;
  }

  private static int stringToEventType( String str ) {
    int result = -1;
    if( SELECTION.equals( str ) ) {
      result = SWT.Selection;
    } else if( DEFAULT_SELECTION.equals( str ) ) {
      result = SWT.DefaultSelection;
    }
    return result;
  }

}
