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

package org.eclipse.rap.rwt.client.old;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptLoader;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectImpl;
import org.eclipse.rap.rwt.remote.OperationHandler;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.rap.rwt.service.ResourceManager;


@SuppressWarnings("restriction")
public class UniversalRemoteObject implements RemoteObject {

  private static final String UNIVERSAL_REMOTE_OBJECT_JS = "UniversalRemoteObject.js";
  private static final String REMOTE_TYPE = "rwt.client.UniversalRemoteObject";
  private Map<String, Object> properties = new HashMap<String, Object>();
  private RemoteObjectImpl remoteObject;
  private OperationHandler handler;

  public UniversalRemoteObject() {
    this( null );
  }

  public UniversalRemoteObject( OperationHandler handler ) {
    ensureTypeHandler();
    setHandler( handler );
    remoteObject
      = ( RemoteObjectImpl )RWT.getUISession().getConnection().createRemoteObject( REMOTE_TYPE );
    remoteObject.setHandler( new InternalOperationHandler() );
  }

  public void listen( String eventType, boolean listen ) {
    remoteObject.listen( eventType, listen );
  }

  public void set( String name, int value ) {
    remoteObject.set( name, value );
    properties.put( name, new Integer( value ) );
  }

  public void set( String name, double value ) {
    remoteObject.set( name, value );
    properties.put( name, new Double( value ) );
  }

  public void set( String name, boolean value ) {
    remoteObject.set( name, value );
    properties.put( name, new Boolean( value ) );
  }

  public void set( String name, String value ) {
    remoteObject.set( name, value );
    properties.put( name, value );
  }

  public void set( String name, Object value ) {
    remoteObject.set( name, value );
    properties.put( name, value );
  }

  public void call( String method, Map<String, Object> properties ) {
    handler.handleCall( method, properties );
    remoteObject.call( method, properties );
  }

  public void destroy() {
    remoteObject.destroy();
  }

  public void setHandler( OperationHandler handler ) {
    if( handler != null ) {
      this.handler = handler;
    } else {
      this.handler = new OperationAdapter(){};
    }
  }

  ////////////////////////////////////
  // Note part of the RemoteObject API

  public String getId() {
    return remoteObject.getId();
  }

  public Object get( String property ) {
    return properties.get( property );
  }

  public int getInt( String property ) {
    return ( ( Integer )properties.get( property ) ).intValue();
  }

  public double getDouble( String property ) {
    return ( ( Double )properties.get( property ) ).doubleValue();
  }

  public boolean getBoolean( String property ) {
    return ( ( Boolean )properties.get( property ) ).booleanValue();
  }

  public String getString( String property ) {
    return ( ( String )properties.get( property ) );
  }

  // Support SWT constants?
  public void notify( String event, Map<String, Object> properties ) {
    handler.handleNotify( event, properties );
    remoteObject.call( "_notifyInternal", properties );
  }

  ////////////
  // internals

  private void ensureTypeHandler() {
    ResourceManager manager = RWT.getResourceManager();
    if( !manager.isRegistered( UNIVERSAL_REMOTE_OBJECT_JS ) ) {
      manager.register(
        UNIVERSAL_REMOTE_OBJECT_JS,
        getClass().getResourceAsStream( UNIVERSAL_REMOTE_OBJECT_JS )
      );
    }
    JavaScriptLoader jsl = RWT.getClient().getService( JavaScriptLoader.class );
    jsl.require( manager.getLocation( UNIVERSAL_REMOTE_OBJECT_JS ) );
  }

  private final class InternalOperationHandler implements OperationHandler {

    public void handleSet( Map<String, Object> properties ) {
      UniversalRemoteObject.this.properties.putAll( properties );
      UniversalRemoteObject.this.handler.handleSet( properties );
    }

    public void handleNotify( String event, Map<String, Object> properties ) {
      UniversalRemoteObject.this.handler.handleNotify( event, properties );
    }

    public void handleCall( String method, Map<String, Object> parameters ) {
      UniversalRemoteObject.this.handler.handleCall( method, parameters );
    }

  }

}
