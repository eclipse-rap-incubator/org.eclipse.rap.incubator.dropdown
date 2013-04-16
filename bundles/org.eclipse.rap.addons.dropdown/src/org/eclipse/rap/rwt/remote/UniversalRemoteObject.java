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

package org.eclipse.rap.rwt.remote;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptLoader;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectImpl;
import org.eclipse.rap.rwt.service.ResourceManager;

@SuppressWarnings("restriction")
public class UniversalRemoteObject implements RemoteObject {

  private static final String REMOTE_TYPE = "rwt.remote.UniversalRemoteObject";
  private static final String UNIVERSAL_REMOTE_OBJECT_JS
    = "rwt/remote/UniversalRemoteObject.js";

  private RemoteObjectImpl remoteObject;
  private Map<String, Object> properties = new HashMap<String, Object>();
  private OperationHandler handler;


  public UniversalRemoteObject() {
    ensureTypeHandler();
    remoteObject
      = ( RemoteObjectImpl )RWT.getUISession().getConnection().createRemoteObject( REMOTE_TYPE );
  }

  public String getId() {
    return remoteObject.getId();
  }

  private void ensureTypeHandler() {
    ResourceManager manager = RWT.getResourceManager();
    if( !manager.isRegistered( UNIVERSAL_REMOTE_OBJECT_JS ) ) {
      manager.register(
        UNIVERSAL_REMOTE_OBJECT_JS,
        getClass().getClassLoader().getResourceAsStream( UNIVERSAL_REMOTE_OBJECT_JS )
      );
    }
    JavaScriptLoader jsl = RWT.getClient().getService( JavaScriptLoader.class );
    jsl.require( manager.getLocation( UNIVERSAL_REMOTE_OBJECT_JS ) );
  }

  // TODO : also invoke set operation handler?
  public void set( String name, int value ) {
    properties.put( name, new Integer( value ) );
    remoteObject.set( name, value );
  }

  public void set( String name, double value ) {
    properties.put( name, new Double( value ) );
    remoteObject.set( name, value );
  }

  public void set( String name, boolean value ) {
    properties.put( name, new Boolean( value ) );
    remoteObject.set( name, value );
  }

  public void set( String name, String value ) {
    properties.put( name, value );
    remoteObject.set( name, value );
  }

  public void set( String name, Object value ) {
    properties.put( name, value );
    remoteObject.set( name, value );
  }

  public void listen( String eventType, boolean listen ) {
    remoteObject.listen( eventType, listen );
  }

  public void call( String method, Map<String, Object> properties ) {
    throw new UnsupportedOperationException( "Operation \"call\" not supported on this type" );
  }

  public void setHandler( OperationHandler handler ) {
    this.handler = handler;
    remoteObject.setHandler( handler );
  }

  public Object get( String key ) {
    checkDestroyed();
    return properties.get( key );
  }

  public boolean getBoolean( String key ) {
    return ( ( Boolean )get( key ) ).booleanValue();
  }

  public double getDouble( String key ) {
    return ( ( Double )get( key ) ).doubleValue();
  }

  public int getInt( String key ) {
    return ( ( Integer )get( key ) ).intValue();
  }

  public String getString( String key ) {
    return ( String )get( key );
  }

  public void notify( String event, Map<String, Object> properties ) {
    // TODO Currently good for testing, but could also trigger client listener
    handler.handleNotify( event, properties );
  }

  public void destroy() {
    remoteObject.destroy();
  }

  public boolean isDestroyed() {
    return remoteObject.isDestroyed();
  }

  private void checkDestroyed() {
    if( remoteObject.isDestroyed() ) {
      throw new IllegalStateException( "The RemoteObject was alredy destroyed" );
    }
  }

}
