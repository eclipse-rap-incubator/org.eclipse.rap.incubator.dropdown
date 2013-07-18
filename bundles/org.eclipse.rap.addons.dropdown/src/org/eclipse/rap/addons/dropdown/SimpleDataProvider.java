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
package org.eclipse.rap.addons.dropdown;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.remote.Connection;
import org.eclipse.rap.rwt.remote.RemoteObject;


public class SimpleDataProvider {

  private static final String REMOTE_TYPE = "rwt.remote.Model";
  private RemoteObject remoteObject;

  public SimpleDataProvider( String[] data ) {
    if( data == null ) {
      throw new NullPointerException( "Data must not be null" );
    }
    Connection connection = RWT.getUISession().getConnection();
    remoteObject = connection.createRemoteObject( REMOTE_TYPE );
    JsonArray array = createJsonArray( data );
    remoteObject.set( "data", array );
  }

  public String getId() {
    return remoteObject.getId();
  }

  private static JsonArray createJsonArray( String[] data ) {
    JsonArray array = new JsonArray();
    for( int i = 0; i < data.length; i++ ) {
      array.add( data[ i ] );
    }
    return array;
  }

}
