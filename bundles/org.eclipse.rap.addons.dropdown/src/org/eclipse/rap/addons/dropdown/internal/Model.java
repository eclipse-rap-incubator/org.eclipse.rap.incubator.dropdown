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
package org.eclipse.rap.addons.dropdown.internal;

import java.util.*;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.remote.AbstractOperationHandler;
import org.eclipse.rap.rwt.remote.RemoteObject;


public class Model {

  private static final String REMOTE_TYPE = "rwt.remote.Model";

  private RemoteObject remoteObject;
  private Map<String, List<ModelListener>> listeners = new HashMap<String, List<ModelListener>>();

  public Model() {
    remoteObject = RWT.getUISession().getConnection().createRemoteObject( REMOTE_TYPE );
    remoteObject.setHandler( new InternalOperationHandler() );
  }

  public void set( String name, String value ) {
    remoteObject.set( name, value );
  }

  public void set( String name, int value ) {
    remoteObject.set( name, value );
  }

  public void set( String name, JsonValue value ) {
    remoteObject.set( name, value );
  }

  public void addListener( String eventType, ModelListener listener ) {
    checkArguments( eventType, listener );
    if( listener instanceof ClientModelListener ) {
      ( ( ClientModelListener )listener ).addTo( this, eventType );
    } else {
      addServerListener( eventType, listener );
    }
  }

  public void removeListener( String eventType, ModelListener listener ) {
    checkArguments( eventType, listener );
    if( listener instanceof ClientModelListener ) {
      ( ( ClientModelListener )listener ).removeFrom( this, eventType );
    } else {
      boolean removed = getListeners( eventType ).remove( listener );
      if( removed && !hasListeners( eventType ) ) {
        remoteObject.listen( eventType, false );
      }
    }
  }

  public void destroy() {
    remoteObject.destroy();
  }

  public String getId() {
    return remoteObject.getId();
  }

  private static void checkArguments( Object... arguments ) {
    for( Object argument : arguments ) {
      if( argument == null ) {
        throw new IllegalArgumentException( "Argument may not be null" );
      }
    }
  }

  private void addServerListener( String eventType, ModelListener listener ) {
    if( !hasListeners( eventType ) ) {
      remoteObject.listen( eventType, true );
    }
    List<ModelListener> eventListeners = getListeners( eventType );
    if( !eventListeners.contains( listener ) ) {
      eventListeners.add( listener );
    }
  }

  private List<ModelListener> getListeners( String eventType ) {
    List<ModelListener> result = listeners.get( eventType );
    if( result == null ) {
      result = new ArrayList<ModelListener>();
      listeners.put( eventType, result );
    }
    return result;
  }

  private boolean hasListeners( String eventType ) {
    return listeners.containsKey( eventType ) && !listeners.get( eventType ).isEmpty();
  }

  private class InternalOperationHandler extends AbstractOperationHandler {

    @Override
    public void handleNotify( String event, JsonObject properties ) {
      List<ModelListener> eventListeners = getListeners( event );
      for( ModelListener eventListener : eventListeners ) {
        eventListener.handleEvent( properties );
      }
    }

  }

}
