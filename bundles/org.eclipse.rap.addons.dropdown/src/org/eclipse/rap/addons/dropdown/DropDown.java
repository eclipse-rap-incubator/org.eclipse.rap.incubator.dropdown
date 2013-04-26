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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.addons.dropdown.internal.resources.DropDownResources;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.client.WidgetDataWhiteList;
import org.eclipse.rap.rwt.internal.protocol.IClientObjectAdapter;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectImpl;
import org.eclipse.rap.rwt.lifecycle.WidgetAdapter;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.AbstractOperationHandler;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.widgets.WidgetAdapterImpl;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

@SuppressWarnings("restriction")
public class DropDown extends Widget {


  private static final String REMOTE_TYPE = "rwt.dropdown.DropDown";
  private static final String SELECTION = "Selection";
  private static final String DEFAULT_SELECTION = "DefaultSelection";

  private RemoteObject remoteObject;
  private boolean disposed = false;
  private Object widgetAdapter;
  private final Control parent;
  private final Listener disposeListener;
  private boolean visibility = false;
  private int selectionIndex = -1;

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

  public Control getParent() {
    return parent;
  }

  public void setItems( String[] strings ) {
    remoteObject.set( "items", strings.clone() );
    setSelectionIndexImpl( -1 );
  }

  public int getSelectionIndex() {
    return selectionIndex;
  }

  public void show() {
    setVisibility( true );
  }

  public void hide() {
    setVisibility( false );
  }

  public void setVisibility( boolean value ) {
    checkDisposed();
    if( visibility != value ) {
      setVisibilityImpl( value );
      remoteObject.set( "visibility", value );
    }
  }

  public boolean getVisibility() {
    return visibility;
  }

  @Override
  public void dispose() {
    if( !disposed ) {
      super.dispose();
      parent.removeListener( SWT.Dispose, disposeListener );
      remoteObject.destroy();
      disposed = true;
    }
  }

  @Override
  public void setData( String key, Object value ) {
    super.setData( key, value );
    renderData( key, value );
  }

  public void setVisibleItemCount( int itemCount ) {
    remoteObject.set( "visibleItemCount", itemCount );
  }

  @Override
  public void addListener( int type, Listener listener ) {
    super.addListener( type, listener );
    String remoteType = evenTypeToString( type );
    if( remoteType != null ) {
      remoteObject.listen( remoteType, true );
    }
  }

  @Override
  public void removeListener( int type, Listener listener ) {
    super.removeListener( type, listener );
    String remoteType = evenTypeToString( type );
    if( remoteType != null ) {
      remoteObject.listen( remoteType, false );
    }
  }

  ////////////
  // Internals

  private class InternalOperationHandler extends AbstractOperationHandler {

    @Override
    public void handleSet( Map<String, Object> properties ) {
      if( properties.containsKey( "visibility" ) ) {
        setVisibilityImpl( (( Boolean )properties.get( "visibility" )).booleanValue() );
      }
      if( properties.containsKey( "selectionIndex" ) ) {
        setSelectionIndexImpl( (( Integer )properties.get( "selectionIndex" )).intValue() );
      }
    }

    @Override
    public void handleNotify( String type, Map<String, Object> properties ) {
      if( SELECTION.equals( type ) || DEFAULT_SELECTION.equals( type )) {
        Event event = new Event();
        event.index = (( Integer )properties.get( "index" )).intValue();
        event.text = ( String )properties.get( "text" );
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

  private void checkDisposed() {
    if( disposed ) {
      throw new IllegalStateException( "DropDown is disposed" );
    }
  }

  private String getProtocolId() {
    return ( ( RemoteObjectImpl )getRemoteObject() ).getId();
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
      Map<String, Object> data = new HashMap<String, Object>();
      data.put( key, value );
      remoteObject.call( "setData", data );
    }
  }

  private static String evenTypeToString( int type ) {
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
